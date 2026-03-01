package nuts.commerce.settlement.security;


import jakarta.validation.Valid;
import nuts.commerce.settlement.domain.application.dto.CreateRefundRequest;
import nuts.commerce.settlement.domain.model.Order;
import nuts.commerce.settlement.domain.model.Refund;
import nuts.commerce.settlement.domain.model.Seller;
import nuts.commerce.settlement.domain.repository.OrderRepository;
import nuts.commerce.settlement.domain.repository.RefundRepository;
import nuts.commerce.settlement.domain.repository.SellerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/refunds")
public class AdminRefundController {

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;

    public AdminRefundController(SellerRepository sellerRepository, OrderRepository orderRepository, RefundRepository refundRepository) {
        this.sellerRepository = sellerRepository;
        this.orderRepository = orderRepository;
        this.refundRepository = refundRepository;
    }

    @PostMapping
    public Refund create(@RequestBody @Valid CreateRefundRequest req) {
        Seller seller = sellerRepository.findById(req.sellerId()).orElseThrow();
        Order order = orderRepository.findById(req.orderId()).orElseThrow();
        return refundRepository.save(new Refund(seller, order, req.refundedAmount(), req.refundedAt()));
    }

    @GetMapping
    public List<Refund> list() {
        return refundRepository.findAll();
    }
}