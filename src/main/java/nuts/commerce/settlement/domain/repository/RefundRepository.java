package nuts.commerce.settlement.domain.repository;


import nuts.commerce.settlement.domain.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findBySellerIdAndRefundedAtBetween(Long sellerId, Instant fromInclusive, Instant toExclusive);
}