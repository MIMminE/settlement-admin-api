package nuts.commerce.settlement.domain.service;

import nuts.commerce.settlement.domain.application.dto.SettlementSummaryResponse;
import nuts.commerce.settlement.domain.model.Settlement;
import nuts.commerce.settlement.domain.model.SettlementItem;
import nuts.commerce.settlement.domain.repository.SettlementItemRepository;
import nuts.commerce.settlement.domain.repository.SettlementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Transactional(readOnly = true)
    public Page<SettlementSummaryResponse> list(Long sellerId, LocalDate from, LocalDate to, Pageable pageable) {
        Page<Settlement> page = (sellerId == null)
                ? settlementRepository.findBySettlementDateBetween(from, to, pageable)
                : settlementRepository.findBySellerIdAndSettlementDateBetween(sellerId, from, to, pageable);

        return page.map(s -> new SettlementSummaryResponse(
                s.getId(),
                s.getSeller().getId(),
                s.getSeller().getName(),
                s.getSettlementDate(),
                s.getVersion(),
                s.getStatus(),
                s.getGrossAmount(),
                s.getRefundAmount(),
                s.getFeeAmount(),
                s.getNetAmount()
        ));
    }
}