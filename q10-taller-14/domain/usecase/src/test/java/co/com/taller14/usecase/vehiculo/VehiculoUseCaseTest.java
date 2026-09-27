package co.com.taller14.usecase.vehiculo;

import co.com.taller14.model.exceptions.ValidacionException;
import co.com.taller14.model.vehiculo.Vehiculo;
import co.com.taller14.model.vehiculo.gateways.VehiculoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VehiculoUseCaseTest {

    @Test
    void rechazaPlacaVaciaYDelegaLaCargaEnLotesDeQuinientos() {
        VehiculoRepository repository = Mockito.mock(VehiculoRepository.class);
        VehiculoUseCase useCase = new VehiculoUseCase(repository);
        Vehiculo vehiculo = new Vehiculo(1L, "ABC123", "BOG", 100, 0);
        when(repository.guardar(vehiculo)).thenReturn(Mono.just(vehiculo));
        when(repository.todos()).thenReturn(Flux.just(vehiculo));
        when(repository.upsertLote(org.mockito.ArgumentMatchers.any(), eq(500))).thenReturn(Mono.just(500L));

        StepVerifier.create(useCase.crear(new Vehiculo(1L, " ", "BOG", 1, 0)))
                .expectError(ValidacionException.class)
                .verify();
        StepVerifier.create(useCase.crear(vehiculo))
                .expectNext(vehiculo)
                .verifyComplete();
        StepVerifier.create(useCase.listar())
                .expectNext(vehiculo)
                .verifyComplete();
        StepVerifier.create(useCase.cargarMasivo(Flux.just(vehiculo)))
                .assertNext(resumen -> {
                    assertEquals(500L, resumen.procesados());
                    assertEquals(1L, resumen.lotes());
                })
                .verifyComplete();

        verify(repository).upsertLote(org.mockito.ArgumentMatchers.any(), anyInt());
    }
}
