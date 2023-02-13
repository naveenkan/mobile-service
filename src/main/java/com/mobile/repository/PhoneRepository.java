package com.mobile.repository;

import com.mobile.repository.entity.PhoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneRepository extends JpaRepository<PhoneEntity, Long> {
  Optional<PhoneEntity> findByName(String name);
}
