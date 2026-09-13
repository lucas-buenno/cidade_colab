# API Cidade Colab

## POST /auth

Público (sem JWT). Login com `username` + `password`. O front **não** chama o Keycloak; o backend troca as credenciais e devolve só o `accessToken`.

```json
{ "username": "lucas", "password": "********" }
```

Resposta 200: `{ "accessToken": "eyJ..." }`.

---

## POST /auth/forgot-password

Público (sem JWT). Fluxo de “esqueci a senha” da SPA (`/esqueci-senha`). O front **não** chama o Keycloak.

O backend gera um token opaco, guarda só o hash, e envia e-mail com link **da SPA** (não há tela do Keycloak). Depois: `/redefinir-senha` → `POST /auth/reset-password` → `POST /auth`.

Body:

```json
{ "email": "voce@email.com" }
```

Comportamento:

1. E-mail malformado ou ausente → **400** com texto `E-mail inválido`.
2. Caso contrário, **sempre 202 Accepted** com o mesmo body, exista ou não o usuário (não há 404, username nem token):

```json
{ "message": "Se o e-mail existir, enviaremos instruções." }
```

3. Se existir no Keycloak: invalida tokens anteriores daquele usuário, persiste hash + TTL (**900 s / 15 min**), envia e-mail SMTP com  
   `{PASSWORD_RESET_APP_URL}?token=...` (default `http://localhost:5173/redefinir-senha?token=`).
4. Só Mongo, sem Keycloak: **não** envia e-mail. Log de inconsistência. Client ainda 202.
5. Falha ao buscar no Keycloak ou ao enviar e-mail: log interno (hash do e-mail) e **202**.
6. Rate limit: **3 / 15 min** por IP e por e-mail → **429** sem body.
7. Logs não gravam o e-mail em claro. O e-mail **não** contém senha.

---

## POST /auth/reset-password

Público (sem JWT). A SPA lê `token` da query e envia no body. **Não** devolve `accessToken`.

```json
{ "token": "opaco-do-email", "password": "novaSenha8" }
```

- **204** senha atualizada no Keycloak (`PUT .../users/{id}/reset-password`, `temporary: false`) e token marcado como usado.
- **400** `Link inválido ou expirado` — token ausente, desconhecido, expirado ou já usado.
- **400** `A senha deve conter ao menos 8 caracteres`.
- **429** rate limit por IP (mesmo teto 3 / 15 min).
- Token de uso único. Pedido novo de forgot invalida o anterior.
- Se o Keycloak Admin falhar **depois** de validar o token, o token **não** é consumido (retry possível). **5xx** ao client.

### Configuração (ambiente)

| Variável / property | Uso |
| --- | --- |
| `KEYCLOAK_BASE_URL` / `keycloak.base-url` | URL do Keycloak (Admin + token) |
| `KEYCLOAK_REALM` / `keycloak.realm` | Realm |
| `KEYCLOAK_ADMIN_CLIENT_ID` / `KEYCLOAK_ADMIN_CLIENT_SECRET` | Client de serviço já usado em `POST /v1/users` (`manage-users`) |
| `PASSWORD_RESET_APP_URL` / `password-reset.app-url` | Base do link da SPA (default `http://localhost:5173/redefinir-senha`) |
| `PASSWORD_RESET_TTL_SECONDS` | Validade do token (default `900`) |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_FROM` | SMTP (dev: Mailpit `localhost:1025`) |
| `FORGOT_PASSWORD_RATE_LIMIT_MAX` / `FORGOT_PASSWORD_RATE_LIMIT_WINDOW_SECONDS` | Default `3` / `900` |

A API Java usa `MAIL_HOST=localhost` quando roda no host. O Keycloak no Docker, se ainda tiver SMTP de realm, é outro assunto — **este fluxo não usa e-mail do Keycloak**.

---

Convenções gerais:

- Base path dos recursos: `/v1`
- Coordenadas são **sempre GeoJSON**: `[longitude, latitude]` (WGS84)
- `location` persistido e devolvido como Point GeoJSON:

```json
{
  "name": "Praça Central",
  "reference": "em frente à igreja",
  "type": "Point",
  "address": {
    "street": "Rua A",
    "number": "10",
    "neighborhood": "Centro",
    "city": "Divinópolis",
    "state": "MG",
    "postalCode": "35500000"
  },
  "coordinates": [-44.88, -20.14]
}
```

Índice MongoDB: `2dsphere` em `colabs.location` (o documento precisa ter `type: "Point"` e `coordinates: [lng, lat]`; não use array solto).

O exemplo antigo `(-23.55, -46.63)` estava em ordem `[latitude, longitude]` e **não** deve ser enviado assim. O equivalente GeoJSON é `[-46.63, -23.55]`. No create, coordenadas no padrão histórico brasileiro `[lat, lng]` ainda são normalizadas para `[lng, lat]` na persistência.

Erros de validação da busca geográfica devolvem **400** com o corpo em texto (mensagem clara), sem envelope extra. Lista vazia é **200**.

---

## GET /v1/feed

Público (token opcional).

Feed cronológico dos colabs, paginação cursor por `_id` (ObjectId em Base64 URL).

Query:

| Param | Tipo | Default | Notas |
| --- | --- | --- | --- |
| pageToken | string | — | cursor da página anterior |
| size | int | 20 | inválido → 20; máximo 50 |

Resposta 200:

```json
{
  "items": [ { "...ColabResponse" } ],
  "nextPageToken": "string ou null"
}
```

Este endpoint **não** filtra por geolocalização.

---

## GET /v1/colab/{id}

Público (token opcional). Detalhe de um colab. Com token, `supportedByMe` reflete o usuário autenticado; sem token, `false`.

---

## GET /v1/categories

Público.

Query: `includeInactive` (default `false`).

---

## POST /v1/colab/prepare

Autenticado. Upload de imagem (multipart `file`). Devolve `imageKey`, `colabId`, `url`.

---

## POST /v1/colab/create

Autenticado (`ROLE_COLLABORATOR` + `PERM_colabs:create`).

`location.coordinates` deve ser GeoJSON `[lng, lat]`. O backend persiste `type: "Point"` e normaliza a ordem se receber o padrão legado `[lat, lng]` do Brasil.

---

## PUT /v1/colab/support/{colabId}

Autenticado (`ROLE_COLLABORATOR` + `PERM_colabs:support`). Toggle de apoio.

---

## GET /v1/colabs/search

Público (token opcional). Busca da tela `/buscar` (lista + mapa). Um único endpoint combina três filtros independentes:

- **Perto de mim:** `lat` + `lng` (GPS no front; o backend **não** chama Nominatim)
- **Tags:** `category` repetível (OR entre slugs)
- **Texto:** `q` (cidade, bairro, rua, título, descrição, nome do local)

Não devolve o feed cronológico: é obrigatório enviar ao menos um filtro (`lat`+`lng`, `bbox`, `category` ou `q`).

Query:

| Param | Tipo | Default | Obrigatório | Notas |
| --- | --- | --- | --- | --- |
| lat | double | — | só no modo "Perto de mim" (junto com `lng`) | WGS84 |
| lng | double | — | só no modo "Perto de mim" (junto com `lat`) | WGS84 |
| radiusKm | double | 3 | não | min 0.2, max 10. Fora do intervalo → 400. Ignorado se `bbox` for válido |
| bbox | string | — | não (modo mapa) | `minLng,minLat,maxLng,maxLat`. Diagonal máxima **15 km** (400 se maior) |
| category | string (repetível) | — | não | slug de categoria **ativa**. Slug inexistente/inativa → 400. Várias tags = OR (`categories` in) |
| q | string | — | não | case-insensitive em `title`, `description`, `location.name`, `location.address.neighborhood`, `city`, `street` (máx. 200) |
| pageToken | string | — | não | ver formato abaixo |
| size | int | 20 | não | inválido (`<= 0`) → 20; máximo 50 |

Regras:

1. `bbox` válido → `$geoWithin` no polígono do retângulo. `lat` / `lng` / `radiusKm` são ignorados. Distância = metros até o **centro do bbox**. Ordenação por distância crescente.
2. Senão, `lat`+`lng` → `$geoNear` / `$maxDistance` = `radiusKm` em metros. Centro `[lng, lat]`. Distância crescente a partir do ponto.
3. Senão, `category` e/ou `q` → busca **sem** geolocalização. Ordenação por `createdAt` decrescente. `distanceMeters` vem `null`.
4. Sem geo, sem `category` e sem `q` → **400**.
5. Os eixos combinam com AND (geo ∩ tags ∩ texto). Tags entre si: OR.
6. Apenas `status = CREATED`. Se existir campo `deleted`, documentos com `deleted: true` são excluídos.
7. `nextPageToken` é `null` no fim. Sem resultados → **200** com `items: []`.
8. Sem token, `supportedByMe` é `false`. Com token válido, o campo segue o usuário autenticado (mesmo formato do `ColabResponse` do feed).

### pageToken

Não reutiliza o cursor do feed (ObjectId em Base64). Formato interno, depois Base64 URL sem padding.

Modo geo (`lat`/`lng` ou `bbox`):

```
geo:v1:{distanceMeters}:{colabId}
```

Exemplo: distância 312 m, id `colab-1` → payload `geo:v1:312:colab-1`. Paginação por distância crescente (empate por `_id`).

Modo filtro (só `q` e/ou `category`):

```
filter:v1:{createdAtEpochMillis}:{colabId}
```

Paginação por `createdAt` decrescente (empate por `_id`). Token geo neste modo (e vice-versa) → 400. Token do feed → 400. Token inválido → 400.

### Resposta 200

Espelha `ColabResponse` de `GET /v1/colab/{id}` e adiciona `distanceMeters` (número no modo geo; `null` no modo filtro):

```json
{
  "items": [
    {
      "id": "colab-1",
      "userId": "user-1",
      "username": "lucas",
      "title": "Buraco na via",
      "description": "...",
      "categories": [],
      "status": "CREATED",
      "supportCount": 3,
      "supportedByMe": false,
      "location": {
        "name": "Praça Central",
        "reference": null,
        "type": "Point",
        "address": {
          "street": "Rua A",
          "number": "10",
          "neighborhood": "Centro",
          "city": "Divinópolis",
          "state": "MG",
          "postalCode": "35500000"
        },
        "coordinates": [-44.88, -20.14]
      },
      "createdAt": "2026-01-01T00:00:00Z",
      "updatedAt": "2026-01-01T00:00:00Z",
      "imageUrl": "https://...",
      "distanceMeters": 312
    }
  ],
  "nextPageToken": null
}
```

### Erros 400

- `lat` e `lng` incompletos (só um dos dois)
- sem nenhum filtro (nem geo, nem category, nem q)
- bbox malformado, invertido ou com diagonal > 15 km
- `radiusKm` < 0.2 ou > 10
- `category` com slug inexistente ou inativa
- `pageToken` inválido
- `q` com mais de 200 caracteres

Exemplos:

```
GET /v1/colabs/search?lat=-20.14&lng=-44.88&radiusKm=3
GET /v1/colabs/search?bbox=-44.90,-20.16,-44.86,-20.12&category=pothole&q=buraco
GET /v1/colabs/search?lat=-20.14&lng=-44.88&category=pothole&category=illegal-dumping
GET /v1/colabs/search?q=Centro
GET /v1/colabs/search?category=pothole&category=flooding
GET /v1/colabs/search?q=Divinópolis&category=pothole
```
