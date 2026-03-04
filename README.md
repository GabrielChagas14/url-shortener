# 🔗 URL Shortener API

Uma API REST para encurtamento de URLs construída com **Spring Boot**, **PostgreSQL** e **Flyway**. Gera códigos curtos únicos usando codificação Base62 e suporta redirecionamento automático com validade de 7 dias.

---

## 🚀 Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.2 |
| Spring Data JPA | — |
| Spring Validation | — |
| Flyway | — |
| PostgreSQL | latest |
| Lombok | — |
| Docker / Docker Compose | — |

---

## 📁 Estrutura do Projeto

```
src/
└── main/
    ├── java/com/api/shortener/
    │   ├── ShortenerApplication.java       # Ponto de entrada da aplicação
    │   ├── config/
    │   │   └── CorsConfig.java             # Configuração de CORS (permite todas as origens)
    │   ├── controller/
    │   │   └── UrlController.java          # Endpoints REST
    │   ├── domain/url/
    │   │   ├── Url.java                    # Entidade JPA
    │   │   ├── UrlRequestDTO.java          # DTO de entrada
    │   │   └── UrlResponseDTO.java         # DTO de saída
    │   ├── repositories/
    │   │   └── UrlRepository.java          # Repositório JPA
    │   └── service/
    │       └── UrlService.java             # Lógica de negócio (Base62, expiração)
    └── resources/
        ├── application.properties
        └── db/migration/
            ├── V1__create-url-table.sql    # Criação da tabela url
            └── V2__alter-url-table.sql     # Renomeia coluna url_original → original_url
```

---

## ⚙️ Configuração

### Pré-requisitos

- [Java 21+](https://adoptium.net/)
- [Docker e Docker Compose](https://docs.docker.com/get-docker/)

### Variáveis / `application.properties`

```properties
spring.application.name=shortener
app.base-url=http://localhost:8080/
```

A propriedade `app.base-url` define o prefixo da URL encurtada retornada pela API.

---

## 🐳 Executando com Docker Compose

```bash
docker compose up -d
```

| Serviço | Porta |
|---|---|
| PostgreSQL | 5432 |
| Adminer (UI do banco) | http://localhost:8081 |

> **Credenciais:** usuário `postgres`, senha `postgres`, banco `shortener`.

---

## ▶️ Executando a Aplicação

```bash
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`. As migrações do Flyway são executadas automaticamente na inicialização.

---

## 📡 Endpoints da API

### `POST /api/url/create` — Criar URL curta

**Request Body:**
```json
{
  "originalUrl": "https://www.exemplo.com/pagina/muito/longa"
}
```

**Response `200 OK`:**
```json
{
  "originalUrl": "https://www.exemplo.com/pagina/muito/longa",
  "shortUrl": "http://localhost:8080/b",
  "valid": "2025-01-15T12:00:00"
}
```

---

### `GET /{shortCode}` — Redirecionar para URL original

Redireciona para a URL original correspondente ao código curto.

**Exemplo:**
```
GET http://localhost:8080/b
→ Redireciona para: https://www.exemplo.com/pagina/muito/longa
```

| Status | Descrição |
|---|---|
| `302 Found` | Redirecionamento bem-sucedido |
| `404 Not Found` | Código curto não encontrado |
| `410 Gone` | URL expirada (válida por 7 dias) |

---

## 🧠 Como Funciona

1. A URL original é salva no banco e recebe um `id` auto-incrementado.
2. O `id` é convertido para **Base62** (`[a-z][A-Z][0-9]`), gerando um código curto compacto.
3. No redirecionamento, o código é convertido de volta para o `id` e a URL é recuperada do banco.
4. URLs têm validade de **7 dias** a partir da criação.

---

## 🗄️ Banco de Dados

Tabela `url` gerenciada pelo Flyway:

```sql
CREATE TABLE url (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    original_url TEXT      NOT NULL,
    valid        TIMESTAMP NOT NULL
);
```

---

## 🌐 CORS

A API permite requisições de **qualquer origem** (`*`), com todos os métodos e headers. Ideal para desenvolvimento — ajuste para valores específicos em produção.
