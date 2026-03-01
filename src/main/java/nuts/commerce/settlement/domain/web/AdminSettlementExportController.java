package nuts.commerce.settlement.domain.web;

import nuts.commerce.settlement.domain.model.Settlement;
import nuts.commerce.settlement.domain.model.SettlementItem;
import nuts.commerce.settlement.domain.service.SettlementQueryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/admin/settlements")
public class AdminSettlementExportController {

    private final SettlementQueryService queryService;

    public AdminSettlementExportController(SettlementQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping(value = "/{settlementId}/export.csv", produces = "text/csv")
    public @ResponseBody byte[] exportCsv(@PathVariable Long settlementId, @RequestHeader HttpHeaders headers) {
        Settlement settlement = queryService.get(settlementId);
        List<SettlementItem> items = queryService.items(settlementId);

        StringBuilder sb = new StringBuilder();
        sb.append("settlementId,sellerId,settlementDate,version,status,gross,refund,fee,net\n");
        sb.append(settlement.getId()).append(',')
                .append(settlement.getSeller().getId()).append(',')
                .append(settlement.getSettlementDate()).append(',')
                .append(settlement.getVersion()).append(',')
                .append(settlement.getStatus()).append(',')
                .append(settlement.getGrossAmount()).append(',')
                .append(settlement.getRefundAmount()).append(',')
                .append(settlement.getFeeAmount()).append(',')
                .append(settlement.getNetAmount()).append('\n');

        sb.append("\n");
        sb.append("itemId,type,orderId,refundId,amount\n");
        for (SettlementItem it : items) {
            sb.append(it.getId()).append(',')
                    .append(it.getType()).append(',')
                    .append(it.getOrderId()).append(',')
                    .append(it.getRefundId()).append(',')
                    .append(it.getAmount()).append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @GetMapping(value = "/{settlementId}/export", produces = MediaType.TEXT_PLAIN_VALUE)
    public String exportHint(@PathVariable Long settlementId) {
        return "Use /admin/settlements/" + settlementId + "/export.csv";
    }
}