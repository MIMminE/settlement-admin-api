package nuts.commerce.settlement.domain.web;

import jakarta.validation.Valid;
import nuts.commerce.settlement.domain.application.dto.CreateOrderRequest;
import nuts.commerce.settlement.domain.model.Order;
import nuts.commerce.settlement.domain.model.Seller;
import nuts.commerce.settlement.domain.repository.OrderRepository;
import nuts.commerce.settlement.domain.repository.SellerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;

    public AdminOrderController(SellerRepository sellerRepository, OrderRepository orderRepository) {
        this.sellerRepository = sellerRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public Order create(@RequestBody @Valid CreateOrderRequest req) {
        Seller seller = sellerRepository.findById(req.sellerId()).orElseThrow();
        return orderRepository.save(new Order(seller, req.paidAmount(), req.paidAt()));
    }

    @GetMapping
    public List<Order> list() {
        return orderRepository.findAll();
    }
}