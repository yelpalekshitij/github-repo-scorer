# 📘 github-repo-scorer

## Overview
`github-repo-scorer` is a backend service that queries public GitHub repositories based on user-defined criteria (language, creation date) and assigns a **popularity score** using stars, forks, and update recency.  

The service acts as a **proxy/wrapper** around the official GitHub Search API, enforcing rate-limit safety, caching, and clear separation between data fetching and scoring logic.

---

## 🧩 Problem Statement
Build a backend service that:
- Fetches repositories from GitHub based on user-provided filters:
  - **Language**
  - **Earliest created date**
- Assigns a **popularity score** using a custom algorithm based on:
  - Stars
  - Forks
  - Last updated date
- Returns scored repositories efficiently, handling pagination, caching, and GitHub’s rate limits gracefully.

---

## 🚀 Tech Stack

| Technology | Purpose |
|-------------|----------|
| **Java 21** | Modern language features, records, and improved concurrency APIs |
| **Spring Boot 3.x** | Simplified REST API development, DI, and configuration |
| **Spring Cache (Caffeine)** | Caching repository responses for rate-limit efficiency |
| **JUnit 5 + Mockito** | Unit testing and integration coverage |
| **Docker** | Containerized deployment |
| *(Optional)* **Testcontainers** | For future integration testing setup |

---

## ⚙️ Architecture & Design Decisions

### 1. Project Type
Chose **Spring Boot** over plain Java:
- Easier to expose clean REST endpoints.
- Simple dependency injection and configuration handling.
- Easier testability and scalability if extended later.

### 2. Service Responsibility
This is a **proxy / wrapper service** around GitHub’s Search API:
- It does **not store** data persistently (to respect GitHub’s data freshness).
- It can **cache** responses in memory for a configurable duration (default: 15 min).

### 3. GitHub API Integration
- Endpoint: `GET https://api.github.com/search/repositories`
- Parameters:  
  `q=language:<lang>+created:>=<date>&sort=stars&order=desc&page=X&per_page=Y`
- Uses `java.net.http.HttpClient` with headers:
  ```http
  Accept: application/vnd.github+json
  X-GitHub-Api-Version: 2022-11-28
  ```
- Pagination handled safely with limit on total pages (e.g., up to 200 repos). Because hard limit `per_page` is 100 and
  max allowed pages are 10. 

### 4. Scoring Algorithm
Defined in `PopularityScoreCalculator`:

```
score = (stars * 0.6) + (forks * 0.3) / Square root of (recencyFactor in days)
```

Where:
```
recencyFactor = Max of (1, No. of days from the last updated day till now)
```

Weights are configurable in `application.yml`.

### 5. Caching
Implemented via `CaffeineCache`:
- Key: combination of `language` and `earliestDate` parameters.
- TTL: 15 minutes (configurable).
- Reduces repeated GitHub API calls.

### 6. Resilience & Error Handling
- Gracefully handles:
  - 403 Rate Limit → requesting limited data.
  - Network failures → fallback to previous cache.
- Logs meaningful messages (no stack traces in normal flow).

### 7. Performance
- Uses `Future`’s API I/O for parallel page fetching **within rate limits**.

---

## 🧠 Example Query Flow

### Request:
```
GET /api/repositories/search?language=java&earliestDate=2025-10-02
```

### Response (simplified):
```json
[
  {
    "name": "RoadWeaver",
    "language": "Java",
    "stars": 234,
    "forks": 20,
    "lastUpdatedAt": "2025-11-01T18:00:40Z",
    "popularityScore": 146.4
  }
]
```

---

## 📈 Scalability Considerations
- **Pagination Limit**: GitHub caps search to first 1000 results.  
  → Service handles this and clearly communicates the cap to the client.
- **Future-ready**: Can easily add background fetcher + database (Postgres/Redis) if persistent ranking required.
- **Extensible**: Scoring algorithm can later include watchers, open issues, or PR count.

---

## 🧪 Testing
- **Unit Tests** for:
  - Score calculation.
  - Query construction logic.
- **Integration Tests** (with mock GitHub API) to verify:
  - Pagination handling.
  - Controller layer.
- **Coverage Goal**: >80% across business logic classes.

---

## 🐳 Docker
**Build:**
```bash
./gradlew build
or
docker build -t github-repo-scorer .
```
Also integrated with Github actions that executes build for all branches and PRs. 

**Run:**
```bash
java -jar ./build/libs/github-repo-scorer-0.0.1-SNAPSHOT.jar
or
docker run -p 8080:8080 github-repo-scorer
```
THe docker image is available at
```shell
docker pull ghcr.io/yelpalekshitij/github-repo-scorer:latest
or
docker pull kshitijdocker123/github-repo-scorer:latest
```

---

## ⚙️ Configuration

`application.yml`
```yaml
app-config:
  github:
    api-url: https://api.github.com
    per-page: 100 # Hard limit by Github
    max-pages: 2 # Max hard limit by Github is 10
    parallelism: 10
  popularity-score-weights:
    stars: 0.6
    forks: 0.3
```

---

## 🔐 Authentication
Github supports **GitHub personal access token** so that authenticated requests get **5000 req/hour** instead of **60 req/hour**.

---

## 🧭 Future Improvements
- Add persistent storage for historical scoring.
- Introduce async scheduling for periodic refresh.
- Add OpenAPI / Swagger docs.
- Enhance scoring with engagement metrics (issues, watchers).

---

## 🧑‍💻 Author Notes
This project demonstrates:
- Scalable API integration design.
- Proper error handling and resilience.
- Clean modular structure (`controller → service → client → model`).
- Testable, configurable, and extensible code.

---

## 📜 License
MIT License © 2025
