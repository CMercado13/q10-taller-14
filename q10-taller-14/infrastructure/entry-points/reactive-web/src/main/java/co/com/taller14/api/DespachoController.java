package co.com.taller14.api;

import co.com.taller14.api.dto.DespachoRequest;
import co.com.taller14.api.dto.DespachoResponse;
import co.com.taller14.api.support.TrazaIdSupport;
import co.com.taller14.model.despacho.Despacho;
import co.com.taller14.model.despacho.DespachoEvento;
import co.com.taller14.usecase.despacho.ConfirmarDespachoUseCase;
import co.com.taller14.usecase.despacho.ConsultarDespachoUseCase;
import co.com.taller14.usecase.despacho.CrearDespachoUseCase;
import co.com.taller14.usecase.despacho.EventosDespachoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/despachos")
@RequiredArgsConstructor
public class DespachoController {

    private final CrearDespachoUseCase crearDespachoUseCase;
    private final ConsultarDespachoUseCase consultarDespachoUseCase;
    private final ConfirmarDespachoUseCase confirmarDespachoUseCase;
    private final EventosDespachoUseCase eventosDespachoUseCase;

    @PostMapping
    public Mono<ResponseEntity<DespachoResponse>> crear(@Valid @RequestBody DespachoRequest request,
                                                        @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {

        return TrazaIdSupport.actual().flatMap(trazaId -> {

                    var paquetes = request.paquetes().stream()
                            .map(p -> new Despacho.ItemPaquete(p.vehiculoId(), p.pesoKg()))
                            .toList();

                    var comando = new Despacho.Comando(request.clienteId(),
                            request.ciudad(),
                            paquetes,
                            trazaId,
                            idemKey
                    );

                    return crearDespachoUseCase.crear(comando).map(DespachoResponse::desde);
                })
                .map(dr -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(dr)
                );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<DespachoResponse>> obtener(@PathVariable("id") Long id) {
        return consultarDespachoUseCase.obtener(id).map(DespachoResponse::desde)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/confirm")
    public Mono<ResponseEntity<DespachoResponse>> confirmar(@PathVariable("id") Long id) {
        return confirmarDespachoUseCase.confirmar(id).map(DespachoResponse::desde)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<DespachoEvento>> eventos(@PathVariable("id") Long id) {
        Flux<DespachoEvento> stream = eventosDespachoUseCase.eventos(id);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(stream);
    }
}