package com.cloudfox.api.repository;

import com.cloudfox.api.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CurrencyRepository extends JpaRepository<Currency, UUID> {

    Currency findCurrencyByCode(String code);
}
