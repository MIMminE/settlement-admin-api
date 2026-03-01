package nuts.commerce.settlement.domain.repository;

import nuts.commerce.settlement.domain.model.Settlement;
import nuts.commerce.settlement.domain.model.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findTopBySellerIdAndSettlementDateOrderByVersionDesc(Long sellerId, LocalDate settlementDate);

    List<Settlement> findBySettlementDateAndStatus(LocalDate settlementDate, SettlementStatus status);
}