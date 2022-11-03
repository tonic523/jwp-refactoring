package kitchenpos.application;

import java.time.LocalDateTime;
import java.util.List;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderDao orderDao;
    private final OrderTableDao orderTableDao;

    public OrderService(OrderDao orderDao, OrderTableDao orderTableDao) {
        this.orderDao = orderDao;
        this.orderTableDao = orderTableDao;
    }

    @Transactional
    public Order create(Long orderTableId, List<OrderLineItem> orderLineItems) {
        OrderTable orderTable = searchValidOrderTable(orderTableId);
        Order order = new Order(orderTable, OrderStatus.COOKING.name(), LocalDateTime.now(), orderLineItems);
        return orderDao.save(order);
    }

    private OrderTable searchValidOrderTable(Long orderTableId) {
        OrderTable orderTable = orderTableDao.findById(orderTableId)
                .orElseThrow(IllegalArgumentException::new);
        if (orderTable.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return orderTable;
    }

    public List<Order> list() {
        return orderDao.findAll();
    }

    @Transactional
    public Order changeOrderStatus(Long orderId, String orderStatus) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(IllegalArgumentException::new);
        order.updateOrderStatus(OrderStatus.valueOf(orderStatus));
        return order;
    }
}
