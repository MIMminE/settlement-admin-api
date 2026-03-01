package nuts.commerce.settlement.domain.repository;

import nuts.commerce.settlement.domain.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long> {
}
