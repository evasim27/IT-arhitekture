package si.pricescout.stores.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import si.pricescout.stores.dto.StoreRequest;
import si.pricescout.stores.model.Store;

public interface StoreService {

    Mono<Store> createStore(StoreRequest request);

    Flux<Store> getAllStores();
}
