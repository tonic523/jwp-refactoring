package kitchenpos.dao.repository;

import kitchenpos.dao.OrderDao;
import kitchenpos.domain.Order;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;

@Primary
public interface OrderRepository extends JpaRepository<Order, Long>, OrderDao {
}