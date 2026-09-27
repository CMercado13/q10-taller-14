package co.com.taller14.usecase.despacho;

import co.com.taller14.model.despacho.Despacho;
import co.com.taller14.model.despacho.DespachoEvento;
import co.com.taller14.model.despacho.EstadoDespacho;
import co.com.taller14.model.despacho.gateways.DespachoRepository;
import co.com.taller14.model.despacho.gateways.EventPublisherGateway;
import co.com.taller14.model.exceptions.DespachoNoExisteException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class ConsultarYEventosUseCaseTest {

    @Test
    void consultarFallaSiNoExiste() {
        DespachoRepository repository = Mockito.mock(DespachoRepository.class);
        when(repository.porId(4L)).thenReturn(Mono.empty());

        StepVerifier.create(new ConsultarDespachoUseCase(repository).obtener(4L))
                .expectError(DespachoNoExisteException.class)
                .verify();
    }

    @Test
    void elStreamCierraEnEstadoTerminalYEmiteHeartbeat() {
        DespachoRepository repository = Mockito.mock(DespachoRepository.class);
        EventPublisherGateway publisher = Mockito.mock(EventPublisherGateway.class);
        Despacho rechazado = Despacho.nuevo(1L, "BOG", "t", null, List.of())
                .conId(8L).conEstado(EstadoDespacho.RECHAZADO);
        Despacho asignado = rechazado.conEstado(EstadoDespacho.ASIGNADO).conId(9L);
        when(repository.porId(8L)).thenReturn(Mono.just(rechazado));
        when(repository.porId(9L)).thenReturn(Mono.just(asignado));
        when(publisher.eventosOrden()).thenReturn(Flux.never());
        EventosDespachoUseCase useCase = new EventosDespachoUseCase(repository, publisher);

        StepVerifier.create(useCase.eventos(8L))
                .assertNext(evento -> assertEquals(EstadoDespacho.RECHAZADO, evento.estado()))
                .verifyComplete();

        StepVerifier.withVirtualTime(() -> useCase.eventos(9L))
                .assertNext(evento -> assertEquals(EstadoDespacho.ASIGNADO, evento.estado()))
                .thenAwait(Duration.ofSeconds(15))
                .assertNext(evento -> assertNull(evento.estado()))
                .thenCancel()
                .verify();
    }

    @Test
    void elTableroReutilizaLaFuenteGlobal() {
        EventPublisherGateway publisher = Mockito.mock(EventPublisherGateway.class);
        DespachoEvento evento = DespachoEvento.heartbeat(1L);
        when(publisher.eventosOrden()).thenReturn(Flux.just(evento));

        StepVerifier.create(new EventosDespachoUseCase(Mockito.mock(DespachoRepository.class), publisher).suscribirGlobal())
                .expectNext(evento)
                .verifyComplete();
    }
}
