package com.api.shortener.repositories;

import com.api.shortener.domain.url.Url;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository <Url, Long> {
}
