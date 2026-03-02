package nuts.commerce.settlement.service;

import nuts.commerce.settlement.domain.model.*;
import nuts.commerce.settlement.domain.repository.*;
import nuts.commerce.settlement.domain.service.AuditService;
import nuts.commerce.settlement.domain.service.SettlementCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementCommandServiceTest {

    @Mock
    SellerRepository sellerRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    RefundRepository refundRepository;
    @Mock
    SettlementRepository settlementRepository;
    @Mock
    SettlementItemRepository settlementItemRepository;
    @Mock
    AuditService auditService;

    @InjectMocks
    SettlementCommandService service;

    @BeforeEach
    void setup() {
        service = new SettlementCommandService(sellerRepository, orderRepository, refundRepository,
                settlementRepository, settlementItemRepository, auditService, ZoneId.of("Asia/Seoul"));
    }

    @Test
    void calculateDaily_shouldComputeAmountsAndSaveSettlementAndItems() {
        Seller seller = new Seller("s1", "bn");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));

        Instant from = LocalDate.of(2026,3,1).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
        Instant to = LocalDate.of(2026,3,2).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();

        Order o1 = new Order(seller, new BigDecimal("10000.00"), from.plusSeconds(3600));
        when(orderRepository.findBySellerIdAndPaidAtBetween(1L, from, to)).thenReturn(List.of(o1));

        Refund r1 = new Refund(seller, o1, new BigDecimal("1000.00"), from.plusSeconds(7200));
        when(refundRepository.findBySellerIdAndRefundedAtBetween(1L, from, to)).thenReturn(List.of(r1));

        var settlement = service.calculateDaily(1L, LocalDate.of(2026,3,1), 1, "testActor");

        assertThat(settlement.getGrossAmount()).isEqualByComparingTo(new BigDecimal("10000.00"));
        assertThat(settlement.getRefundAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(settlement.getFeeAmount()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(settlement.getNetAmount()).isEqualByComparingTo(new BigDecimal("8550.00"));

        verify(settlementRepository, atLeastOnce()).save(any(Settlement.class));
        verify(settlementItemRepository, atLeast(1)).save(any(SettlementItem.class));
        verify(auditService).record(eq(nuts.commerce.settlement.domain.model.enums.AuditAction.SETTLEMENT_CALCULATED), eq("testActor"), anyString());
    }
}
