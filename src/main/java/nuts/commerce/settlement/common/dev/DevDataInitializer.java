package nuts.commerce.settlement.common.dev;

import nuts.commerce.settlement.domain.model.Order;
import nuts.commerce.settlement.domain.model.Refund;
import nuts.commerce.settlement.domain.model.Seller;
import nuts.commerce.settlement.domain.repository.OrderRepository;
import nuts.commerce.settlement.domain.repository.RefundRepository;
import nuts.commerce.settlement.domain.repository.SellerRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@Profile({"dev", "docker"})
public class DevDataInitializer {

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final Clock clock;
    private final ZoneId zoneId = ZoneId.of("Asia/Seoul");

    public DevDataInitializer(SellerRepository sellerRepository, OrderRepository orderRepository, RefundRepository refundRepository, Clock clock) {
        this.sellerRepository = sellerRepository;
        this.orderRepository = orderRepository;
        this.refundRepository = refundRepository;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (!sellerRepository.findAll().isEmpty()) return; // 이미 데이터가 있으면 건너뜀

        Seller s1 = sellerRepository.save(new Seller("seller-1", "BIZ-001"));
        Seller s2 = sellerRepository.save(new Seller("seller-2", "BIZ-002"));

        // 날짜 기준: 오늘(앱 Clock) 기준으로 어제와 이틀전
        LocalDate today = LocalDate.now(clock);
        Instant yesterdayStart = today.minusDays(1).atStartOfDay(zoneId).toInstant();
        Instant yesterdayMid = yesterdayStart.plusSeconds(12 * 3600);
        Instant twoDaysAgoMid = today.minusDays(2).atStartOfDay(zoneId).plusSeconds(14 * 3600).toInstant();

        // seller1: 2 orders yesterday, 1 refund yesterday
        orderRepository.saveAll(List.of(
                new Order(s1, new BigDecimal("100.00"), yesterdayMid),
                new Order(s1, new BigDecimal("50.00"), yesterdayMid.plusSeconds(3600))
        ));
        refundRepository.save(new Refund(s1, orderRepository.findAll().get(0), new BigDecimal("30.00"), yesterdayMid.plusSeconds(7200)));

        // seller2: 1 order two days ago, 1 order yesterday
        orderRepository.save(new Order(s2, new BigDecimal("200.00"), twoDaysAgoMid));
        orderRepository.save(new Order(s2, new BigDecimal("80.00"), yesterdayMid.plusSeconds(1800)));

    }
}

