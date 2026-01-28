package com.cloudfox.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PricingPlanEnum {
    FREE_PLAN("CFX_FREE"),
    PRO_PLAN("CFX_PRO"),
    ENTERPRISE_PLAN("CFX_ENTERPRISE");

    private final String value;
}