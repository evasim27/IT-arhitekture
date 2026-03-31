package si.pricescout.stores.service.impl;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import si.pricescout.stores.dto.PriceCreateRequest;
import si.pricescout.stores.dto.PriceUpdateRequest;
import si.pricescout.stores.exception.ConflictException;
import si.pricescout.stores.exception.ResourceNotFoundException;
import si.pricescout.stores.messaging.PriceChangedEvent;
import si.pricescout.stores.messaging.PriceEventPublisher;
import si.pricescout.stores.messaging.PriceEventType;
import si.pricescout.stores.model.Price;
import si.pricescout.stores.repository.PriceRepository;
import si.pricescout.stores.repository.StoreRepository;
import si.pricescout.stores.service.PriceService;

@Service
public class PriceServiceImpl implements PriceService {

    private static final Logger log = LoggerFactory.getLogger(PriceServiceImpl.class);

    private final PriceRepository priceRepository;
    private final StoreRepository storeRepository;
    private final PriceEventPublisher eventPublisher;

    public PriceServiceImpl(PriceRepository priceRepository,
                            StoreRepository storeRepository,
                            PriceEventPublisher eventPublisher) {
        this.priceRepository = priceRepository;
        this.storeRepository = storeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<Price> createPrice(PriceCreateRequest request) {
        validatePositive(request.getValue());

        return storeRepository.existsById(request.getStoreId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResourceNotFoundException("Store not found: " + request.getStoreId()));
                    }

                    Price price = new Price(null, request.getProductId(), request.getStoreId(), request.getValue());
                    return priceRepository.save(price)
                            .doOnSuccess(saved -> log.info("Created price id={} productId={} storeId={} value={}",
                                    saved.getId(), saved.getProductId(), saved.getStoreId(), saved.getValue()))
                            .flatMap(saved -> eventPublisher.publishPriceChanged(new PriceChangedEvent(
                                            PriceEventType.PRICE_CREATED,
                                            saved.getProductId(),
                                            saved.getStoreId(),
                                            saved.getValue()
                                    ))
                                    .thenReturn(saved))
                            .onErrorMap(DataIntegrityViolationException.class,
                                    ex -> new ConflictException("Price for product and store already exists"));
                });
    }

    @Override
    public Mono<Price> updatePrice(Long id, PriceUpdateRequest request) {
        validatePositive(request.getValue());

        return priceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Price not found: " + id)))
                .flatMap(existing -> {
                    existing.setValue(request.getValue());
                    return priceRepository.save(existing)
                            .doOnSuccess(saved -> log.info("Updated price id={} productId={} storeId={} value={}",
                                    saved.getId(), saved.getProductId(), saved.getStoreId(), saved.getValue()))
                            .flatMap(saved -> eventPublisher.publishPriceChanged(new PriceChangedEvent(
                                            PriceEventType.PRICE_UPDATED,
                                            saved.getProductId(),
                                            saved.getStoreId(),
                                            saved.getValue()
                                    ))
                                    .thenReturn(saved));
                });
    }

    @Override
    public Flux<Price> getPricesForProduct(Long productId) {
        return priceRepository.findAllByProductId(productId);
    }

    private void validatePositive(BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException("Price value must be positive");
        }
    }
}
