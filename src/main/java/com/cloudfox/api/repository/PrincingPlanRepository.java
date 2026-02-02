package com.cloudfox.api.repository;

import com.cloudfox.api.model.PricingPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrincingPlanRepository extends JpaRepository<PricingPlan, UUID> {

    PricingPlan findPricingPlanByCode(String code);
}

