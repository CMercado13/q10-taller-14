package co.com.taller14.model.despacho.gateways;

import co.com.taller14.model.despacho.Despacho;
import co.com.taller14.model.despacho.EstadoDespacho;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface DespachoRepository {

    Mono<Despacho> crearRecibido(Despacho despacho);

    /**
     * Persiste despacho + paquetes en una única transacción reactiva (TransactionalOperator)
     * y cambia el estado a ASIGNADO con su expiración.
     */
    Mono<Despacho> asignar(Despacho despacho);

    Mono<Despacho> porId(Long id);

    Mono<Despacho> porIdemKey(String idemKey);

    Mono<Despacho> cambiarEstado(Long id, EstadoDespacho nuevoEstado);

    Flux<Despacho> vencidosAntesDe(Instant instante);
}
