package com.api.shortener.controller;

import com.api.shortener.domain.url.Url;
import com.api.shortener.domain.url.UrlRequestDTO;
import com.api.shortener.domain.url.UrlResponseDTO;
import com.api.shortener.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;

@RestController
public class UrlController {

    @Autowired
    private UrlService urlService;

    @PostMapping("api/url/create")
    public ResponseEntity<UrlResponseDTO> create(@RequestBody UrlRequestDTO body) {
        UrlResponseDTO newUrl = this.urlService.createUrl(body);
        return ResponseEntity.ok(newUrl);
    }

    @GetMapping("/{shortCode}")
    public RedirectView redirect(@PathVariable String shortCode) {

        Url url = urlService.findByShortCode(shortCode);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(url.getOriginalUrl());
        return redirectView;
    }

}
