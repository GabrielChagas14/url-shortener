package com.api.shortener;

import com.api.shortener.domain.url.UrlRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShortenerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Contexto da aplicação deve carregar corretamente")
    void contextLoads() {
    }

    @Test
    @DisplayName("POST /api/url/create deve retornar 200 com originalUrl e shortUrl")
    void createUrl_deveRetornar200ComShortUrl() throws Exception {
        UrlRequestDTO request = new UrlRequestDTO("https://www.github.com");

        MvcResult result = mockMvc.perform(post("/api/url/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value("https://www.github.com"))
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.valid").exists())
                .andReturn();

        String shortUrl = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("shortUrl").asText();
        assertThat(shortUrl).contains("localhost:8080");
    }

    @Test
    @DisplayName("POST /api/url/create com a mesma URL deve retornar a mesma shortUrl (idempotência)")
    void createUrl_mesmaUrl_deveRetornarMesmaShortUrl() throws Exception {
        UrlRequestDTO request = new UrlRequestDTO("https://www.idempotencia.com");
        String body = objectMapper.writeValueAsString(request);

        MvcResult first = mockMvc.perform(post("/api/url/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult second = mockMvc.perform(post("/api/url/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        String shortUrl1 = objectMapper.readTree(first.getResponse().getContentAsString()).get("shortUrl").asText();
        String shortUrl2 = objectMapper.readTree(second.getResponse().getContentAsString()).get("shortUrl").asText();

        assertThat(shortUrl1).isEqualTo(shortUrl2);
    }

    @Test
    @DisplayName("GET /{shortCode} com código válido deve redirecionar (302) para a URL original")
    void redirect_codigoValido_deveRedirecionar() throws Exception {
        // Primeiro cria a URL
        UrlRequestDTO request = new UrlRequestDTO("https://www.redirect-test.com");
        MvcResult createResult = mockMvc.perform(post("/api/url/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String shortUrl = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("shortUrl").asText();
        // Extrai apenas o shortCode da shortUrl, ex.: "http://localhost:8080/b" → "b"
        String shortCode = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        // Testa o redirecionamento
        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "https://www.redirect-test.com"));
    }

    @Test
    @DisplayName("GET /{shortCode} com código inexistente deve retornar 404")
    void redirect_codigoInexistente_deveRetornar404() throws Exception {
        // "zzzzzzz" é um shortCode que certamente não existe
        mockMvc.perform(get("/zzzzzzz"))
                .andExpect(status().isNotFound());
    }
}

