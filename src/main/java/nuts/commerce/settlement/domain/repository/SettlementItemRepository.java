package nuts.commerce.settlement.domain.repository;

import nuts.commerce.settlement.domain.model.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {
    List<SettlementItem> findBySettlementId(Long settlementId);
}