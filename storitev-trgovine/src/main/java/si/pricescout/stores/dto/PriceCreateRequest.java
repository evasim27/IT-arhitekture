package si.pricescout.stores.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PriceCreateRequest {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "storeId is required")
    private Long storeId;

    @NotNull(message = "value is required")
    @Positive(message = "Price value must be positive")
    private BigDecimal value;

    public PriceCreateRequest() {
    }

    public PriceCreateRequest(Long productId, Long storeId, BigDecimal value) {
        this.productId = productId;
        this.storeId = storeId;
        this.value = value;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
