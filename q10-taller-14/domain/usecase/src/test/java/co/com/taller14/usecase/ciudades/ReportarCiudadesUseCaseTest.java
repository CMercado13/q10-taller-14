package co.com.taller14.usecase.ciudades;

import co.com.taller14.model.reporte.ReporteCiudad;
import co.com.taller14.model.reporte.gateway.ReporteRepositoryGateway;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ReportarCiudadesUseCaseTest {

    @Test
    void emiteTotalesYAcumuladoEnVivo() {
        ReporteRepositoryGateway gateway = Mockito.mock(ReporteRepositoryGateway.class);
        ReporteCiudad primero = new ReporteCiudad("BOG", 10, BigDecimal.TEN, 1);
        ReporteCiudad segundo = new ReporteCiudad("BOG", 5, BigDecimal.ONE, 1);
        when(gateway.agregarPorCiudad()).thenReturn(Flux.just(primero, segundo));
        ReportarCiudadesUseCase useCase = new ReportarCiudadesUseCase(gateway);

        StepVerifier.create(useCase.reporteTotal())
                .expectNext(primero, segundo)
                .verifyComplete();

        StepVerifier.create(useCase.reporteEnVivo())
                .assertNext(reporte -> assertEquals(10, reporte.totalKg()))
                .assertNext(reporte -> assertEquals(15, reporte.totalKg()))
                .verifyComplete();
    }
}
