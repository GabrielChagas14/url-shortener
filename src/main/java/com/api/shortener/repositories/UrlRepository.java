package com.api.shortener.repositories;

import com.api.shortener.domain.url.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository <Url, Long> {
    Optional<Url> findByOriginalUrl(String originalUrl);
}
