package nuts.commerce.settlement.domain.web;

import jakarta.validation.Valid;
import nuts.commerce.settlement.domain.application.dto.CreateSellerRequest;
import nuts.commerce.settlement.domain.model.Seller;
import nuts.commerce.settlement.domain.repository.SellerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/sellers")
public class AdminSellerController {

    private final SellerRepository sellerRepository;

    public AdminSellerController(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    @PostMapping
    public Seller create(@RequestBody @Valid CreateSellerRequest req) {
        return sellerRepository.save(new Seller(req.name(), req.businessNo()));
    }

    @GetMapping
    public List<Seller> list() {
        return sellerRepository.findAll();
    }
}