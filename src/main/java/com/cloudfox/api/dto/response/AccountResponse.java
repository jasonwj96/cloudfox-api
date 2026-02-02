package com.cloudfox.api.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountResponse {
    private String username;
    private String fullname;
    private String email;
    private long tokenBalance;
    private long pricingPlanMicros;
    private String pricingPlanCurrency;
}