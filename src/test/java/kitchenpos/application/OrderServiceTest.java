package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import kitchenpos.dao.MenuDao;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderLineItemDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.Product;
import kitchenpos.fakedao.MenuFakeDao;
import kitchenpos.fakedao.MenuGroupFakeDao;
import kitchenpos.fakedao.OrderFakeDao;
import kitchenpos.fakedao.OrderLineItemFakeDao;
import kitchenpos.fakedao.OrderTableFakeDao;
import kitchenpos.fakedao.ProductFakeDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class OrderServiceTest {

    private MenuDao menuDao = new MenuFakeDao();
    private MenuGroupDao menuGroupDao = new MenuGroupFakeDao();
    private OrderDao orderDao = new OrderFakeDao();
    private OrderLineItemDao orderLineItemDao = new OrderLineItemFakeDao();
    private OrderTableDao orderTableDao = new OrderTableFakeDao();
    private ProductDao productDao = new ProductFakeDao();

    private OrderService orderService = new OrderService(orderDao, orderTableDao);

    @DisplayName("주문을 생성할 때")
    @Nested
    class Create {

        @DisplayName("성공")
        @Test
        void success() {
            // given
            OrderTable orderTable = orderTableDao.save(new OrderTable(null, 2, false));
            MenuGroup menuGroup = menuGroupDao.save(new MenuGroup("메뉴그룹1"));
            Product product = productDao.save(new Product("상품", BigDecimal.valueOf(1000)));
            MenuProduct menuProduct = new MenuProduct(null, product.getId(), 1, BigDecimal.valueOf(1000));
            Menu menu = menuDao.save(new Menu("메뉴1", BigDecimal.valueOf(1000), menuGroup.getId(),
                    List.of(menuProduct)));
            List<OrderLineItem> orderLineItems = new ArrayList<>();
            orderLineItems.add(orderLineItemDao.save(new OrderLineItem(menu.getId(), 3)));
            // when
            Order order = orderService.create(orderTable.getId(), orderLineItems);

            // then
            assertAll(
                    () -> assertThat(orderDao.findById(order.getId())).isPresent(),
                    () -> assertThat(orderTableDao.findById(order.getOrderTableId())).isPresent(),
                    () -> assertThat(menuDao.findById(menu.getId())).isPresent()
            );
        }

        @DisplayName("주문에 속하는 메뉴가 없으면 예외를 발생시킨다.")
        @Test
        void notFoundOrderLineItem_exception() {
            // given
            OrderTable orderTable = orderTableDao.save(new OrderTable(null, 2, false));

            // then
            assertThatThrownBy(() -> orderService.create(orderTable.getId(), new ArrayList<>()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("주문 테이블이 없으면 예외를 발생시킨다.")
        @Test
        void notFoundOrderTable_exception() {
            // given
            ArrayList<OrderLineItem> orderLineItems = new ArrayList<>();
            orderLineItems.add(orderLineItemDao.save(new OrderLineItem(0L, 3)));

            // then
            assertThatThrownBy(() -> orderService.create(0L, orderLineItems))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @DisplayName("모든 주문을 조회한다.")
    @Test
    void list() {
        // given
        OrderTable orderTable = orderTableDao.save(new OrderTable(null, 2, false));
        MenuGroup menuGroup = menuGroupDao.save(new MenuGroup("메뉴그룹1"));
        Menu menu = menuDao.save(new Menu("메뉴1", BigDecimal.valueOf(1000), menuGroup.getId(),
                List.of(new MenuProduct(null, 3L, 3, BigDecimal.valueOf(1000)))));
        ArrayList<OrderLineItem> orderLineItems = new ArrayList<>();
        orderLineItems.add(orderLineItemDao.save(new OrderLineItem(menu.getId(), 3)));
        orderDao.save(new Order(orderTable.getId(), OrderStatus.MEAL.name(), LocalDateTime.now(), orderLineItems));
        orderDao.save(new Order(orderTable.getId(), OrderStatus.MEAL.name(), LocalDateTime.now(), orderLineItems));

        // when
        List<Order> actual = orderService.list();

        // then
        assertThat(actual).hasSize(2);
    }

    @DisplayName("주문 상태를 변경한다.")
    @Nested
    class ChangeOrderStatus {

        private OrderTable orderTable;
        private Menu menu;
        private List<OrderLineItem> orderLineItems;

        @BeforeEach
        void setUp() {
            orderTable = orderTableDao.save(new OrderTable(null, 2, false));
            MenuGroup menuGroup = menuGroupDao.save(new MenuGroup("메뉴그룹1"));
            Product product = productDao.save(new Product("상품", BigDecimal.valueOf(1000)));
            MenuProduct menuProduct = new MenuProduct(null, product.getId(), 1, BigDecimal.valueOf(1000));
            menu = menuDao.save(new Menu("메뉴1", BigDecimal.valueOf(1000), menuGroup.getId(),
                    List.of(menuProduct)));
            orderLineItems = new ArrayList<>();
            orderLineItems.add(orderLineItemDao.save(new OrderLineItem(menu.getId(), 3)));
        }

        @DisplayName("성공")
        @Test
        void success() {
            // given
            Order order = orderDao.save(
                    new Order(orderTable.getId(), OrderStatus.COOKING.name(), LocalDateTime.now(), orderLineItems));

            // when
            Order changedOrder = orderService.changeOrderStatus(order.getId(), OrderStatus.MEAL.name());

            // then
            assertThat(changedOrder.getOrderStatus()).isEqualTo(OrderStatus.MEAL.name());
        }

        @DisplayName("주문을 찾지 못하면 예외를 발생시킨다.")
        @Test
        void notFoundOrder_exception() {
            // when
            assertThatThrownBy(() -> orderService.changeOrderStatus(0L, OrderStatus.MEAL.name()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("주문 상태가 완료이면 예외를 발생시킨다.")
        @Test
        void orderStatusIsCompletion_exception() {
            // given
            orderDao.save(new Order(orderTable.getId(), OrderStatus.COMPLETION.name(), LocalDateTime.now(),
                    orderLineItems));

            // when
            assertThatThrownBy(() -> orderService.changeOrderStatus(0L, OrderStatus.MEAL.name()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
