package si.pricescout.stores.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import si.pricescout.stores.dto.StoreRequest;
import si.pricescout.stores.exception.ConflictException;
import si.pricescout.stores.model.Store;
import si.pricescout.stores.repository.StoreRepository;
import si.pricescout.stores.service.StoreService;

@Service
public class StoreServiceImpl implements StoreService {

    private static final Logger log = LoggerFactory.getLogger(StoreServiceImpl.class);

    private final StoreRepository storeRepository;

    public StoreServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public Mono<Store> createStore(StoreRequest request) {
        Store store = new Store(null, request.getName().trim());
        return storeRepository.save(store)
                .doOnSuccess(saved -> log.info("Created store id={} name={}", saved.getId(), saved.getName()))
                .onErrorMap(DataIntegrityViolationException.class,
                        ex -> new ConflictException("Store with this name already exists"));
    }

    @Override
    public Flux<Store> getAllStores() {
        return storeRepository.findAll();
    }
}
