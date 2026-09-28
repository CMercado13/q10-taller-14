# Taller 14 · Aplicativo reactivo

Spring Boot 4.1.1 · Java 25 · WebFlux · Spring Data R2DBC + PostgreSQL 15 · Project Reactor.
App en `localhost:8081`.

## Base de datos con podman

No hace falta Docker Desktop. Dos caminos, ambos probados:

```bash
./deployment/db.sh up                  # podman directo: arranca la máquina si hace falta y espera a pg_isready
podman-compose up -d
```

Comandos del script:

| Comando | Qué hace |
|---|---|
| `./deployment/db.sh up` | Crea/arranca `postgres_r2dbc` en 5432 y espera a que acepte conexiones |
| `./deployment/db.sh down` | Detiene el contenedor (conserva los datos del volumen `pgdata`) |
| `./deployment/db.sh reset` | Borra contenedor **y volumen**, vuelve a arrancar en limpio |
| `./deployment/db.sh psql` | Abre `psql` dentro del contenedor (acepta `-c 'SELECT ...'`) |
| `./deployment/db.sh logs` | Sigue los logs de Postgres |
| `./deployment/db.sh status` | Estado del contenedor |

## Arrancar y probar

```bash
./deployment/db.sh up
./gradlew test            # XXXXXXXXX tests, incluye el flujo E2E contra Postgres
./gradlew bootRun         # app en 8081
```

Sembrar catálogo y crear una orden:

```bash
curl --request GET \
  --url http://localhost:8081/api/ops/tablero \
  --header 'Accept: text/event-stream'

curl --request GET \
  --url http://localhost:8081/api/reports/ciudades \
  --header 'Accept: application/json'

curl --request GET \
  --url http://localhost:8081/api/reports/ciudades/stream

curl --request POST \
  --url http://localhost:8081/api/despachos/1/confirm \
  --header 'Accept: application/json'
  
curl --request GET \
  --url http://localhost:8081/api/despachos/1 \
  --header 'Accept: application/json'
  
curl --request POST \
  --url http://localhost:8081/api/despachos \
  --header 'Content-Type: application/json' \
  --data '{
	"clienteId": 12345,
	"ciudad": "BOG",
	"paquetes": [
		{
			"vehiculoId": 2,
			"pesoKg": 1
		},
		{
			"vehiculoId": 1,
			"pesoKg": 8
		}
	]
}'

curl --request GET \
  --url http://localhost:8081/api/despachos/1/events \
  --header 'Accept: text/event-stream'  
  
curl -X POST http://localhost:8081/api/vehiculos/bulk -H 'Content-Type: application/x-ndjson' \
  --data-binary $'{"id":1,"placa":"XYZ123","ciudad":"CAR","cupoKg":5000.0,"reservadoKg":0.0}\n{"id":5,"placa":"ABC781","ciudad":"TUR","cupoKg":8000.0,"reservadoKg":1200.0}\n{"id":6,"placa":"QWE426","ciudad":"VLL","cupoKg":3500.0,"reservadoKg":500.0}\n'
```

Inspeccionar la base (equivalente podman del `docker exec` del documento):

```bash
./deployment/db.sh psql -c 'SELECT * FROM vehiculo LIMIT 5;' \
                    -c 'SELECT * FROM paquete LIMIT 5;' \
                    -c 'SELECT * FROM despacho LIMIT 5;'
```

## Endpoints

| Método | Endpoint (URL) | Tipo de Cabecera | Propósito / Formato |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/ops/tablero` | `Accept: text/event-stream` | Consultar el tablero con actualizaciones en tiempo real (Streaming). |
| **GET** | `/api/reports/ciudades` | `Accept: application/json` | Obtener el reporte de ciudades en formato JSON. |
| **GET** | `/api/reports/ciudades/stream` | *Ninguna* | Obtener el reporte de ciudades en flujo continuo (Stream). |
| **POST** | `/api/despachos/1/confirm` | `Accept: application/json` | Confirmar el despacho con ID 1 y esperar respuesta en JSON. |
| **GET** | `/api/despachos/1` | `Accept: application/json` | Consultar el detalle del despacho con ID 1 en formato JSON. |
| **POST** | `/api/despachos` | `Content-Type: application/json` | Crear un nuevo despacho enviando los datos del cliente y paquetes en JSON. |
| **GET** | `/api/despachos/1/events` | `Accept: text/event-stream` | Escuchar los eventos del despacho 1 en tiempo real (Streaming). |

- Errores: `VehiculoNoExisteException` 404 · `CupoInsuficienteException` 409 · `ZonaRiesgosaException` 422 · `DespachoNoExisteException` 404 · `EstadoInvalidoException` 409 · `ValidacionException` 400. 
- Cuerpo de error uniforme: { "codigo", "mensaje", "trazaId", "instante" }.
  ```
  {
  "codigo": "ESTADO_INVALIDO",
  "mensaje": "No se puede pasar de EN_RUTA a ASIGNADO",
  "trazaId": "ab36ac68-ba36-4b3d-b2bc-666e1fa21720",
  "instante": "2026-09-27T22:21:36.203988600Z"
  }

## Notas de entorno

- `podman compose` (subcomando) delega en `/usr/local/bin/docker-compose` y falla con
  `docker-credential-desktop: executable file not found` porque `~/.docker/config.json` conserva
  `"credsStore": "desktop"` de una instalación de Docker Desktop ya borrada. Usa `podman-compose`
  o `./deployment/db.sh`. Para arreglarlo de raíz: quitar esa línea del `~/.docker/config.json`.
- `/usr/local/bin/docker` es un symlink roto a `/Applications/Docker.app` (desinstalada): por eso
  `docker` da `command not found`.
- Las imágenes se referencian como `docker.io/library/postgres:15`; podman no asume el registro.
