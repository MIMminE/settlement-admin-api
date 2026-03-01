package nuts.commerce.settlement.domain.repository;

import nuts.commerce.settlement.domain.model.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findTopBySellerIdAndSettlementDateOrderByVersionDesc(Long sellerId, LocalDate settlementDate);

    Page<Settlement> findBySettlementDateBetween(LocalDate from, LocalDate to, Pageable pageable);

    Page<Settlement> findBySellerIdAndSettlementDateBetween(Long sellerId, LocalDate from, LocalDate to, Pageable pageable);
}