package co.com.taller14.api;

import co.com.taller14.model.despacho.DespachoEvento;
import co.com.taller14.usecase.despacho.EventosDespachoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ops/tablero")
@RequiredArgsConstructor
public class TableroController {

    private final EventosDespachoUseCase eventosDespachoUseCase;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DespachoEvento> tablero() {
        return eventosDespachoUseCase.suscribirGlobal();
    }
}