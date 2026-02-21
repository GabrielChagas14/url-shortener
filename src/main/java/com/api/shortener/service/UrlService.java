package com.api.shortener.service;
import com.api.shortener.domain.url.UrlResponseDTO;
import com.api.shortener.repositories.UrlRepository;

import com.api.shortener.domain.url.Url;
import com.api.shortener.domain.url.UrlRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public UrlResponseDTO createUrl(UrlRequestDTO data) {

        Optional<Url> existingUrl = urlRepository.findByOriginalUrl(data.originalUrl());

        if (existingUrl.isPresent()) {
            Url url = existingUrl.get();
            String shortUrl = shortenUrl(url.getId());
            return new UrlResponseDTO(url.getOriginalUrl(), shortUrl, url.getValid());
        }

        Url url = new Url();
        url.setOriginalUrl(data.originalUrl());
        url.setValid(LocalDateTime.now().plusDays(7));

        urlRepository.save(url);

        String shortUrl = shortenUrl(url.getId());
        return new UrlResponseDTO(url.getOriginalUrl(), shortUrl,url.getValid());
    }

    private String shortenUrl(Long urlId) {
        String shortKey = convertToBase62(urlId);
        return baseUrl + shortKey;
    }

    private String convertToBase62(Long urlId) {
        StringBuilder sb = new StringBuilder();
        while (urlId > 0) {
            sb.append(BASE62.charAt((int) (urlId % 62)));
            urlId /= 62;
        }
        return sb.reverse().toString();
    }

    public Url findByShortCode(String shortCode) {

        long urlId = convertToLongId(shortCode);
        Url url = urlRepository.findById(urlId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "URL não encontrada"));

        if (url.getValid().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.GONE, "URL expirada");
        }

        return url;

    }

    private long convertToLongId(String shortKey) {
        long id = 0L;
        for (int i = 0; i < shortKey.length(); i++) {
            int base62Value = BASE62.indexOf(shortKey.charAt(i));
            id = id * 62 + base62Value;
        }
        return id;
    }
}
