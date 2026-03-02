package nuts.commerce.settlement.security;

import nuts.commerce.settlement.domain.model.Order;
import nuts.commerce.settlement.domain.model.Refund;
import nuts.commerce.settlement.domain.model.Seller;
import nuts.commerce.settlement.domain.repository.OrderRepository;
import nuts.commerce.settlement.domain.repository.RefundRepository;
import nuts.commerce.settlement.domain.repository.SellerRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class DomainDataInitializer {

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;

    public DomainDataInitializer(SellerRepository sellerRepository,
                                 OrderRepository orderRepository,
                                 RefundRepository refundRepository) {
        this.sellerRepository = sellerRepository;
        this.orderRepository = orderRepository;
        this.refundRepository = refundRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        // 포트폴리오용 기본 데이터: 이미 있으면 스킵
        if (sellerRepository.count() > 0) {
            return;
        }

        // Seller A: 여러 주문 + 여러 환불
        Seller s1 = sellerRepository.save(new Seller("sellerA", "123-45-67890"));
        // 기존 기본 주문
        Order o1 = orderRepository.save(new Order(s1, new BigDecimal("10000.00"), Instant.parse("2026-03-01T01:00:00Z")));
        refundRepository.save(new Refund(s1, o1, new BigDecimal("1000.00"), Instant.parse("2026-03-02T01:00:00Z")));
        refundRepository.save(new Refund(s1, o1, new BigDecimal("500.00"), Instant.parse("2026-03-03T01:00:00Z")));

        // 추가 주문/환불들
        Order o2 = orderRepository.save(new Order(s1, new BigDecimal("2000.00"), Instant.parse("2026-03-01T03:00:00Z")));
        refundRepository.save(new Refund(s1, o2, new BigDecimal("200.00"), Instant.parse("2026-03-02T03:00:00Z")));

        Order o3 = orderRepository.save(new Order(s1, new BigDecimal("3000.00"), Instant.parse("2026-03-01T04:00:00Z")));
        refundRepository.save(new Refund(s1, o3, new BigDecimal("300.00"), Instant.parse("2026-03-02T04:00:00Z")));

        Order o4 = orderRepository.save(new Order(s1, new BigDecimal("1500.00"), Instant.parse("2026-03-01T05:00:00Z")));

        // Seller B: 주문 1건 + 환불 1건
        Seller s2 = sellerRepository.save(new Seller("sellerB", "222-22-22222"));
        Order b1 = orderRepository.save(new Order(s2, new BigDecimal("5000.00"), Instant.parse("2026-03-01T02:00:00Z")));
        refundRepository.save(new Refund(s2, b1, new BigDecimal("250.00"), Instant.parse("2026-03-02T02:00:00Z")));

        // Seller C: 여러 주문과 환불(테스트 다양화용)
        Seller s3 = sellerRepository.save(new Seller("sellerC", "333-33-33333"));
        Order c1 = orderRepository.save(new Order(s3, new BigDecimal("8000.00"), Instant.parse("2026-03-01T06:00:00Z")));
        Order c2 = orderRepository.save(new Order(s3, new BigDecimal("12000.00"), Instant.parse("2026-03-01T07:00:00Z")));
        refundRepository.save(new Refund(s3, c1, new BigDecimal("800.00"), Instant.parse("2026-03-02T06:00:00Z")));
        refundRepository.save(new Refund(s3, c2, new BigDecimal("1200.00"), Instant.parse("2026-03-03T07:00:00Z")));
    }
}
