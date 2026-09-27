package co.com.taller14.usecase.despacho;

import co.com.taller14.model.despacho.Despacho;
import co.com.taller14.model.despacho.EstadoDespacho;
import co.com.taller14.model.despacho.gateways.DespachoRepository;
import co.com.taller14.model.despacho.gateways.DespachoTransactionalGateway;
import co.com.taller14.model.despacho.gateways.EventPublisherGateway;
import co.com.taller14.model.exceptions.DespachoNoExisteException;
import co.com.taller14.model.exceptions.EstadoInvalidoException;
import co.com.taller14.model.paquete.Paquete;
import co.com.taller14.model.vehiculo.Vehiculo;
import co.com.taller14.model.vehiculo.gateways.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfirmarDespachoUseCaseTest {

    private DespachoRepository despachoRepository;
    private VehiculoRepository vehiculoRepository;
    private EventPublisherGateway eventPublisherGateway;
    private ConfirmarDespachoUseCase useCase;

    @BeforeEach
    void setUp() {
        despachoRepository = Mockito.mock(DespachoRepository.class);
        vehiculoRepository = Mockito.mock(VehiculoRepository.class);
        DespachoTransactionalGateway transactionalGateway = Mockito.mock(DespachoTransactionalGateway.class);
        eventPublisherGateway = Mockito.mock(EventPublisherGateway.class);
        when(transactionalGateway.executeOperationTransactional(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventPublisherGateway.publicar(any())).thenReturn(Mono.empty());
        useCase = new ConfirmarDespachoUseCase(despachoRepository, vehiculoRepository,
                transactionalGateway, eventPublisherGateway);
    }

    @Test
    void fallaSiElDespachoNoExiste() {
        when(despachoRepository.porId(9L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.confirmar(9L))
                .expectError(DespachoNoExisteException.class)
                .verify();
    }

    @Test
    void fallaSiNoEstaAsignado() {
        Despacho recibido = Despacho.nuevo(1L, "BOG", "t", null, List.of()).conId(3L);
        when(despachoRepository.porId(3L)).thenReturn(Mono.just(recibido));

        StepVerifier.create(useCase.confirmar(3L))
                .expectError(EstadoInvalidoException.class)
                .verify();
    }

    @Test
    void consumeLaReservaYPasaAEnRuta() {
        Despacho asignado = Despacho.nuevo(1L, "BOG", "t", null,
                List.of(new Paquete(1L, 3L, 7L, 20))).conId(3L).conEstado(EstadoDespacho.ASIGNADO);
        when(despachoRepository.porId(3L)).thenReturn(Mono.just(asignado));
        when(vehiculoRepository.consumirReservado(7L, 20)).thenReturn(Mono.just(new Vehiculo(7L, "ABC", "BOG", 100, 0)));
        when(despachoRepository.cambiarEstado(3L, EstadoDespacho.EN_RUTA))
                .thenReturn(Mono.just(asignado.conEstado(EstadoDespacho.EN_RUTA)));

        StepVerifier.create(useCase.confirmar(3L))
                .assertNext(despacho -> assertEquals(EstadoDespacho.EN_RUTA, despacho.estado()))
                .verifyComplete();

        verify(vehiculoRepository).consumirReservado(7L, 20);
        verify(eventPublisherGateway).publicar(any());
    }
}
