package si.pricescout.stores.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import si.pricescout.stores.dto.PriceCreateRequest;
import si.pricescout.stores.dto.PriceUpdateRequest;
import si.pricescout.stores.model.Price;

public interface PriceService {

    Mono<Price> createPrice(PriceCreateRequest request);

    Mono<Price> updatePrice(Long id, PriceUpdateRequest request);

    Flux<Price> getPricesForProduct(Long productId);
}
