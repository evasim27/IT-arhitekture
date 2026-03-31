package si.pricescout.stores.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import si.pricescout.stores.dto.PriceCreateRequest;
import si.pricescout.stores.dto.PriceUpdateRequest;
import si.pricescout.stores.dto.StoreRequest;
import si.pricescout.stores.exception.ResourceNotFoundException;
import si.pricescout.stores.messaging.PriceEventPublisher;
import si.pricescout.stores.model.Price;
import si.pricescout.stores.model.Store;
import si.pricescout.stores.repository.PriceRepository;
import si.pricescout.stores.repository.StoreRepository;
import si.pricescout.stores.service.impl.PriceServiceImpl;
import si.pricescout.stores.service.impl.StoreServiceImpl;

@ExtendWith(MockitoExtension.class)
class StoreAndPriceServiceTest {

    @Mock
    private PriceRepository priceRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PriceEventPublisher eventPublisher;

    private PriceServiceImpl priceService;
    private StoreServiceImpl storeService;

    @BeforeEach
    void setUp() {
        priceService = new PriceServiceImpl(priceRepository, storeRepository, eventPublisher);
        storeService = new StoreServiceImpl(storeRepository);
    }

    @Test
    void createPriceShouldSaveAndPublishEvent() {
        PriceCreateRequest request = new PriceCreateRequest(100L, 1L, BigDecimal.valueOf(4.99));
        Price saved = new Price(5L, 100L, 1L, BigDecimal.valueOf(4.99));

        when(storeRepository.existsById(1L)).thenReturn(Mono.just(true));
        when(priceRepository.save(any(Price.class))).thenReturn(Mono.just(saved));
        when(eventPublisher.publishPriceChanged(any())).thenReturn(Mono.empty());

        StepVerifier.create(priceService.createPrice(request))
                .expectNextMatches(price -> price.getId().equals(5L)
                        && price.getValue().compareTo(BigDecimal.valueOf(4.99)) == 0)
                .verifyComplete();
    }

    @Test
    void createPriceShouldFailWhenStoreDoesNotExist() {
        PriceCreateRequest request = new PriceCreateRequest(100L, 55L, BigDecimal.valueOf(7.19));
        when(storeRepository.existsById(55L)).thenReturn(Mono.just(false));

        StepVerifier.create(priceService.createPrice(request))
                .expectErrorMatches(error -> error instanceof ResourceNotFoundException
                        && error.getMessage().contains("Store not found"))
                .verify();
    }

    @Test
    void updatePriceShouldFailWhenPriceDoesNotExist() {
        when(priceRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(priceService.updatePrice(999L, new PriceUpdateRequest(BigDecimal.TEN)))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void updatePriceShouldFailForNegativeValue() {
        assertThrows(IllegalArgumentException.class,
                () -> priceService.updatePrice(1L, new PriceUpdateRequest(BigDecimal.valueOf(-1))));
    }

    @Test
    void getPricesForProductShouldReturnEmptyFlux() {
        when(priceRepository.findAllByProductId(777L)).thenReturn(Flux.empty());

        StepVerifier.create(priceService.getPricesForProduct(777L))
                .verifyComplete();
    }

    @Test
    void createStoreShouldTrimAndPersistName() {
        StoreRequest request = new StoreRequest("  Lidl  ");
        when(storeRepository.save(any(Store.class))).thenReturn(Mono.just(new Store(1L, "Lidl")));

        StepVerifier.create(storeService.createStore(request))
                .expectNextMatches(store -> store.getId().equals(1L) && "Lidl".equals(store.getName()))
                .verifyComplete();
    }

    @Test
    void getAllStoresShouldReturnEmptyFluxWhenNoStoresExist() {
        when(storeRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(storeService.getAllStores())
                .verifyComplete();
    }
}
