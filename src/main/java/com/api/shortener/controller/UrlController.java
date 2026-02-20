package com.api.shortener.controller;

import com.api.shortener.domain.url.Url;
import com.api.shortener.domain.url.UrlRequestDTO;
import com.api.shortener.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/url")
public class UrlController {

    @Autowired
    private UrlService urlService;

    @PostMapping("/create")
    public ResponseEntity<Url> create(@RequestBody UrlRequestDTO body) {
        Url newUrl = this.urlService.createUrl(body);
        return ResponseEntity.ok(newUrl);
    }
}
