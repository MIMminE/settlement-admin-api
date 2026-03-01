package nuts.commerce.settlement.domain.web;


import jakarta.validation.Valid;
import nuts.commerce.settlement.domain.application.dto.ConfirmSettlementRequest;
import nuts.commerce.settlement.domain.application.dto.RerunSettlementRequest;
import nuts.commerce.settlement.domain.model.Settlement;
import nuts.commerce.settlement.domain.model.SettlementItem;
import nuts.commerce.settlement.domain.service.SettlementCommandService;
import nuts.commerce.settlement.domain.service.SettlementQueryService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/settlements")
public class AdminSettlementController {

    private final SettlementCommandService commandService;
    private final SettlementQueryService queryService;

    public AdminSettlementController(SettlementCommandService commandService, SettlementQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping("/confirm")
    public Settlement confirm(@RequestBody @Valid ConfirmSettlementRequest req, Authentication authentication) {
        String actor = authentication != null ? authentication.getName() : "unknown";
        return commandService.confirm(req.settlementId(), actor);
    }

    @PostMapping("/rerun")
    public Settlement rerun(@RequestBody @Valid RerunSettlementRequest req, Authentication authentication) {
        String actor = authentication != null ? authentication.getName() : "unknown";
        return commandService.rerunDaily(req.sellerId(), req.settlementDate(), actor);
    }

    @GetMapping("/{settlementId}")
    public Settlement get(@PathVariable Long settlementId) {
        return queryService.get(settlementId);
    }

    @GetMapping("/{settlementId}/items")
    public List<SettlementItem> items(@PathVariable Long settlementId) {
        return queryService.items(settlementId);
    }
}