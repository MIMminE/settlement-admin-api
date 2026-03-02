package nuts.commerce.settlement.web;

import tools.jackson.databind.ObjectMapper;
import nuts.commerce.settlement.domain.application.dto.ConfirmSettlementRequest;
import nuts.commerce.settlement.domain.application.dto.RerunSettlementRequest;
import nuts.commerce.settlement.domain.model.Settlement;
import nuts.commerce.settlement.domain.model.Seller;
import nuts.commerce.settlement.domain.web.AdminSettlementController;
import nuts.commerce.settlement.domain.service.SettlementCommandService;
import nuts.commerce.settlement.domain.service.SettlementQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminSettlementControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @Mock
    SettlementCommandService commandService;

    @Mock
    SettlementQueryService queryService;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        AdminSettlementController controller = new AdminSettlementController(commandService, queryService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void confirm_shouldReturnOk() throws Exception {
        Seller s = new Seller("s-test", "bn");
        Settlement st = new Settlement(s, LocalDate.of(2026,3,1), 1);
        st.markCalculated(new BigDecimal("10000.00"), new BigDecimal("1000.00"), new BigDecimal("450.00"), new BigDecimal("8550.00"));

        when(commandService.confirm(eq(1L), anyString())).thenReturn(st);
        ConfirmSettlementRequest req = new ConfirmSettlementRequest(1L);
        mockMvc.perform(post("/admin/settlements/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void rerun_shouldReturnOk() throws Exception {
        Seller s = new Seller("s-test", "bn");
        Settlement st = new Settlement(s, LocalDate.of(2026,3,1), 2);
        st.markCalculated(new BigDecimal("10000.00"), new BigDecimal("1000.00"), new BigDecimal("450.00"), new BigDecimal("8550.00"));

        when(commandService.rerunDaily(eq(1L), eq(LocalDate.of(2026,3,1)), anyString())).thenReturn(st);
        RerunSettlementRequest req = new RerunSettlementRequest(1L, LocalDate.of(2026,3,1));
        mockMvc.perform(post("/admin/settlements/rerun")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
