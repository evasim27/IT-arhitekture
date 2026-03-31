package si.pricescout.stores.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import si.pricescout.stores.model.Price;

public interface PriceRepository extends ReactiveCrudRepository<Price, Long> {

    Flux<Price> findAllByProductId(Long productId);
}
