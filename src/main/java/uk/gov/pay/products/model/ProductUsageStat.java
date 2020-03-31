package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductUsageStat {
    
    @JsonProperty
    private final Long paymentCount;
    @JsonProperty
    private final ZonedDateTime lastPaymentDate;
    @JsonProperty
    private final ProductEntity product;

    public ProductUsageStat(Long paymentCount, ZonedDateTime lastPaymentDate, ProductEntity product) {
        this.paymentCount = paymentCount;
        this.lastPaymentDate = lastPaymentDate;
        this.product = product;
    }

    public Long getPaymentCount() {
        return paymentCount;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public ZonedDateTime getLastPaymentDate() {
        return lastPaymentDate;
    }

    @Override
    public String toString() {
        return "ProductUsageStat{" +
                "paymentCount=" + paymentCount +
                ", lastPaymentDate=" + lastPaymentDate +
                ", product=" + product +
                '}';
    }
}
