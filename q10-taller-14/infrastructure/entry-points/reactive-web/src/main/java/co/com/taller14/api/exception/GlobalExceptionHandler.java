package co.com.taller14.api.exception;

import co.com.taller14.api.dto.ErrorResponse;
import co.com.taller14.api.support.TrazaIdSupport;
import co.com.taller14.model.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(VehiculoNoExisteException.class)
    public Mono<ResponseEntity<ErrorResponse>> handle(VehiculoNoExisteException ex) {
        return construir(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CupoInsuficienteException.class)
    public Mono<ResponseEntity<ErrorResponse>> handle(CupoInsuficienteException ex) {
        return construir(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ZonaRiesgosaException.class)
    public Mono<ResponseEntity<ErrorResponse>> handle(ZonaRiesgosaException ex) {
        return construir(ex, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ExceptionHandler(DespachoNoExisteException.class)
    public Mono<ResponseEntity<ErrorResponse>> handle(DespachoNoExisteException ex) {
        return construir(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EstadoInvalidoException.class)
    public Mono<ResponseEntity<ErrorResponse>> handle(EstadoInvalidoException ex) {
        return construir(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ValidacionException.class)
    public Mono<ResponseEntity<ErrorResponse>> handle(ValidacionException ex) {
        return construir(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handle(WebExchangeBindException ex) {
        String mensaje = ex.getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Error de validación");

        return TrazaIdSupport.actual().map(trazaId ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.de("VALIDACION", mensaje, trazaId)));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenerico(Exception ex) {
        log.error("::handleGenerico error: {}", ex.getMessage(), ex);
        return TrazaIdSupport.actual().map(trazaId ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ErrorResponse.de("ERROR_INTERNO", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), trazaId)));
    }

    private Mono<ResponseEntity<ErrorResponse>> construir(DomainException ex, HttpStatus status) {
        return TrazaIdSupport.actual().map(trazaId ->
                ResponseEntity.status(status)
                        .body(ErrorResponse.de(ex.codigo(), ex.getMessage(), trazaId)));
    }

}
