package kitchenpos.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class OrderTableGroup {

    private Long id;
    private LocalDateTime createdDate;
    private List<OrderTable> orderTables;

    public OrderTableGroup() {
    }

    public OrderTableGroup(LocalDateTime createdDate, List<OrderTable> orderTables) {
        if (orderTables.size() < 2) {
            throw new IllegalArgumentException();
        }
        if (orderTables.stream()
                .map(OrderTable::getId)
                .filter(Objects::nonNull)
                .distinct()
                .count() != orderTables.size()) {
            throw new IllegalArgumentException();
        }
        this.createdDate = createdDate;
        this.orderTables = orderTables;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public List<OrderTable> getOrderTables() {
        return orderTables;
    }

    public void setOrderTables(final List<OrderTable> orderTables) {
        this.orderTables = orderTables;
    }
}