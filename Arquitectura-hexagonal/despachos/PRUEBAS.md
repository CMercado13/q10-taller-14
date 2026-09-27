# Guía de ejecución y pruebas — Servicio de Despachos

Microservicio reactivo (Spring WebFlux + R2DBC + PostgreSQL) para gestión de despachos logísticos:
creación con reserva de cupo tipo saga, consulta de tarifa/clima/scoring externos, confirmación,
expiración automática, reportes en vivo y streaming de eventos (SSE/NDJSON).

## 1. Prerrequisitos

- **Java 17** (el `build.gradle` usa toolchain 17)
- **Docker** o **Podman** (para levantar PostgreSQL)
- **PowerShell 7+** (para la prueba de concurrencia con `ForEach-Object -Parallel`)
- `curl.exe` (viene con Windows 10/11; **no usar el alias `curl` de PowerShell**, usar siempre `curl.exe`)

## 2. Levantar la base de datos

```powershell
# Con Docker
docker compose up -d

# Con Podman
podman-compose up -d
```

Esto crea Postgres en `localhost:5432` (db `testdb`, user/pass `postgres`/`postgres`).

> ⚠️ El archivo `schema.sql` hace `DROP TABLE` + `CREATE TABLE` en **cada arranque** de la app
> (`spring.sql.init.mode=always`). Los datos se reinician cada vez que reinicias la aplicación.
> Se insertan 3 vehículos semilla: `id=1 (BOG, 500kg)`, `id=2 (MDE, 200kg)`, `id=3 (CLO, 800kg)`.

## 3. Ejecutar la aplicación

```powershell
.\gradlew.bat bootRun
```

La app queda escuchando en **`http://localhost:8081`**.

Documentación interactiva (Swagger):
```
http://localhost:8081/swagger-ui.html
```

## 4. Contrato HTTP por endpoint

Antes de probar, es clave respetar **exactamente** el `Content-Type` que cada endpoint declara en
`consumes` (si envías el tipo equivocado, WebFlux responde `415 Unsupported Media Type` **antes**
de llegar al controlador, y ese error no pasa por el `@RestControllerAdvice` custom).

| Método | Path                              | `Content-Type` (request) | `Accept` recomendado        | Headers opcionales                              | Status éxito |
|--------|-----------------------------------|---------------------------|------------------------------|--------------------------------------------------|--------------|
| POST   | `/api/vehiculos/bulk`             | `application/x-ndjson`    | `application/json`            | —                                                | 200 OK       |
| POST   | `/api/despachos`                  | `application/json`        | `application/json`            | `X-Traza-Id`, `Idempotency-Key`                  | 201 Created  |
| GET    | `/api/despachos/{id}`             | —                         | `application/json`            | —                                                | 200 OK       |
| POST   | `/api/despachos/{id}/confirm`     | — (sin body)              | `application/json`            | —                                                | 200 OK       |
| GET    | `/api/despachos/{id}/events`      | —                         | `text/event-stream`           | —                                                | 200 OK (stream) |
| GET    | `/api/ops/tablero`                | —                         | `text/event-stream`           | —                                                | 200 OK (stream) |
| GET    | `/api/reports/ciudades`           | —                         | `application/json`            | —                                                | 200 OK       |
| GET    | `/api/reports/ciudades/stream`    | —                         | `application/x-ndjson`        | —                                                | 200 OK (stream) |
| GET/PUT/DELETE | `/external/simulator`     | `application/json` (PUT)  | `application/json`            | —                                                | 200 OK       |

**Reglas clave:**
- `POST /api/despachos`: **siempre** envía `Content-Type: application/json` explícito. Sin este
  header, Spring no puede deserializar el body y responde `415`/`400` antes de validar campos.
- `POST /api/vehiculos/bulk`: **solo** acepta `application/x-ndjson` (declarado con
  `consumes = MediaType.APPLICATION_NDJSON_VALUE` en `VehiculoController`). Un array JSON normal
  (`[{...},{...}]`) con `Content-Type: application/json` será **rechazado con 415**.
- `X-Traza-Id`: opcional. Si no lo envías, `TrazaIdWebFilter` genera uno (`UUID`) y lo devuelve en
  la respuesta bajo el mismo header — revísalo con `curl.exe -i` para verlo en los headers de salida.
- `Idempotency-Key`: opcional, pero si la reutilizas en una segunda solicitud **con un body
  distinto**, el servicio te devuelve el resultado de la **primera** ejecución (no vuelve a
  procesar). Usa una key nueva por cada escenario de prueba.
- Los endpoints de streaming (`/events`, `/tablero`, `/ciudades/stream`) **no cierran la
  conexión** — no importa el `Accept` que envíes, siempre responden en el `produces` declarado en
  el controlador (`text/event-stream` o `application/x-ndjson`).

## 5. Flujo de pruebas manual

Todos los ejemplos usan `curl.exe` desde PowerShell. Los cuerpos se escriben a archivo primero
porque PowerShell no soporta bien las comillas anidadas ni `$'...\n'` como bash.

### 5.1 Carga masiva de vehículos (NDJSON)

> Nota: los vehículos `1`, `2` y `3` ya vienen precargados por `schema.sql`. Este paso es útil si
> quieres agregar vehículos adicionales o reemplazar el catálogo.

```powershell
@'
{"id":1,"placa":"ABC123","ciudad":"BOG","cupoKg":500,"reservadoKg":0}
{"id":2,"placa":"XYZ987","ciudad":"MDE","cupoKg":200,"reservadoKg":0}
'@ | Out-File vehiculos.ndjson -Encoding utf8; curl.exe -X POST "http://localhost:8081/api/vehiculos/bulk" -H "Content-Type: application/x-ndjson" --data-binary "@vehiculos.ndjson"
```

Respuesta esperada: `{"procesados":2,"lotes":1}` (según `CargaMasivaVehiculosUseCase.ResumenCarga`).

### 5.2 Crear un despacho

```powershell
@'
{"clienteId":1,"ciudad":"BOG","paquetes":[{"vehiculoId":1,"pesoKg":120}]}
'@ | Out-File -FilePath despacho.json -Encoding utf8

curl.exe -i -X POST "http://localhost:8081/api/despachos" `
  -H "Content-Type: application/json" `
  -H "Accept: application/json" `
  -H "X-Traza-Id: taller-1" `
  -H "Idempotency-Key: K1" `
  --data-binary "@despacho.json"
```

- `X-Traza-Id`: viaja por Reactor Context y se propaga en la respuesta y en los eventos publicados.
- `Idempotency-Key`: si repites la solicitud con la **misma key**, el servicio devuelve el
  despacho ya creado en vez de procesarlo de nuevo (usa una **key distinta por cada prueba nueva**
  para evitar resultados obsoletos).

Estado esperado si todo sale bien: `"estado": "ASIGNADO"` con `tarifa`, `total` y `scoreRiesgo` poblados.

### 5.3 Confirmar el despacho

```powershell
curl.exe -i -X POST http://localhost:8081/api/despachos/1/confirm -H "Accept: application/json"
```

Pasa de `ASIGNADO` → `EN_RUTA` (equivalente a "confirmado" en este dominio; no existe un
estado literal `CONFIRMADO` en el enum `EstadoDespacho`). No lleva body: enviar uno es ignorado.

### 5.4 Consultar un despacho

```powershell
curl.exe http://localhost:8081/api/despachos/1 -H "Accept: application/json"
```

### 5.5 Streams en vivo (SSE / NDJSON)

> ⚠️ Estos endpoints **no cierran la conexión**. Usa `curl.exe -N` (no-buffering) en una terminal
> aparte. **No los pruebes desde el navegador ni Postman**, se quedan "colgados" esperando el
> siguiente evento (es el comportamiento correcto de un stream infinito).

```powershell
# Tablero global de eventos de despacho (Server-Sent Events)
curl.exe -N http://localhost:8081/api/ops/tablero -H "Accept: text/event-stream"

# Eventos de UN despacho puntual (Server-Sent Events)
curl.exe -N http://localhost:8081/api/despachos/1/events -H "Accept: text/event-stream"

# Reporte de ciudades en vivo (NDJSON)
curl.exe -N http://localhost:8081/api/reports/ciudades/stream -H "Accept: application/x-ndjson"
```

Mientras esta terminal queda abierta, ejecuta creaciones/confirmaciones de despachos en **otra
terminal** y observa cómo llegan los eventos en tiempo real.

Reporte agregado (snapshot, no streaming):
```powershell
curl.exe http://localhost:8081/api/reports/ciudades -H "Accept: application/json"
```

### 5.6 Simular fallas en servicios externos (tarifa / clima / scoring)

El simulador vive en `/external/simulator` y controla el comportamiento de `/external/tarifa`,
`/external/clima` y `/external/scoring` (consumidos internamente por el propio servicio).

**Campos válidos** (ver `SimuladorConfig` / `SimuladorController`):

| Campo             | Tipo    | Efecto                                                              |
|-------------------|---------|----------------------------------------------------------------------|
| `fallarTarifa`    | boolean | La tarifa falla intermitentemente (2 de cada 3 intentos)             |
| `latenciaClimaMs` | number  | Retraso artificial (ms) en la respuesta de clima                    |
| `colgarScoring`   | boolean | El scoring "se cuelga" 5s (fuerza el timeout de 800ms del cliente)   |
| `scoreFijo`       | number  | Fuerza un score de riesgo fijo (`-1` = aleatorio; `>80` = rechazo)   |

```powershell
@'
{"fallarTarifa":true,"latenciaClimaMs":3000,"scoreFijo":95}
'@ | Out-File -FilePath sim.json -Encoding utf8
 
curl.exe -X PUT "http://localhost:8081/external/simulator" -H "Content-Type: application/json" -H "Accept: application/json" --data-binary "@sim.json"

# Ver estado actual del simulador
curl.exe http://localhost:8081/external/simulator -H "Accept: application/json"

# Volver a la normalidad
curl.exe -X DELETE http://localhost:8081/external/simulator
```

Con `scoreFijo` > 80 (`ScoreRiesgo.UMBRAL_RECHAZO`), el siguiente despacho que crees debería
quedar en `"estado": "RECHAZADO"` (zona riesgosa) y liberar los cupos reservados.

### 5.7 Prueba de concurrencia de cupo (requiere PowerShell 7)

Verifica que el cupo del vehículo (`cupo_kg`, `reservado_kg`) nunca quede negativo bajo carga
concurrente (constraint `CHECK (cupo_kg >= 0)` en `schema.sql`).

```powershell
1..20 | ForEach-Object -Parallel {
  curl.exe -s -o NUL -X POST http://localhost:8081/api/despachos `
    -H "Content-Type: application/json" -H "Accept: application/json" --data-binary "@despacho.json"
} -ThrottleLimit 20
```

> Nota: como `despacho.json` no lleva `Idempotency-Key` en este bloque, cada llamada crea un
> despacho **distinto**, forzando 20 reservas de cupo concurrentes sobre el mismo vehículo
> (`vehiculoId: 1`, cupo 500 kg). Con 120 kg por paquete, solo ~4 deberían tener éxito
> (`ASIGNADO`) y el resto debería fallar con `409 CONFLICT` (`CupoInsuficienteException`) sin que
> el cupo se sobregire.

Verifica el resultado final del vehículo:
```powershell
curl.exe http://localhost:8081/api/despachos/1 -H "Accept: application/json"
```
(o revisa directamente en la BD la columna `reservado_kg` del vehículo `1`, no debe superar 500).

## 6. Códigos de error esperados

| Excepción                              | HTTP Status               |
|-----------------------------------------|----------------------------|
| `VehiculoNoExisteException`             | 404 Not Found              |
| `DespachoNoExisteException`             | 404 Not Found              |
| `CupoInsuficienteException`             | 409 Conflict               |
| `EstadoInvalidoException`               | 409 Conflict               |
| `ZonaRiesgosaException`                 | 422 Unprocessable Entity   |
| `ValidacionException` / body inválido   | 400 Bad Request            |
| Cualquier otro error no controlado      | 500 Internal Server Error  |

## 7. Notas / troubleshooting

- Si todas las peticiones devuelven `500` con el formato genérico de Spring
  (`timestamp/path/status/error/requestId`, sin `codigo`/`mensaje`/`trazaId`), el error está
  ocurriendo **fuera** del alcance del `@RestControllerAdvice` (por ejemplo, en un `WebFilter`).
  Revisa la consola de la app para el stack trace real.
- Si recibes `415 Unsupported Media Type`, revisa la tabla de la sección 4: seguramente enviaste
  `Content-Type: application/json` a `/api/vehiculos/bulk` (que exige `application/x-ndjson`), o
  viceversa.
- Reinicia la app entre baterías de pruebas si quieres partir de datos limpios (recuerda que
  `schema.sql` resetea las tablas en cada arranque).
- Usa una `Idempotency-Key` **distinta** por cada escenario de prueba nuevo; reutilizarla
  siempre devuelve el resultado de la primera ejecución asociada a esa key.
