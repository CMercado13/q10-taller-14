package co.com.taller14.model.paquete;

/**
 * Paquete individual dentro de un despacho, asignado a un vehículo concreto.
 */
public record Paquete(Long id, Long despachoId, Long vehiculoId, Integer pesoKg) {

    public Paquete {
        if (pesoKg != null && pesoKg <= 0) {
            throw new IllegalArgumentException("pesoKg debe ser mayor a cero");
        }
    }

    public Paquete asignadoA(Long vehiculoId) {
        return new Paquete(id, despachoId, vehiculoId, pesoKg);
    }

    public Paquete conDespacho(Long despachoId) {
        return new Paquete(id, despachoId, vehiculoId, pesoKg);
    }
}
