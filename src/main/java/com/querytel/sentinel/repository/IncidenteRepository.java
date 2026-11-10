package com.querytel.sentinel.repository;

import com.querytel.sentinel.model.Incidente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidenteRepository extends JpaRepository<Incidente, Long> {
}
