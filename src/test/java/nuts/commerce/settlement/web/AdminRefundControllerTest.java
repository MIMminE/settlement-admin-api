package nuts.commerce.settlement.web;

import tools.jackson.databind.ObjectMapper;
import nuts.commerce.settlement.domain.application.dto.CreateRefundRequest;
import nuts.commerce.settlement.domain.model.Order;
import nuts.commerce.settlement.domain.model.Refund;
import nuts.commerce.settlement.domain.model.Seller;
import nuts.commerce.settlement.domain.repository.OrderRepository;
import nuts.commerce.settlement.domain.repository.RefundRepository;
import nuts.commerce.settlement.domain.repository.SellerRepository;
import nuts.commerce.settlement.domain.web.AdminRefundController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminRefundControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @Mock
    SellerRepository sellerRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    RefundRepository refundRepository;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        AdminRefundController controller = new AdminRefundController(sellerRepository, orderRepository, refundRepository);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void list_shouldReturnOk() throws Exception {
        when(refundRepository.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/admin/refunds")).andExpect(status().isOk());
    }

    @Test
    void create_shouldReturnCreated() throws Exception {
        Seller s = new Seller("s1", "bn");
        Order o = new Order(s, new BigDecimal("100.00"), Instant.now());
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(s));
        when(orderRepository.findById(2L)).thenReturn(Optional.of(o));
        when(refundRepository.save(any(Refund.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateRefundRequest req = new CreateRefundRequest(1L, 2L, new BigDecimal("10.00"), Instant.now());
        mockMvc.perform(post("/admin/refunds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
