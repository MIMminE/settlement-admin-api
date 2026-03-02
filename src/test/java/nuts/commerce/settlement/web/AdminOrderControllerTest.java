package nuts.commerce.settlement.web;

import nuts.commerce.settlement.domain.application.dto.CreateOrderRequest;
import nuts.commerce.settlement.domain.model.Order;
import nuts.commerce.settlement.domain.model.Seller;
import nuts.commerce.settlement.domain.repository.OrderRepository;
import nuts.commerce.settlement.domain.repository.SellerRepository;
import nuts.commerce.settlement.domain.web.AdminOrderController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

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
class AdminOrderControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @Mock
    SellerRepository sellerRepository;
    @Mock
    OrderRepository orderRepository;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        AdminOrderController controller = new AdminOrderController(sellerRepository, orderRepository);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void list_shouldReturnOk() throws Exception {
        when(orderRepository.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/admin/orders")).andExpect(status().isOk());
    }

    @Test
    void create_shouldReturnOk() throws Exception {
        Seller s = new Seller("s1", "bn");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(s));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderRequest req = new CreateOrderRequest(1L, new BigDecimal("100.00"), Instant.now());
        mockMvc.perform(post("/admin/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
