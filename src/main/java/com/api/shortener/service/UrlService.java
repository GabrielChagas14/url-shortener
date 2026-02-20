package com.api.shortener.service;
import com.api.shortener.repositories.UrlRepository;

import com.api.shortener.domain.url.Url;
import com.api.shortener.domain.url.UrlRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    public Url createUrl(UrlRequestDTO data) {

        Url url = new Url();
        url.setOriginalUrl(data.originalUrl());
        url.setValid(LocalDateTime.now().plusDays(7));
        urlRepository.save(url);
        return url;
    }
}
