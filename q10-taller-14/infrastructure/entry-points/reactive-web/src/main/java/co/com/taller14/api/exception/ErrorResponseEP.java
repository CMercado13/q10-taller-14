package co.com.taller14.api.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponseEP(int codigo,
                              Map<String, String> errores,
                              String trazaId,
                              Instant instante) {
}
