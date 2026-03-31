package si.pricescout.stores.model;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("prices")
public class Price {

    @Id
    private Long id;

    @Column("product_id")
    private Long productId;

    @Column("store_id")
    private Long storeId;

    private BigDecimal value;

    public Price() {
    }

    public Price(Long id, Long productId, Long storeId, BigDecimal value) {
        this.id = id;
        this.productId = productId;
        this.storeId = storeId;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
