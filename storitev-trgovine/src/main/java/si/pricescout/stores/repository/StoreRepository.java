package si.pricescout.stores.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import si.pricescout.stores.model.Store;

public interface StoreRepository extends ReactiveCrudRepository<Store, Long> {

    Mono<Store> findByName(String name);
}
