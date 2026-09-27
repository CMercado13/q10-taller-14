package co.com.taller14.usecase.despacho;

import co.com.taller14.model.despacho.Despacho;
import co.com.taller14.model.despacho.EstadoDespacho;
import co.com.taller14.model.despacho.gateways.DespachoRepository;
import co.com.taller14.model.despacho.gateways.EventPublisherGateway;
import co.com.taller14.model.exceptions.CupoInsuficienteException;
import co.com.taller14.model.exceptions.ValidacionException;
import co.com.taller14.model.exceptions.ZonaRiesgosaException;
import co.com.taller14.model.transportista.Clima;
import co.com.taller14.model.transportista.ScoreRiesgo;
import co.com.taller14.model.transportista.Tarifa;
import co.com.taller14.model.transportista.gateway.ClimaGateway;
import co.com.taller14.model.transportista.gateway.ScoringGateway;
import co.com.taller14.model.transportista.gateway.TarifaGateway;
import co.com.taller14.model.vehiculo.Vehiculo;
import co.com.taller14.model.vehiculo.gateways.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CrearDespachoUseCaseTest {

    private DespachoRepository despachoRepository;
    private VehiculoRepository vehiculoRepository;
    private TarifaGateway tarifaGateway;
    private ClimaGateway climaGateway;
    private ScoringGateway scoringGateway;
    private EventPublisherGateway eventPublisherGateway;
    private CrearDespachoUseCase useCase;

    @BeforeEach
    void setUp() {
        despachoRepository = Mockito.mock(DespachoRepository.class);
        vehiculoRepository = Mockito.mock(VehiculoRepository.class);
        tarifaGateway = Mockito.mock(TarifaGateway.class);
        climaGateway = Mockito.mock(ClimaGateway.class);
        scoringGateway = Mockito.mock(ScoringGateway.class);
        eventPublisherGateway = Mockito.mock(EventPublisherGateway.class);
        useCase = new CrearDespachoUseCase(despachoRepository, vehiculoRepository, tarifaGateway,
                climaGateway, scoringGateway, eventPublisherGateway);
        when(eventPublisherGateway.publicar(any())).thenReturn(Mono.empty());
    }

    @Test
    void rechazaCiudadVacia() {
        StepVerifier.create(useCase.crear(comando("  ", 10)))
                .expectError(ValidacionException.class)
                .verify();

        verify(despachoRepository, never()).crearRecibido(any());
    }

    @Test
    void rechazaDespachoSinPaquetes() {
        Despacho.Comando comando = new Despacho.Comando(1L, "BOG", List.of(), "t", null);

        StepVerifier.create(useCase.crear(comando))
                .expectError(ValidacionException.class)
                .verify();
    }

    @Test
    void rechazaPesoNoPositivo() {
        StepVerifier.create(useCase.crear(comando("BOG", 0)))
                .expectError(ValidacionException.class)
                .verify();
    }

    @Test
    void devuelveElDespachoExistenteCuandoLaLlaveEsIdempotente() {
        Despacho existente = Despacho.nuevo(1L, "BOG", "t", "K1", List.of()).conId(9L);
        when(despachoRepository.porIdemKey("K1")).thenReturn(Mono.just(existente));
        Despacho.Comando comando = new Despacho.Comando(1L, "BOG",
                List.of(new Despacho.ItemPaquete(1L, 10)), "t", "K1");

        StepVerifier.create(useCase.crear(comando))
                .assertNext(despacho -> assertEquals(9L, despacho.id()))
                .verifyComplete();

        verify(despachoRepository, never()).crearRecibido(any());
    }

    @Test
    void asignaCuandoElRiesgoEsBajo() {
        stubExternos(20, new Tarifa("BOG", BigDecimal.TEN, false));
        when(vehiculoRepository.reservarCupo(1L, 100)).thenReturn(Mono.just(vehiculo()));
        when(despachoRepository.crearRecibido(any())).thenAnswer(invocation ->
                Mono.just(((Despacho) invocation.getArgument(0)).conId(4L)));
        when(despachoRepository.asignar(any())).thenAnswer(invocation -> {
            Despacho despacho = invocation.getArgument(0);
            return Mono.just(despacho.conEstado(EstadoDespacho.ASIGNADO));
        });

        StepVerifier.create(useCase.crear(comando("BOG", 100)))
                .assertNext(despacho -> {
                    assertEquals(EstadoDespacho.ASIGNADO, despacho.estado());
                    assertEquals(0, BigDecimal.valueOf(1000).compareTo(despacho.total()));
                })
                .verifyComplete();

        verify(eventPublisherGateway).publicar(any());
    }

    @Test
    void usaTarifaDeCatalogoSiElExternoFalla() {
        when(tarifaGateway.consultar("BOG")).thenReturn(Mono.error(new IllegalStateException("caido")));
        when(climaGateway.consultar("BOG")).thenReturn(Mono.just(new Clima("BOG", "AM", 1)));
        when(scoringGateway.consultar("BOG")).thenReturn(Mono.just(new ScoreRiesgo("BOG", 10)));
        when(vehiculoRepository.reservarCupo(1L, 10)).thenReturn(Mono.just(vehiculo()));
        when(despachoRepository.crearRecibido(any())).thenAnswer(invocation ->
                Mono.just(((Despacho) invocation.getArgument(0)).conId(4L)));
        when(despachoRepository.asignar(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.crear(comando("BOG", 10)))
                .assertNext(despacho -> assertEquals(0, BigDecimal.valueOf(50000).compareTo(despacho.total())))
                .verifyComplete();
    }

    @Test
    void rechazaZonaRiesgosaYLiberaElCupo() {
        stubExternos(95, new Tarifa("BOG", BigDecimal.ONE, false));
        when(vehiculoRepository.reservarCupo(1L, 10)).thenReturn(Mono.just(vehiculo()));
        when(vehiculoRepository.liberarCupo(1L, 10)).thenReturn(Mono.just(vehiculo()));
        when(despachoRepository.crearRecibido(any())).thenAnswer(invocation ->
                Mono.just(((Despacho) invocation.getArgument(0)).conId(4L)));
        when(despachoRepository.cambiarEstado(4L, EstadoDespacho.RECHAZADO))
                .thenReturn(Mono.just(Despacho.nuevo(1L, "BOG", "t", null, List.of()).conId(4L)
                        .conEstado(EstadoDespacho.RECHAZADO)));

        StepVerifier.create(useCase.crear(comando("BOG", 10)))
                .expectError(ZonaRiesgosaException.class)
                .verify();

        verify(vehiculoRepository).liberarCupo(1L, 10);
        verify(despachoRepository).cambiarEstado(4L, EstadoDespacho.RECHAZADO);
    }

    @Test
    void marcaRechazadoSiNoHayCupo() {
        when(vehiculoRepository.reservarCupo(1L, 10)).thenReturn(Mono.empty());
        when(vehiculoRepository.porId(1L)).thenReturn(Mono.empty());
        when(despachoRepository.crearRecibido(any())).thenAnswer(invocation ->
                Mono.just(((Despacho) invocation.getArgument(0)).conId(4L)));
        when(despachoRepository.cambiarEstado(4L, EstadoDespacho.RECHAZADO))
                .thenReturn(Mono.just(Despacho.nuevo(1L, "BOG", "t", null, List.of()).conId(4L)));

        StepVerifier.create(useCase.crear(comando("BOG", 10)))
                .expectError(CupoInsuficienteException.class)
                .verify();

        verify(despachoRepository).cambiarEstado(4L, EstadoDespacho.RECHAZADO);
    }

    @Test
    void lasTresConsultasExternasCorrenEnParalelo() {
        when(tarifaGateway.consultar("BOG")).thenReturn(
                Mono.just(new Tarifa("BOG", BigDecimal.ONE, false)).delayElement(Duration.ofMillis(300)));
        when(climaGateway.consultar("BOG")).thenReturn(
                Mono.just(new Clima("BOG", "AM", 1)).delayElement(Duration.ofMillis(200)));
        when(scoringGateway.consultar("BOG")).thenReturn(
                Mono.just(new ScoreRiesgo("BOG", 10)).delayElement(Duration.ofMillis(100)));
        when(vehiculoRepository.reservarCupo(1L, 10)).thenReturn(Mono.just(vehiculo()));
        when(despachoRepository.crearRecibido(any())).thenAnswer(invocation ->
                Mono.just(((Despacho) invocation.getArgument(0)).conId(4L)));
        when(despachoRepository.asignar(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.withVirtualTime(() -> useCase.crear(comando("BOG", 10)))
                .expectSubscription()
                .thenAwait(Duration.ofMillis(350))
                .expectNextCount(1)
                .verifyComplete();
    }

    private void stubExternos(int score, Tarifa tarifa) {
        when(tarifaGateway.consultar("BOG")).thenReturn(Mono.just(tarifa));
        when(climaGateway.consultar("BOG")).thenReturn(Mono.just(new Clima("BOG", "AM", 1)));
        when(scoringGateway.consultar("BOG")).thenReturn(Mono.just(new ScoreRiesgo("BOG", score)));
    }

    private Despacho.Comando comando(String ciudad, int peso) {
        return new Despacho.Comando(1L, ciudad, List.of(new Despacho.ItemPaquete(1L, peso)), "t", null);
    }

    private Vehiculo vehiculo() {
        return new Vehiculo(1L, "ABC123", "BOG", 500, 0);
    }
}
