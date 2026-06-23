# Tahadaw — Intelligent Gift Planning & Gift Card Platform

**Tahadaw** is a Spring Boot backend that helps people plan the perfect gift. It builds rich recipient
profiles, asks smart required and AI-generated follow-up questions, recommends gift ideas, searches real
products, generates heartfelt gift messages and premium gift cards (with QR codes), runs group-gift
voting, tracks gift history, and sends reminders — all powered by OpenAI and a Moyasar payment flow for
premium features.

> **نبذة بالعربية**
> **تهدّاو** هو نظام خلفي (Backend) مبني على Spring Boot يساعد المستخدم على تخطيط الهدية المثالية.
> ينشئ ملفًا تعريفيًا غنيًا عن المُهدى إليه، ويطرح أسئلة إلزامية وأسئلة ذكية مولّدة بالذكاء الاصطناعي،
> ثم يقترح أفكار هدايا ويبحث عن منتجات حقيقية، ويولّد رسائل تهنئة وبطاقات هدايا مميّزة مع رمز QR،
> ويدير التصويت على الهدايا الجماعية، ويتتبّع سجل الهدايا، ويرسل التذكيرات — مدعومًا بالذكاء الاصطناعي
> من OpenAI ونظام دفع Moyasar للميزات المدفوعة (Premium).

---

## Live API (AWS)

```
http://tahadaw-alb-278802991.eu-central-1.elb.amazonaws.com/api/v1
```

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [External Integrations](#external-integrations)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [UI Design (Figma)](#ui-design-figma)
- [System / Use Case Diagram](#system--use-case-diagram)
- [Entity Relationship Diagram](#entity-relationship-diagram)
- [Team Contributions](#team-contributions)
- [API Base URL](#api-base-url)
- [Postman API Documentation](#postman-api-documentation)

---

## Overview

Tahadaw uses HTTP Basic authentication; the signed-in user comes from the Spring Security principal.

| Role | Description |
|------|-------------|
| **User** | Manages recipients and gift plans, answers questions, gets AI recommendations, searches products, generates messages and gift cards, runs group gifts, tracks history, and sets reminders |
| **Admin** | Manages the required-question catalog and raw AI question/answer tooling |

Main feature areas:

- **Gift Journey** — recipient profiles → gift plan → required & AI questions → AI gift ideas → real product search → selection → gift message.
- **Premium Features** — premium gift cards (image + QR), surprise plans, unlocked via a Moyasar one-time payment.
- **Group Gifting** — create a group gift, add/AI-generate options, send invites, public token-based voting, results.
- **Engagement** — gift history & spending insights, reminders (email / WhatsApp / in-app), notifications, and an aggregated dashboard.

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 4.1.0 |
| Web | Spring MVC + Spring WebFlux (`RestClient`) |
| Security | Spring Security (HTTP Basic auth, BCrypt) |
| Persistence | Spring Data JPA / Hibernate |
| Database | MySQL (`mysql-connector-j`) |
| Validation | Jakarta Bean Validation |
| Object Mapping | ModelMapper |
| JSON | Jackson |
| Email | Spring Mail (SMTP) |
| PDF | OpenPDF (LibrePDF) |
| QR Codes | Google ZXing (`core` + `javase`) |
| Scheduling | Spring `@Scheduled` (reminder jobs) |
| Boilerplate | Lombok |
| Build | Maven (`mvnw` wrapper) |
| Containerization | Docker |
| Testing | Spring Boot Test, Spring Security Test, JPA Test |

---

## External Integrations

| Integration | Purpose |
|-------------|---------|
| **OpenAI** (`gpt-4o-mini`) | Required/AI follow-up questions, gift idea recommendations, gift messages, surprise plans, group-gift options, gift quality checks |
| **Moyasar** | Premium one-time payment gateway (sandbox) with 3‑D Secure browser callback + webhook |
| **SearchAPI.io** | Real product search (Google Shopping) for selected gift ideas |
| **Twilio** | WhatsApp reminder notifications |
| **SMTP Email** | Gift card delivery, payment receipts, group-gift invites |

---

## Prerequisites

- **Java 17+**
- **Maven 3.9+** (or use the included `./mvnw` wrapper)
- **MySQL 8+** with a database named `tahadaw`
- Optional but recommended for full functionality:
  - OpenAI API key
  - Moyasar (test) secret key
  - SearchAPI.io key
  - SMTP credentials (Gmail App Password or similar)
  - Twilio credentials (WhatsApp)

---

## Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
cd Tahadaw
```

### 2. Create the database

```sql
CREATE DATABASE tahadaw;
```

### 3. Configure local secrets

Create `src/main/resources/application-local.properties` (gitignored) and fill in your values:

```properties
spring.datasource.password=your_mysql_password
openai.api.key=sk-your-key-here
searchapi.api.key=your-searchapi-key
moyasar.api-key=your-moyasar-test-key
```

See [Configuration](#configuration) for the full list of optional settings.

### 4. Run the application

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API starts on **http://localhost:8080** by default.

### 5. Verify

```bash
curl -u <username>:<password> http://localhost:8080/api/v1/recipients/get
```

---

## Configuration

| Property | Description | Required |
|----------|-------------|----------|
| `spring.datasource.password` | MySQL password | Yes |
| `openai.api.key` | OpenAI API key for AI features | Yes (for AI endpoints) |
| `ai.model` | OpenAI model (default: `gpt-4o-mini`) | No |
| `searchapi.api.key` | SearchAPI.io key for product search | For product search |
| `moyasar.api-key` | Moyasar secret key (sandbox) | For premium payments |
| `moyasar.callback-url` | 3‑D Secure browser redirect target | No (has default) |
| `premium.amount-minor` / `premium.currency` | Premium price (default `9900` / `SAR`) | No |
| `spring.mail.*` | SMTP settings for email notifications | For email features |
| `twilio.account-sid` / `twilio.auth-token` / `twilio.from` | WhatsApp notifications | For WhatsApp features |
| `spring.jpa.hibernate.ddl-auto` | Schema mode (default: `create-drop`) | No |

Non-secret defaults live in `src/main/resources/application.properties`. Secrets belong in
`application-local.properties` (gitignored).

---

## Project Structure

```
src/main/java/org/example/tahadaw/
├── AI/              # OpenAI integration (AiService, parsers)
├── Api/             # Shared API types (ApiResponse, ApiException)
├── Config/          # Spring Security configuration
├── Controller/      # REST endpoints
├── DTO/IN           # Request bodies
├── DTO/OUT          # Response bodies
├── Model/           # JPA entities
├── Repository/      # Spring Data repositories
└── Service/         # Business logic + integrations (Moyasar, Twilio, SearchAPI, Email, QR, PDF)

docs/images/         # Architecture & ER diagram assets
postman/             # Full-system Postman collection (flows grouped by developer)
```

---

## UI Design (Figma)

Interactive UI mockups for the Tahadaw platform (Arabic RTL dashboard and user flows):

**[Tahadaw UI — Figma (تهادوا)](https://www.figma.com/design/1kn0xnKDmQyf60eT7sz27N/%D8%AA%D9%87%D8%A7%D8%AF%D9%88%D8%A7?node-id=0-1&t=PU0KDFWhmlcEtHAu-1)**

---

## System / Use Case Diagram

![Tahadaw System & Use Case Diagram](docs/images/tahadaw-use-case-diagram.png)

---

## Entity Relationship Diagram

![Tahadaw Entity Relationship Diagram](docs/images/tahadaw-erd.png)

---

## Team Contributions

### Saud Shafie

The following 34 endpoints make up Saud's flows (Premium Payment, Gift Messages, Gift Cards, Surprise
Plan, Gift History) plus the cross-cutting Dashboard and Recipient Insights.

**Premium Payment (Moyasar)**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/payments/premium` | Start a one-time premium payment |
| `GET` | `/api/v1/payments/my` | List my payments |
| `GET` | `/api/v1/premium/status` | Get my premium status |
| `POST` | `/api/v1/payments/webhook/moyasar` | Moyasar payment webhook (public) |
| `GET` | `/api/v1/payments/moyasar-status/{id}` | Check a Moyasar payment status |

**Gift Messages**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/gift-messages/generate` | AI-generate a standalone gift message |
| `POST` | `/api/v1/gift-messages/generate-from-plan/{giftPlanId}` | AI message from a gift plan |
| `POST` | `/api/v1/gift-messages/manual` | Create a manual gift message |
| `PUT` | `/api/v1/gift-messages/{messageId}` | Update a gift message |
| `GET` | `/api/v1/gift-messages/my` | List my gift messages |
| `GET` | `/api/v1/gift-messages/{messageId}` | Get one gift message |

**Gift Card (Premium)**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/gift-cards` | Create a gift card |
| `GET` | `/api/v1/gift-cards/my` | List my gift cards |
| `GET` | `/api/v1/gift-cards/{giftCardId}` | Get one gift card |
| `PUT` | `/api/v1/gift-cards/{giftCardId}` | Update a gift card |
| `POST` | `/api/v1/gift-cards/{giftCardId}/regenerate` | Regenerate the card image |
| `GET` | `/api/v1/gift-cards/{giftCardId}/image` | View card as PNG |
| `POST` | `/api/v1/gift-cards/{giftCardId}/send-email` | Email the gift card |
| `DELETE` | `/api/v1/gift-cards/{giftCardId}` | Delete a gift card |
| `GET` | `/api/v1/gift-cards/{giftCardId}/download` | Download card as PDF (or PNG) |

**Surprise Plan (Premium)**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/gift-plans/{giftPlanId}/surprise-plan/generate` | Generate AI surprise plan |
| `POST` | `/api/v1/gift-plans/{giftPlanId}/surprise-plan/regenerate` | Regenerate surprise plan |
| `PUT` | `/api/v1/gift-plans/{giftPlanId}/surprise-plan` | Update surprise plan |
| `DELETE` | `/api/v1/gift-plans/{giftPlanId}/surprise-plan` | Delete surprise plan |
| `GET` | `/api/v1/gift-plans/{giftPlanId}/surprise-plan` | Get surprise plan |

**Gift History**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/gift-history/from-product/{selectedProductId}` | Log a gift from a selected product |
| `PUT` | `/api/v1/gift-history/from-product/{selectedProductId}` | Edit a gift history log |
| `DELETE` | `/api/v1/gift-history/from-product/{selectedProductId}` | Delete a gift history log |
| `GET` | `/api/v1/gift-history/from-product/{selectedProductId}` | Get gift history for a product |
| `GET` | `/api/v1/gift-history/my` | List my gift history |
| `GET` | `/api/v1/gift-history/summary` | Gift history summary |
| `GET` | `/api/v1/gift-history/spending-stats` | Spending stats with date filters |

**Dashboard & Insights**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/dashboard` | Aggregated home-screen dashboard |
| `GET` | `/api/v1/recipients/{recipientId}/insights` | Per-recipient gifting insights |

#### Example requests (Saud Shafie endpoints)

**Spending stats (date-filtered)**

```http
GET /api/v1/gift-history/spending-stats?from=2026-01-01&to=2026-12-31
```

**Download a gift card as PDF**

```http
GET /api/v1/gift-cards/1/download?format=pdf
```

**Aggregated dashboard**

```http
GET /api/v1/dashboard
```

---

## API Base URL

```
http://localhost:8080/api/v1
```

All endpoints return JSON. Successful mutations typically respond with an `ApiResponse` message or the
created/updated DTO. Authentication is HTTP Basic; the user is taken from the Spring Security principal.

Errors are returned with HTTP 4xx/5xx and a message body (`spring.web.error.include-message=always`).

---

## Postman API Documentation

**[Tahadaw — Full System Flows (Postman API Docs)](https://documenter.getpostman.com/view/54224474/2sBXwwm7kY#1b2a0212-71da-4b20-95f0-bf480fa27d16)**

**Import the collection locally:**

| Resource | Location |
|----------|----------|
| Collection JSON | [`postman/Tahadaw-Full-System-Flows.postman_collection.json`](postman/Tahadaw-Full-System-Flows.postman_collection.json) |
| Regenerate script | [`postman/build-collection.js`](postman/build-collection.js) |

The collection includes end-to-end flows grouped by developer (Bayan, Shahad, Saud) plus an **Extra**
folder for out-of-flow endpoints (dashboard, spending stats, recipient insights, gift card PDF download).

---

## License

Capstone project — see repository for license details.
