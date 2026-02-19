package com.api.shortener.repositories;

import com.api.shortener.domain.access.Access;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessRepository extends JpaRepository <Access, Long> {
}
