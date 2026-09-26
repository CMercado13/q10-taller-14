package co.com.taller14.r2dbc.adapter;

import co.com.taller14.r2dbc.repository.PaqueteR2dbcRepository;
import co.com.taller14.r2dbc.entity.PaqueteEntity;
import co.com.taller14.model.paquete.Paquete;
import co.com.taller14.model.paquete.gateways.PaqueteRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PaqueteRepositoryAdapter implements PaqueteRepository {

    private final PaqueteR2dbcRepository repository;

    public PaqueteRepositoryAdapter(PaqueteR2dbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Paquete> guardar(Paquete paquete) {
        return repository.save(toEntity(paquete)).map(this::toDomain);
    }

    @Override
    public Flux<Paquete> guardarTodos(Flux<Paquete> paquetes) {
        return repository.saveAll(paquetes.map(this::toEntity)).map(this::toDomain);
    }

    @Override
    public Flux<Paquete> porDespacho(Long despachoId) {
        return repository.findByDespachoId(despachoId).map(this::toDomain);
    }

    PaqueteEntity toEntity(Paquete paquete) {
        return new PaqueteEntity(paquete.id(), paquete.despachoId(), paquete.vehiculoId(), paquete.pesoKg());
    }

    Paquete toDomain(PaqueteEntity entity) {
        return new Paquete(entity.getId(), entity.getDespachoId(), entity.getVehiculoId(), entity.getPesoKg());
    }
}
