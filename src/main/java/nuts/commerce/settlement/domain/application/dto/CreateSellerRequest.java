package nuts.commerce.settlement.domain.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSellerRequest(
        @NotBlank String name,
        @NotBlank String businessNo
) {
}