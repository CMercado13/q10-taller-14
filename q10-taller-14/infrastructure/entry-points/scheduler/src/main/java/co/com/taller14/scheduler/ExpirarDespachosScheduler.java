package co.com.taller14.scheduler;

import co.com.taller14.usecase.despacho.ExpirarDespachosUseCase;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpirarDespachosScheduler {

    private final ExpirarDespachosUseCase expirarDespachosUseCase;

    @PostConstruct
    public void iniciar() {
        log.info("::scheduler init");
        // Flux.interval ya corre en Schedulers.parallel() por defecto (no bloqueante).
        // No se necesita publishOn: toda la cadena aguas abajo es R2DBC reactivo puro.
        Flux.interval(Duration.ofSeconds(30))
                .concatMap(tick -> expirarDespachosUseCase.expirarVencidos()
                        .doOnNext(n -> {
                            if (n > 0) {
                                log.info("Job de expiración: {} despachos expirados", n);
                            }
                        })
                        .onErrorResume(ex -> {
                            log.error("Error en job de expiración error: {}", ex.getMessage(), ex);
                            return Mono.just(0L);
                        }))
                .subscribe();
    }
}
