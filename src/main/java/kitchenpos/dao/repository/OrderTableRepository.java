package kitchenpos.dao.repository;

import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.OrderTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderTableRepository extends JpaRepository<OrderTable, Long>, OrderTableDao {
}
