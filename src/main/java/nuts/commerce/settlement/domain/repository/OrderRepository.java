package nuts.commerce.settlement.domain.repository;

import nuts.commerce.settlement.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findBySellerIdAndPaidAtBetween(Long sellerId, Instant fromInclusive, Instant toExclusive);
}