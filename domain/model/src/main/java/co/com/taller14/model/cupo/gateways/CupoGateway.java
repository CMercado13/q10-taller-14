package co.com.taller14.model.cupo.gateways;

import reactor.core.publisher.Mono;

/**
 * Puerto para la reserva atómica de cupo en vehículos (UPDATE ... RETURNING).
 * La compensación (saga) se apoya en {@link #liberar(Long, Integer)}.
 */
public interface CupoGateway {

    /**
     * Intenta reservar peso en el vehículo indicado de forma atómica.
     *
     * @return el cupo restante si tuvo éxito, o Mono.empty() si no había cupo suficiente.
     */
    Mono<Integer> reservar(Long vehiculoId, Integer pesoKg);

    /**
     * Devuelve cupo reservado previamente (compensación de saga o expiración).
     */
    Mono<Void> liberar(Long vehiculoId, Integer pesoKg);
}
