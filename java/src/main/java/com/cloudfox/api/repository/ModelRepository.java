package com.cloudfox.api.repository;

import com.cloudfox.api.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ModelRepository extends JpaRepository<Model, UUID> {

    Optional<Model> findById(UUID id);

    Optional<Model> findByUserId(UUID id);

}
