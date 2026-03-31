package si.pricescout.stores.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import si.pricescout.stores.dto.StoreRequest;
import si.pricescout.stores.model.Store;
import si.pricescout.stores.service.StoreService;

@RestController
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Store> createStore(@Valid @RequestBody StoreRequest request) {
        return storeService.createStore(request);
    }

    @GetMapping
    public Flux<Store> getAllStores() {
        return storeService.getAllStores();
    }
}
