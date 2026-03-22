package com.api.shortener.service;

import com.api.shortener.domain.url.Url;
import com.api.shortener.domain.url.UrlRequestDTO;
import com.api.shortener.domain.url.UrlResponseDTO;
import com.api.shortener.repositories.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @InjectMocks
    private UrlService urlService;

    private static final String BASE_URL = "http://localhost:8080/";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "baseUrl", BASE_URL);
    }

    @Test
    @DisplayName("createUrl: URL nova deve ser salva e retornar DTO com shortUrl")
    void createUrl_novaUrl_deveSalvarERetornarDTO() {
        String originalUrl = "https://www.google.com";
        UrlRequestDTO request = new UrlRequestDTO(originalUrl);

        // Simula repositório vazio (URL não existe ainda)
        when(urlRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.empty());

        // Simula save: atribui ID à entidade
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> {
            Url url = invocation.getArgument(0);
            ReflectionTestUtils.setField(url, "id", 1L);
            return url;
        });

        UrlResponseDTO response = urlService.createUrl(request);

        assertThat(response.originalUrl()).isEqualTo(originalUrl);
        assertThat(response.shortUrl()).startsWith(BASE_URL);
        assertThat(response.valid()).isAfter(LocalDateTime.now());
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    @DisplayName("createUrl: URL já existente não deve ser salva novamente (idempotência)")
    void createUrl_urlExistente_naoDeveSalvarNovamente() {
        String originalUrl = "https://www.example.com";
        UrlRequestDTO request = new UrlRequestDTO(originalUrl);

        Url existingUrl = new Url();
        ReflectionTestUtils.setField(existingUrl, "id", 10L);
        existingUrl.setOriginalUrl(originalUrl);
        existingUrl.setValid(LocalDateTime.now().plusDays(5));

        when(urlRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.of(existingUrl));

        UrlResponseDTO response = urlService.createUrl(request);

        assertThat(response.originalUrl()).isEqualTo(originalUrl);
        assertThat(response.shortUrl()).startsWith(BASE_URL);
        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    @DisplayName("findByShortCode: código válido deve retornar a URL correta")
    void findByShortCode_codigoValido_deveRetornarUrl() {
        // ID 1 → base62 "b" (índice 1 no alfabeto base62)
        Url url = new Url();
        ReflectionTestUtils.setField(url, "id", 1L);
        url.setOriginalUrl("https://www.google.com");
        url.setValid(LocalDateTime.now().plusDays(7));

        when(urlRepository.findById(1L)).thenReturn(Optional.of(url));

        Url result = urlService.findByShortCode("b");

        assertThat(result.getOriginalUrl()).isEqualTo("https://www.google.com");
    }

    @Test
    @DisplayName("findByShortCode: URL expirada deve lançar 410 GONE")
    void findByShortCode_urlExpirada_deveLancar410() {
        Url url = new Url();
        ReflectionTestUtils.setField(url, "id", 1L);
        url.setOriginalUrl("https://expirado.com");
        url.setValid(LocalDateTime.now().minusDays(1)); // já expirou

        when(urlRepository.findById(1L)).thenReturn(Optional.of(url));

        assertThatThrownBy(() -> urlService.findByShortCode("b"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("URL expirada");
    }

    @Test
    @DisplayName("findByShortCode: código inexistente deve lançar 404 NOT_FOUND")
    void findByShortCode_codigoInexistente_deveLancar404() {
        when(urlRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.findByShortCode("b"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("URL não encontrada");
    }
}
