package si.pricescout.stores.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import si.pricescout.stores.dto.PriceCreateRequest;
import si.pricescout.stores.dto.PriceUpdateRequest;
import si.pricescout.stores.model.Price;
import si.pricescout.stores.service.PriceService;

@RestController
@RequestMapping("/prices")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Price> createPrice(@Valid @RequestBody PriceCreateRequest request) {
        return priceService.createPrice(request);
    }

    @PutMapping("/{id}")
    public Mono<Price> updatePrice(@PathVariable Long id, @Valid @RequestBody PriceUpdateRequest request) {
        return priceService.updatePrice(id, request);
    }

    @GetMapping("/product/{productId}")
    public Flux<Price> getPricesForProduct(@PathVariable Long productId) {
        return priceService.getPricesForProduct(productId);
    }
}
