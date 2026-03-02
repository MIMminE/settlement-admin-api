package nuts.commerce.settlement.web;

import tools.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nuts.commerce.settlement.domain.application.dto.CreateSellerRequest;
import nuts.commerce.settlement.domain.model.Seller;
import nuts.commerce.settlement.domain.repository.SellerRepository;
import nuts.commerce.settlement.domain.web.AdminSellerController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminSellerControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @Mock
    SellerRepository sellerRepository;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        AdminSellerController controller = new AdminSellerController(sellerRepository);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void list_shouldReturnOk() throws Exception {
        when(sellerRepository.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/admin/sellers")).andExpect(status().isOk());
    }

    @Test
    void create_shouldReturnOk() throws Exception {
        when(sellerRepository.save(any(Seller.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateSellerRequest req = new CreateSellerRequest("s1", "bn");
        mockMvc.perform(post("/admin/sellers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
