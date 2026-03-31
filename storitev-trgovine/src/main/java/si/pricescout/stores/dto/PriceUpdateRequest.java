package si.pricescout.stores.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PriceUpdateRequest {

    @NotNull(message = "value is required")
    @Positive(message = "Price value must be positive")
    private BigDecimal value;

    public PriceUpdateRequest() {
    }

    public PriceUpdateRequest(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
