package nuts.commerce.settlement.domain.service;

import nuts.commerce.settlement.domain.model.Settlement;
import nuts.commerce.settlement.domain.model.SettlementItem;
import nuts.commerce.settlement.domain.repository.SettlementItemRepository;
import nuts.commerce.settlement.domain.repository.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SettlementQueryService {

    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;

    public SettlementQueryService(SettlementRepository settlementRepository, SettlementItemRepository settlementItemRepository) {
        this.settlementRepository = settlementRepository;
        this.settlementItemRepository = settlementItemRepository;
    }

    @Transactional(readOnly = true)
    public Settlement get(Long settlementId) {
        return settlementRepository.findById(settlementId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<SettlementItem> items(Long settlementId) {
        return settlementItemRepository.findBySettlementId(settlementId);
    }
}