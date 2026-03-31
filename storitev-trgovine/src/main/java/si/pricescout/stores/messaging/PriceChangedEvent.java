package si.pricescout.stores.messaging;

import java.math.BigDecimal;

public record PriceChangedEvent(
        PriceEventType eventType,
        Long productId,
        Long storeId,
        BigDecimal value
) {
}
