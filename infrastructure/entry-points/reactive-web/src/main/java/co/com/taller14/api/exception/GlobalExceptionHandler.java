package co.com.taller14.api.exception;

import co.com.taller14.model.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponseEP>> handleValidationException(WebExchangeBindException ex) {
        return Mono.deferContextual(cxt -> {
                    String trazaId = cxt.getOrDefault("traceId", "Not trace");
                    Map<String, String> errors = ex.getFieldErrors().stream()
                            .collect(Collectors.toMap(
                                    FieldError::getField,
                                    err -> err.getDefaultMessage(),
                                    (msg1, msg2) -> msg1
                            ));
                    return Mono.just(
                            new ErrorResponseEP(
                                    HttpStatus.BAD_REQUEST.value(),
                                    errors,
                                    trazaId,
                                    Instant.now()
                            )
                    );
                })
                .map(error -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(VehiculoNoExisteException.class)
    public Mono<ResponseEntity<ErrorResponseEP>> handleValidationException(VehiculoNoExisteException ex) {
        return Mono.deferContextual(cxt -> {
                    String trazaId = cxt.getOrDefault("traceId", "Not trace");
                    return Mono.just(
                            new ErrorResponseEP(
                                    HttpStatus.NOT_FOUND.value(),
                                    Map.of("error", ex.getMessage()),
                                    trazaId,
                                    Instant.now()
                            )
                    );
                })
                .map(error -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(CupoInsuficienteException.class)
    public Mono<ResponseEntity<ErrorResponseEP>> handleValidationException(CupoInsuficienteException ex) {
        return Mono.deferContextual(cxt -> {
                    String trazaId = cxt.getOrDefault("traceId", "No trace");
                    return Mono.just(
                            new ErrorResponseEP(
                                    HttpStatus.CONFLICT.value(),
                                    Map.of("error", ex.getMessage()),
                                    trazaId,
                                    Instant.now()
                            )
                    );
                })
                .map(error -> ResponseEntity.status(HttpStatus.CONFLICT).body(error));
    }

    @ExceptionHandler(DespachoNoExisteException.class)
    public Mono<ResponseEntity<ErrorResponseEP>> handleValidationException(DespachoNoExisteException ex) {
        return Mono.deferContextual(cxt -> {
                    String trazaId = cxt.getOrDefault("traceId", "No trace");
                    return Mono.just(
                            new ErrorResponseEP(
                                    HttpStatus.NOT_FOUND.value(),
                                    Map.of("error", ex.getMessage()),
                                    trazaId,
                                    Instant.now()
                            )
                    );
                })
                .map(error -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(EstadoInvalidoException.class)
    public Mono<ResponseEntity<ErrorResponseEP>> handleValidationException(EstadoInvalidoException ex) {
        return Mono.deferContextual(cxt -> {
                    String trazaId = cxt.getOrDefault("traceId", "No trace");
                    return Mono.just(
                            new ErrorResponseEP(
                                    HttpStatus.CONFLICT.value(),
                                    Map.of("error", ex.getMessage()),
                                    trazaId,
                                    Instant.now()
                            )
                    );
                })
                .map(error -> ResponseEntity.status(HttpStatus.CONFLICT).body(error));
    }

    @ExceptionHandler(ValidacionException.class)
    public Mono<ResponseEntity<ErrorResponseEP>> handleValidationException(ValidacionException ex) {
        return Mono.deferContextual(cxt -> {
                    String trazaId = cxt.getOrDefault("traceId", "No trace");
                    return Mono.just(
                            new ErrorResponseEP(
                                    HttpStatus.BAD_REQUEST.value(),
                                    Map.of("error", ex.getMessage()),
                                    trazaId,
                                    Instant.now()
                            )
                    );
                })
                .map(error -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(ZonaRiesgosaException.class)
    public Mono<ResponseEntity<ErrorResponseEP>> handleValidationException(ZonaRiesgosaException ex) {
        return Mono.deferContextual(cxt -> {
                    String trazaId = cxt.getOrDefault("traceId", "No trace");
                    return Mono.just(
                            new ErrorResponseEP(
                                    HttpStatus.UNPROCESSABLE_CONTENT.value(),
                                    Map.of("error", ex.getMessage()),
                                    trazaId,
                                    Instant.now()
                            )
                    );
                })
                .map(error -> ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(error));
    }

}
