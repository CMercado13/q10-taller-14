package co.com.taller14.r2dbc.adapter;

import co.com.taller14.r2dbc.repository.DespachoR2dbcRepository;
import co.com.taller14.r2dbc.repository.PaqueteR2dbcRepository;
import co.com.taller14.r2dbc.entity.DespachoEntity;
import co.com.taller14.r2dbc.entity.PaqueteEntity;
import co.com.taller14.model.despacho.Despacho;
import co.com.taller14.model.despacho.EstadoDespacho;
import co.com.taller14.model.despacho.gateways.DespachoRepository;
import co.com.taller14.model.paquete.Paquete;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

/**
 * La transacción reactiva cubre exactamente el guardado de despacho + paquetes + cambio de
 * estado a ASIGNADO (método {@link #asignar}). No envuelve las llamadas a los externos ni la
 * reserva de cupo: una transacción no puede abarcar una llamada HTTP sin bloquear la conexión
 * (ver DECISIONES.md, sección 4).
 */
@Repository
public class DespachoRepositoryAdapter implements DespachoRepository {

    private final DespachoR2dbcRepository despachoRepository;
    private final PaqueteR2dbcRepository paqueteRepository;
    private final TransactionalOperator transactionalOperator;

    public DespachoRepositoryAdapter(DespachoR2dbcRepository despachoRepository,
                                      PaqueteR2dbcRepository paqueteRepository,
                                      TransactionalOperator transactionalOperator) {
        this.despachoRepository = despachoRepository;
        this.paqueteRepository = paqueteRepository;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<Despacho> crearRecibido(Despacho despacho) {
        return despachoRepository.save(toEntity(despacho)).map(entity -> toDomain(entity, List.of()));
    }

    @Override
    public Mono<Despacho> asignar(Despacho despacho) {
        DespachoEntity entity = toEntity(despacho);
        Mono<Despacho> flujo = despachoRepository.save(entity)
                .flatMap(guardado -> guardarPaquetes(despacho.paquetes(), guardado.getId())
                        .collectList()
                        .map(paquetes -> toDomain(guardado, paquetes)));
        return flujo.as(transactionalOperator::transactional);
    }

    private Flux<Paquete> guardarPaquetes(List<Paquete> paquetes, Long despachoId) {
        List<PaqueteEntity> entidades = paquetes.stream()
                .map(p -> new PaqueteEntity(p.id(), despachoId, p.vehiculoId(), p.pesoKg()))
                .toList();
        return paqueteRepository.saveAll(entidades).map(this::toDomain);
    }

    @Override
    public Mono<Despacho> porId(Long id) {
        return despachoRepository.findById(id)
                .flatMap(entity -> paqueteRepository.findByDespachoId(id)
                        .map(this::toDomain)
                        .collectList()
                        .map(paquetes -> toDomain(entity, paquetes)));
    }

    @Override
    public Mono<Despacho> porIdemKey(String idemKey) {
        return despachoRepository.findByIdemKey(idemKey)
                .flatMap(entity -> paqueteRepository.findByDespachoId(entity.getId())
                        .map(this::toDomain)
                        .collectList()
                        .map(paquetes -> toDomain(entity, paquetes)));
    }

    @Override
    public Mono<Despacho> cambiarEstado(Long id, EstadoDespacho nuevoEstado) {
        return despachoRepository.findById(id)
                .flatMap(entity -> {
                    entity.setEstado(nuevoEstado.name());
                    return despachoRepository.save(entity);
                })
                .flatMap(entity -> paqueteRepository.findByDespachoId(id)
                        .map(this::toDomain)
                        .collectList()
                        .map(paquetes -> toDomain(entity, paquetes)));
    }

    @Override
    public Flux<Despacho> vencidosAntesDe(Instant instante) {
        return despachoRepository.findByEstadoAndExpiraEnBefore(EstadoDespacho.ASIGNADO.name(), instante)
                .map(entity -> toDomain(entity, List.of()));
    }

    private DespachoEntity toEntity(Despacho despacho) {
        DespachoEntity entity = new DespachoEntity();
        entity.setId(despacho.id());
        entity.setClienteId(despacho.clienteId());
        entity.setCiudad(despacho.ciudad());
        entity.setEstado(despacho.estado().name());
        entity.setTarifa(despacho.tarifa());
        entity.setTotal(despacho.total());
        entity.setScoreRiesgo(despacho.scoreRiesgo());
        entity.setTrazaId(despacho.trazaId());
        entity.setIdemKey(despacho.idemKey());
        entity.setCreadoEn(despacho.creadoEn() != null ? despacho.creadoEn() : Instant.now());
        entity.setExpiraEn(despacho.expiraEn());
        return entity;
    }

    private Despacho toDomain(DespachoEntity entity, List<Paquete> paquetes) {
        return new Despacho(entity.getId(), entity.getClienteId(), entity.getCiudad(),
                EstadoDespacho.valueOf(entity.getEstado()), entity.getTarifa(), entity.getTotal(),
                entity.getScoreRiesgo(), entity.getTrazaId(), entity.getIdemKey(),
                entity.getCreadoEn(), entity.getExpiraEn(), paquetes);
    }

    private Paquete toDomain(PaqueteEntity entity) {
        return new Paquete(entity.getId(), entity.getDespachoId(), entity.getVehiculoId(), entity.getPesoKg());
    }
}
