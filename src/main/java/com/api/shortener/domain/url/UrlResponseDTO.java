package com.api.shortener.domain.url;

import java.time.LocalDateTime;

public record UrlResponseDTO(String originalUrl, String shortUrl, LocalDateTime valid) {
}
