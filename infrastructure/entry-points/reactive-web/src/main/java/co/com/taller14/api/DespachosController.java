package co.com.taller14.api;

import co.com.taller14.model.despacho.Despacho;
import co.com.taller14.model.despacho.EventoDespacho;
import co.com.taller14.usecase.despacho.DespachoUseCase;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/despachos")
public class DespachosController {

    private final DespachoUseCase despachoUseCase;

    public DespachosController(DespachoUseCase despachoUseCase) {
        this.despachoUseCase = despachoUseCase;
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Despacho>> buscarPorId(@PathVariable("id") Long id) {
        return despachoUseCase.obtener(id)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/confirm")
    public Mono<ResponseEntity<Despacho>> confirmarPorId(@PathVariable("id") Long id) {
        return despachoUseCase.confirmar(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<EventoDespacho>> eventosPorId(@PathVariable Long id) {
        return despachoUseCase.eventos(id)
                .map(e -> ServerSentEvent.builder(e)
                        .event(e.estado() == null ? "heartbeat" : "order")
                        .build());
    }

}
