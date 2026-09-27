package com.logistica.despachos.infrastructure.jobs;

import com.logistica.despachos.domain.port.in.ExpirarDespachosUseCase;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class ExpiracionJob {

    private static final Logger log = LoggerFactory.getLogger(ExpiracionJob.class);
    private final ExpirarDespachosUseCase expirarUseCase;

    public ExpiracionJob(ExpirarDespachosUseCase expirarUseCase) {
        this.expirarUseCase = expirarUseCase;
    }

    @PostConstruct
    public void iniciar() {
        // Flux.interval ya corre en Schedulers.parallel() por defecto (no bloqueante).
        // No se necesita publishOn: toda la cadena aguas abajo es R2DBC reactivo puro.
        Flux.interval(Duration.ofSeconds(30))
                .concatMap(tick -> expirarUseCase.expirarVencidos()
                        .doOnNext(n -> {
                            if (n > 0) log.info("Job de expiración: {} despachos expirados", n);
                        })
                        .onErrorResume(ex -> {
                            log.error("Error en job de expiración", ex);
                            return Mono.just(0L);
                        }))
                .subscribe();
    }
}