package com.logistica.despachos.infrastructure.web.controller;

import com.logistica.despachos.domain.model.DespachoEvento;
import com.logistica.despachos.domain.port.in.*;
import com.logistica.despachos.infrastructure.web.dto.DespachoRequest;
import com.logistica.despachos.infrastructure.web.dto.DespachoResponse;
import com.logistica.despachos.infrastructure.web.filter.TrazaIdWebFilter;
import com.logistica.despachos.infrastructure.web.support.TrazaIdSupport;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/despachos")
public class DespachoController {

    private final CrearDespachoUseCase crearUseCase;
    private final ConsultarDespachoUseCase consultarUseCase;
    private final ConfirmarDespachoUseCase confirmarUseCase;
    private final SuscribirEventosDespachoUseCase eventosUseCase;

    public DespachoController(CrearDespachoUseCase crearUseCase,
                              ConsultarDespachoUseCase consultarUseCase,
                              ConfirmarDespachoUseCase confirmarUseCase,
                              SuscribirEventosDespachoUseCase eventosUseCase) {
        this.crearUseCase = crearUseCase;
        this.consultarUseCase = consultarUseCase;
        this.confirmarUseCase = confirmarUseCase;
        this.eventosUseCase = eventosUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DespachoResponse> crear(
            @Valid @RequestBody DespachoRequest request,
            @RequestHeader(value = TrazaIdWebFilter.HEADER, required = false) String trazaIdHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {

        return TrazaIdSupport.actual().flatMap(trazaId -> {
            var paquetes = request.paquetes().stream()
                    .map(p -> new CrearDespachoUseCase.ItemPaquete(p.vehiculoId(), p.pesoKg()))
                    .toList();

            var comando = new CrearDespachoUseCase.Comando(
                    request.clienteId(), request.ciudad(), paquetes, trazaId, idemKey);

            return crearUseCase.crear(comando).map(DespachoResponse::desde);
        });
    }

    @GetMapping("/{id}")
    public Mono<DespachoResponse> obtener(@PathVariable Long id) {
        return consultarUseCase.obtener(id).map(DespachoResponse::desde);
    }

    @PostMapping("/{id}/confirm")
    public Mono<DespachoResponse> confirmar(@PathVariable Long id) {
        return confirmarUseCase.confirmar(id).map(DespachoResponse::desde);
    }

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DespachoEvento> eventos(@PathVariable("id") Long id) {
        return eventosUseCase.suscribir(id);
    }
}