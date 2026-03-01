package nuts.commerce.settlement.domain.service;

import nuts.commerce.settlement.domain.model.*;
import nuts.commerce.settlement.domain.model.enums.AuditAction;
import nuts.commerce.settlement.domain.model.enums.SettlementStatus;
import nuts.commerce.settlement.domain.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class SettlementCommandService {

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;
    private final AuditService auditService;
    private final ZoneId appZoneId;

    private final SettlementCalculator calculator = new SettlementCalculator(new BigDecimal("0.05"));

    public SettlementCommandService(
            SellerRepository sellerRepository,
            OrderRepository orderRepository,
            RefundRepository refundRepository,
            SettlementRepository settlementRepository,
            SettlementItemRepository settlementItemRepository,
            AuditService auditService,
            ZoneId appZoneId
    ) {
        this.sellerRepository = sellerRepository;
        this.orderRepository = orderRepository;
        this.refundRepository = refundRepository;
        this.settlementRepository = settlementRepository;
        this.settlementItemRepository = settlementItemRepository;
        this.auditService = auditService;
        this.appZoneId = appZoneId;
    }

    @Transactional
    public Settlement calculateDaily(Long sellerId, LocalDate date, int version, String actor) {
        Seller seller = sellerRepository.findById(sellerId).orElseThrow();

        Instant from = date.atStartOfDay(appZoneId).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(appZoneId).toInstant();

        List<Order> orders = orderRepository.findBySellerIdAndPaidAtBetween(sellerId, from, to);
        List<Refund> refunds = refundRepository.findBySellerIdAndRefundedAtBetween(sellerId, from, to);

        Settlement settlement = new Settlement(seller, date, version);
        settlementRepository.save(settlement);

        BigDecimal gross = BigDecimal.ZERO;
        for (Order o : orders) {
            gross = gross.add(o.getPaidAmount());
            settlementItemRepository.save(SettlementItem.order(settlement, o.getId(), o.getPaidAmount()));
        }

        BigDecimal refundAmount = BigDecimal.ZERO;
        for (Refund r : refunds) {
            refundAmount = refundAmount.add(r.getRefundedAmount());
            settlementItemRepository.save(SettlementItem.refund(settlement, r.getId(), r.getOrder().getId(), r.getRefundedAmount().negate()));
        }

        BigDecimal base = gross.subtract(refundAmount);
        BigDecimal fee = calculator.feeOf(base);
        BigDecimal net = base.subtract(fee);

        settlement.markCalculated(gross, refundAmount, fee, net);

        auditService.record(
                AuditAction.SETTLEMENT_CALCULATED,
                actor,
                "Calculated sellerId=" + sellerId + " date=" + date + " v=" + version +
                        " gross=" + gross + " refund=" + refundAmount + " fee=" + fee + " net=" + net
        );

        return settlement;
    }

    @Transactional
    public Settlement confirm(Long settlementId, String actor) {
        Settlement settlement = settlementRepository.findById(settlementId).orElseThrow();
        settlement.confirm(actor);

        auditService.record(
                AuditAction.SETTLEMENT_CONFIRMED,
                actor,
                "Confirmed settlementId=" + settlementId
        );
        return settlement;
    }

    @Transactional
    public Settlement rerunDaily(Long sellerId, LocalDate date, String actor) {
        // 최신 버전 조회 -> 기존 최신을 SUPERSEDED 처리 -> next version 생성
        Settlement latest = settlementRepository.findTopBySellerIdAndSettlementDateOrderByVersionDesc(sellerId, date)
                .orElse(null);

        int nextVersion = 1;
        if (latest != null) {
            nextVersion = latest.getVersion() + 1;
            latest.supersede();
        }

        auditService.record(
                AuditAction.SETTLEMENT_RERUN,
                actor,
                "Rerun sellerId=" + sellerId + " date=" + date + " nextVersion=" + nextVersion
        );

        return calculateDaily(sellerId, date, nextVersion, actor);
    }

    @Transactional
    public void supersedeAllConfirmedOfDate(LocalDate date) {
        List<Settlement> confirmed = settlementRepository.findBySettlementDateAndStatus(date, SettlementStatus.CONFIRMED);
        confirmed.forEach(Settlement::supersede);
    }
}