# Tahadaw — Team Flow Documentation

**Intelligent Gift Planning & Gift Card Platform**  
Prepared for team implementation · Backend API design

---

## Table of Contents

1. [Platform Overview](#1-platform-overview)
2. [Roles & Rules](#2-roles--rules)
3. [Auth & Security — Added Later (Phase 2)](#3-auth--security--added-later-phase-2)
4. [Three Parallel Parts (No Waiting)](#4-three-parallel-parts-no-waiting)
5. [Part 1 — Platform & Social](#5-part-1--platform--social)
6. [Part 2 — Gift Journey: Plan → Product](#6-part-2--gift-journey-plan--product)
7. [Part 3 — Gift Journey: Message → Premium → Card](#7-part-3--gift-journey-message--premium--card)
8. [Seed Data Strategy (How Nobody Waits)](#8-seed-data-strategy-how-nobody-waits)
9. [Gift Plan Status Lifecycle](#9-gift-plan-status-lifecycle)
10. [External APIs & Services](#10-external-apis--services)
11. [Integration Phase (After All Parts)](#11-integration-phase-after-all-parts)
12. [Current Implementation Status](#12-current-implementation-status)

---

## 1. Platform Overview

Tahadaw is **not an online store**. It is an AI-assisted gift planning backend that helps users move from uncertainty to a selected gift using:

- Registered **recipient profiles** (memory of people)
- **Required questions** (admin-defined) + **AI follow-up questions**
- **AI gift ideas** (not products initially)
- **Real product search** only after the user picks one idea
- **Free gift message** generation
- **Gift quality check** (standalone, no gift plan required)
- **Gift history** (avoids repeated gifts in future AI runs)
- **One-time premium payment** → unlocks **Surprise Plan** and **Gift Card** only
- **Group gift voting** (no in-app payments)
- **Reminders** via email / WhatsApp / in-app notification

**Core value:** remember people, understand occasions, avoid weak or repeated gifts, and turn a selected idea into a real gift plan.

**Base URL (local):** `http://localhost:8080/api/v1`

---

## 2. Roles & Rules

| Role | Description | Access |
|------|-------------|--------|
| **USER** | Registered account owner | All user flows (auth added in Phase 2) |
| **ADMIN** | System manager | Required questions, users, payments report |
| **GUEST** | External group-gift voter | Vote once via secure invite token — no account |

### Global Business Rules

| Rule | Detail |
|------|--------|
| Premium model | **One-time payment** — not subscription |
| Premium unlocks | **Surprise Plan** + **Gift Card** only |
| Free forever | Recipients, gift plans, questions, AI recommendations, product search, messages, quality check, group voting, reminders |
| Recipient first | A **recipient must exist** before any AI recommendation flow |
| Product search timing | **SearchAPI.io** only after one AI gift idea is selected |
| Product storage | **Do not save** every search result — only the **selected** product |
| Group gifts | **No payments** in the app — voting, invites, responsible person only |
| Gift card storage | QR + gift card images as **LONGBLOB** bytes in MySQL |
| AI architecture | One central `AiService.ask(String prompt)` for all AI features |

---

## 3. Auth & Security — Added Later (Phase 2)

**Auth is NOT part of the three parallel parts.** It will be added after all flows work.

### What the team uses now (Phase 1)

Until JWT / Spring Security is wired:

| Temporary approach | Detail |
|--------------------|--------|
| User identity | Pass `userId` as query param or request body field |
| Admin actions | Pass `adminUserId` or hardcode admin user `id = 1` in dev |
| Public group vote | Token in URL — no user id needed |
| Security config | Current `permitAll()` stays until Phase 2 |

### Flow 1 — Register & Login (Phase 2 only)

Implement **after** Parts 1–3 are done and integrated.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/auth/register` | Create account |
| `POST` | `/api/v1/auth/login` | Login → JWT |
| `GET` | `/api/v1/auth/me` | Current user from token |

**Phase 2 tasks:**
- BCrypt password encoding
- JWT filter replaces `userId` params
- Role-based access (`USER` vs `ADMIN`)
- Remove temporary `userId` from request bodies where possible

> **Do not block any part on auth.** Every flow must be buildable and testable with a seeded `userId`.

---

## 4. Three Parallel Parts (No Waiting)

All work is split so **three developers can start on day one** without waiting for each other.

### Principles

| Rule | Why |
|------|-----|
| **No cross-part API dependency** | Dev B never needs Dev A's endpoints running to test their code |
| **Seed data per part** | Each part has its own SQL seed file with fake users, recipients, gift plans |
| **Shared contract only** | Entities, enums, repos already exist — that's the shared agreement |
| **Auth last** | Nobody waits for JWT |
| **Equal effort** | Each part ≈ 5–6 flows + similar mix of CRUD, AI, and integrations |

### Overview

```
┌─────────────────────────┐  ┌─────────────────────────┐  ┌─────────────────────────┐
│  PART 1 — Dev A         │  │  PART 2 — Dev B         │  │  PART 3 — Dev C         │
│  Platform & Social      │  │  Gift Journey (start)   │  │  Gift Journey (finish)  │
│                         │  │                         │  │  + Premium              │
│  • Recipients           │  │  • Gift plans           │  │  • Gift messages        │
│  • Gift quality check   │  │  • Required questions   │  │  • Gift history         │
│  • Admin questions      │  │  • AI follow-up Q       │  │  • Premium payment      │
│  • Notifications        │  │  • AI recommendations   │  │  • Surprise plan        │
│  • Reminders            │  │  • Product search       │  │  • Gift card            │
│  • Group gifts          │  │                         │  │                         │
│                         │  │  (sequential inside     │  │  (uses seed plan at     │
│  (fully independent)    │  │   this part only)       │  │   GIFT_IDEA_SELECTED)   │
└─────────────────────────┘  └─────────────────────────┘  └─────────────────────────┘
         ▲                              ▲                              ▲
         │                              │                              │
    seed-part1.sql                 seed-part2.sql                 seed-part3.sql
    (no other part needed)    (no other part needed)         (no other part needed)
```

### Assignment

| Developer | Part | Flows | Waits for |
|-----------|------|-------|-----------|
| **Dev A** | Part 1 | 2, 10, 14, 15, 16, 17 | Nothing |
| **Dev B** | Part 2 | 3, 4, 5, 6, 7 | Nothing (uses `seed-part2.sql`) |
| **Dev C** | Part 3 | 8, 9, 11, 12, 13 | Nothing (uses `seed-part3.sql`) |

---

## 5. Part 1 — Platform & Social

**Owner: Dev A** · **6 flows** · **Fully independent** — no gift plan pipeline needed.

These features stand alone. Dev A never waits for Dev B or Dev C.

---

### Flow 2 — Recipient Profiles

**Purpose:** Store recipient memory for gifts and AI.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/recipients?userId=` | Create |
| `GET` | `/api/v1/recipients?userId=` | List mine |
| `GET` | `/api/v1/recipients/{recipientId}?userId=` | Get one |
| `PUT` | `/api/v1/recipients/{recipientId}?userId=` | Update |
| `DELETE` | `/api/v1/recipients/{recipientId}?userId=` | Delete |

**Create body:**
```json
{
  "name": "Ahmed",
  "relationship": "Brother",
  "age": 22,
  "gender": "Male",
  "interests": "gaming, coffee, cars",
  "hobbies": "FIFA, brewing",
  "favoriteColors": "black, navy",
  "favoriteBrands": "Logitech, Sony",
  "dislikes": "clothes, generic mugs",
  "personalityStyle": "practical",
  "sizeInfo": "L shirt",
  "notes": "Prefers useful gifts",
  "consentAcknowledged": true
}
```

**Entities:** `User` → `Recipient` · **Services:** `RecipientService` · **Status:** 🔴 Not implemented

---

### Flow 10 — Gift Quality Check

**Purpose:** Standalone AI suitability check. **No gift plan needed** — only a recipient.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/recipients/{recipientId}/gift-quality-checks?userId=` | Run check |
| `GET` | `/api/v1/recipients/{recipientId}/gift-quality-checks?userId=` | List |
| `GET` | `/api/v1/gift-quality-checks/{checkId}?userId=` | Get result |

**Request body:**
```json
{
  "giftName": "Designer wallet",
  "giftDescription": "Luxury leather wallet",
  "priceMinor": 45000,
  "occasionType": "BIRTHDAY"
}
```

**Services:** `GiftQualityCheckService`, `AiService` · **Status:** 🔴 Not implemented

---

### Flow 17 — Admin: Required Questions

**Purpose:** Admin manages fixed questions (used later by Part 2 — but Part 2 seed file includes questions too).

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/admin/required-questions` | Create |
| `GET` | `/api/v1/admin/required-questions` | List all |
| `PUT` | `/api/v1/admin/required-questions/{questionId}` | Update |
| `PUT` | `/api/v1/admin/required-questions/{questionId}/disable` | Disable |

**Question types:** `TEXT`, `NUMBER`, `DATE`, `SINGLE_CHOICE`, `MULTI_CHOICE`

**Services:** `AdminService`, `RequiredQuestionService` · **Status:** 🔴 Not implemented

---

### Flow 16 — Notifications

**Purpose:** In-app alerts. Build the service + endpoints now; other parts call `NotificationService.create(...)` later during integration.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/notifications/my?userId=` | List mine |
| `PUT` | `/api/v1/notifications/{notificationId}/read?userId=` | Mark read |

**Dev-only test endpoint (remove in Phase 2):**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/notifications/test?userId=` | Create test notification |

**Types:** `RECOMMENDATIONS_READY`, `PREMIUM_ACTIVATED`, `GIFT_CARD_SENT`, `GROUP_VOTING_CLOSED`, `REMINDER_DUE`

**Services:** `NotificationService` · **Status:** 🔴 Not implemented

---

### Flow 15 — Reminders

**Purpose:** Scheduled email / WhatsApp / in-app reminders.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/reminders?userId=` | Create |
| `GET` | `/api/v1/reminders/my?userId=` | List mine |
| `PUT` | `/api/v1/reminders/{reminderId}?userId=` | Update |
| `DELETE` | `/api/v1/reminders/{reminderId}?userId=` | Cancel |

**Create body:**
```json
{
  "recipientId": 1,
  "reminderDate": "2026-06-18T09:00:00",
  "message": "Ahmed's graduation is in 2 days!",
  "channel": "WHATSAPP"
}
```

**Services:** `ReminderService`, `EmailService`, `WhatsAppService`  
**Background:** `@Scheduled` job sends due reminders · **Status:** 🔴 Not implemented

---

### Flow 14 — Group Gift Voting

**Purpose:** Group votes on gift options. **No gift plan pipeline.** **No in-app payments.**

**Endpoints:**

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/group-gifts?userId=` | userId | Create |
| `GET` | `/api/v1/group-gifts/my?userId=` | userId | List mine |
| `GET` | `/api/v1/group-gifts/{groupGiftId}?userId=` | userId | Details |
| `POST` | `/api/v1/group-gifts/{groupGiftId}/options?userId=` | userId | Add option |
| `POST` | `/api/v1/group-gifts/{groupGiftId}/options/generate-ai?userId=` | userId | AI options |
| `GET` | `/api/v1/group-gifts/{groupGiftId}/options` | — | List options |
| `POST` | `/api/v1/group-gifts/{groupGiftId}/invites?userId=` | userId | Send invites |
| `GET` | `/api/v1/public/group-gifts/vote/{token}` | **Public** | Vote page data |
| `POST` | `/api/v1/public/group-gifts/vote/{token}` | **Public** | Submit vote |
| `PUT` | `/api/v1/group-gifts/{groupGiftId}/close-voting?userId=` | userId | Close voting |
| `GET` | `/api/v1/group-gifts/{groupGiftId}/results?userId=` | userId | Results |

**Public vote body:** `{ "groupGiftOptionId": 2 }`

**Rule:** One vote per invite (unique `invite_id` on `GroupGiftVote`).

**Services:** `GroupGiftService`, `AiService`, `EmailService` · **Status:** 🔴 Not implemented

---

### Part 1 — Checklist

- [ ] Recipient CRUD with `userId` ownership check
- [ ] Gift quality check (standalone AI)
- [ ] Admin required-question CRUD
- [ ] Notification list + mark read + internal create method
- [ ] Reminder CRUD + scheduler + email/WhatsApp send
- [ ] Group gift full flow + public vote token
- [ ] Load `src/main/resources/seed-part1.sql` (see application-local.properties.example)

---

## 6. Part 2 — Gift Journey: Plan → Product

**Owner: Dev B** · **5 flows** · **Sequential inside this part only** — Dev B owns the full chain from gift plan creation to product selection. Does not need Part 1 or Part 3 endpoints.

**Internal order (Dev B implements in this order):**

```
Flow 3 → Flow 4 → Flow 5 → Flow 6 → Flow 7
Gift Plan → Required Q → AI Q → AI Ideas → Product Search
```

---

### Flow 3 — Gift Plan CRUD

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/gift-plans?userId=` | Create |
| `GET` | `/api/v1/gift-plans?userId=` | List mine |
| `GET` | `/api/v1/gift-plans/{giftPlanId}?userId=` | Get one |
| `PUT` | `/api/v1/gift-plans/{giftPlanId}?userId=` | Update |

**Create body:**
```json
{
  "recipientId": 1,
  "occasionType": "GRADUATION",
  "occasionDate": "2026-06-20",
  "budgetMinor": 30000,
  "currency": "SAR",
  "preferredGiftStyle": "PRACTICAL",
  "language": "en"
}
```

**Status on create:** `CREATED` · **Services:** `GiftPlanService` · **Status:** 🔴 Not implemented

---

### Flow 4 — Required Questions & Answers

**Precondition:** Gift plan status = `CREATED`. Active admin questions must exist (seeded in `seed-part2.sql`).

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/gift-plans/{giftPlanId}/required-questions?userId=` | List active questions |
| `POST` | `/api/v1/gift-plans/{giftPlanId}/required-answers?userId=` | Submit all answers |
| `GET` | `/api/v1/gift-plans/{giftPlanId}/required-answers?userId=` | View answers |

**Submit body:**
```json
{
  "answers": [
    { "requiredQuestionId": 1, "answerText": "Very close — we talk daily" },
    { "requiredQuestionId": 2, "answerText": "He prefers practical items" }
  ]
}
```

**Status:** `CREATED` → `REQUIRED_QUESTIONS_ANSWERED` · **Status:** 🔴 Not implemented

---

### Flow 5 — AI Follow-up Questions

**Precondition:** Status = `REQUIRED_QUESTIONS_ANSWERED`.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/gift-plans/{giftPlanId}/ai-questions/generate?userId=` | Generate |
| `GET` | `/api/v1/gift-plans/{giftPlanId}/ai-questions?userId=` | List |
| `POST` | `/api/v1/gift-plans/{giftPlanId}/ai-answers?userId=` | Submit answers |
| `GET` | `/api/v1/gift-plans/{giftPlanId}/ai-answers?userId=` | View answers |

**AI JSON shape:**
```json
{
  "questions": [
    {
      "questionText": "Does he already own a gaming headset?",
      "reasonForQuestion": "Avoid duplicate gaming gear",
      "displayOrder": 1
    }
  ]
}
```

**Status:** → `AI_QUESTIONS_GENERATED` → `AI_QUESTIONS_ANSWERED`  
**Services:** `AiQuestionService`, `AiService` · **Status:** 🔴 Not implemented

---

### Flow 6 — AI Gift Recommendations

**Precondition:** Status = `AI_QUESTIONS_ANSWERED`.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/gift-plans/{giftPlanId}/recommendations/generate?userId=` | Generate ideas |
| `GET` | `/api/v1/gift-plans/{giftPlanId}/recommendations?userId=` | List ideas |
| `PUT` | `/api/v1/recommendations/{recommendationId}/select?userId=` | Select one |

**AI idea fields:** title, category, priceBand, reason, emotionalFit, practicalFit, riskLevel, aiExplanation, **searchKeyword**

**Status:** → `RECOMMENDATIONS_GENERATED` → `GIFT_IDEA_SELECTED`  
**Services:** `GiftRecommendationService`, `AiService` · **Status:** 🔴 Not implemented

---

### Flow 7 — Product Search & Selection

**Precondition:** Status = `GIFT_IDEA_SELECTED`.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/gift-plans/{giftPlanId}/products/search?userId=` | Search (not saved) |
| `POST` | `/api/v1/gift-plans/{giftPlanId}/products/select?userId=` | Save chosen product |
| `GET` | `/api/v1/gift-plans/{giftPlanId}/selected-product?userId=` | Get saved product |

**Select body:**
```json
{
  "title": "Logitech G733 Wireless Headset",
  "priceMinor": 29900,
  "currency": "SAR",
  "imageUrl": "https://...",
  "productUrl": "https://...",
  "sourceName": "Amazon",
  "rating": 4.5
}
```

**Status:** → `PRODUCT_SELECTED`  
**Services:** `ProductSearchService` · **External:** [SearchAPI.io](https://www.searchapi.io/docs/google-shopping)  
**Status:** 🟢 Implemented

---

### Part 2 — Checklist

- [ ] Gift plan CRUD
- [ ] Required answers + status gate
- [ ] AI questions generate + answer
- [ ] AI recommendations generate + select
- [ ] Product search + select — **search/select done**
- [ ] Load `src/main/resources/seed-part2.sql` (user, recipient, admin questions)

---

## 7. Part 3 — Gift Journey: Message → Premium → Card

**Owner: Dev C** · **5 flows** · **Independent during development** — uses `seed-part3.sql` with a gift plan already at `GIFT_IDEA_SELECTED` or `PRODUCT_SELECTED`. Does not need Dev B's endpoints running.

---

### Flow 8 — Free Gift Message Generation

**Purpose:** AI gift message — always free.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/gift-plans/{giftPlanId}/messages/generate?userId=` | Generate |
| `GET` | `/api/v1/gift-plans/{giftPlanId}/messages?userId=` | List |

**Generate body:**
```json
{
  "tone": "warm",
  "language": "ar",
  "dialect": "saudi"
}
```

**Services:** `GiftMessageService`, `AiService` · **Status:** 🔴 Not implemented

---

### Flow 9 — Gift History

**Purpose:** Record gifts so future AI avoids repeats.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/gift-plans/{giftPlanId}/history?userId=` | Save from plan |
| `GET` | `/api/v1/recipients/{recipientId}/gift-history?userId=` | By recipient |
| `GET` | `/api/v1/gift-history/my?userId=` | All mine |
| `PUT` | `/api/v1/gift-history/{historyId}?userId=` | Update rating/notes |

**Update body:**
```json
{
  "wasGifted": true,
  "userRating": 5,
  "notes": "He loved it"
}
```

**Status:** can set gift plan → `COMPLETED` · **Status:** 🔴 Not implemented

---

### Flow 11 — Premium Payment (Moyasar Sandbox)

**Purpose:** One-time test payment unlocks premium. **Independent** — only needs a user row.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/payments/premium` | Pay with test card |
| `GET` | `/api/v1/payments/my?userId=` | Payment history |
| `GET` | `/api/v1/premium/status?userId=` | Premium status |
| `POST` | `/api/v1/payments/webhook/moyasar` | Webhook sync |
| `GET` | `/api/v1/payments/moyasar-status/{id}` | Refresh (dev) |

**Payment body:**
```json
{
  "userId": 1,
  "name": "Saud Shafie",
  "number": "4111111111111111",
  "cvc": "123",
  "month": "12",
  "year": "30"
}
```

**Services:** `PaymentService`, `MoyasarService`, `PremiumService` · **Status:** 🟢 Implemented (Moyasar sandbox / test card — no real charge)

---

### Flow 12 — Premium Surprise Plan

**Precondition:** `user.isPremium = true` (seed premium user OR run Flow 11 first). Gift plan has selected idea.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/gift-plans/{giftPlanId}/surprise-plan/generate?userId=` | Generate |
| `GET` | `/api/v1/gift-plans/{giftPlanId}/surprise-plan?userId=` | Get plan |

**Generate body (optional):**
```json
{
  "language": "ar"
}
```
If `language` is omitted, falls back to the gift plan's language, then `en`.

**Block with 403 if not premium.**

**Services:** `SurprisePlanService`, `PremiumService`, `AiService` · **Status:** 🟢 Implemented (premium-gated, 403 if not premium)

---

### Flow 13 — Premium Gift Card

**Precondition:** `user.isPremium = true`.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/gift-plans/{giftPlanId}/gift-card?userId=` | Create card |
| `GET` | `/api/v1/gift-cards/my?userId=` | List mine |
| `GET` | `/api/v1/gift-cards/{giftCardId}?userId=` | Get metadata |
| `GET` | `/api/v1/gift-cards/{giftCardId}/image?userId=` | View card PNG |
| `POST` | `/api/v1/gift-cards/{giftCardId}/send-email?userId=` | Email card |
| `POST` | `/api/v1/qr-code/generate` | Test QR (dev) |

**Create body:**
```json
{
  "giftMessageId": 3,
  "recipientName": "Ahmed",
  "senderName": "Saud",
  "cardSize": "MEDIUM",
  "linkType": "SONG",
  "linkUrl": "https://open.spotify.com/track/..."
}
```

**Store `qrCodeImage` + `giftCardImage` as LONGBLOB.**

**Services:** `GiftCardService`, `QrCodeService`, `GiftCardImageService`, `EmailService` · **Status:** 🟢 Implemented (premium-gated; QR + card image as LONGBLOB; send-email)

---

### Part 3 — Checklist

- [ ] Gift message generate (AI)
- [ ] Gift history save + update + list
- [ ] Premium payment — **partially done**
- [ ] Surprise plan with premium gate
- [ ] Gift card create + LONGBLOB + email
- [ ] Load `src/main/resources/seed-part3.sql` (users 1/3, gift plans at `GIFT_IDEA_SELECTED` / `PRODUCT_SELECTED`, premium user, sample message)

---

## 8. Seed Data Strategy (How Nobody Waits)

Each developer loads **their own seed file** locally. No one needs another dev's running server.

### `seed-part1.sql` (Dev A)

```sql
-- User for ownership tests
INSERT INTO user (id, username, password, full_name, email, phone_number, role, is_premium, created_at, updated_at)
VALUES (1, 'devuser', 'not-used-yet', 'Dev User', 'dev@test.com', '+966501111111', 'USER', false, NOW(), NOW());

INSERT INTO user (id, username, password, full_name, email, role, is_premium, created_at, updated_at)
VALUES (2, 'admin', 'not-used-yet', 'Admin', 'admin@test.com', 'ADMIN', false, NOW(), NOW());

-- Recipient for quality check + group gifts
INSERT INTO recipient (id, user_id, name, relationship, age, consent_acknowledged, created_at, updated_at)
VALUES (1, 1, 'Ahmed', 'Brother', 22, true, NOW(), NOW());
```

### `seed-part2.sql` (Dev B)

Includes everything in part1 seed **plus**:

```sql
-- Admin required questions
INSERT INTO required_question (id, question_text, question_type, is_active, display_order, created_at, updated_at)
VALUES (1, 'How close are you?', 'TEXT', true, 1, NOW(), NOW()),
       (2, 'Preferred gift style?', 'SINGLE_CHOICE', true, 2, NOW(), NOW());
```

Dev B creates gift plans through the API during testing — no seed plan required.

### `seed-part3.sql` (Dev C)

Includes user + recipient **plus** a gift plan ready for messages/premium/card:

```sql
-- Premium test user
INSERT INTO user (..., is_premium) VALUES (..., false);  -- id = 1

-- Gift plan at GIFT_IDEA_SELECTED with a selected recommendation
INSERT INTO gift_plan (id, user_id, recipient_id, occasion_type, budget_minor, currency, status, ...)
VALUES (1, 1, 1, 'GRADUATION', 30000, 'SAR', 'GIFT_IDEA_SELECTED', ...);

INSERT INTO gift_idea_recommendation (id, gift_plan_id, title, search_keyword, is_selected, ...)
VALUES (1, 1, 'Wireless Gaming Headset', 'wireless gaming headset under 300 SAR', true, ...);

UPDATE gift_plan SET selected_gift_idea_id = 1 WHERE id = 1;

-- Optional: premium user for surprise/card tests
-- INSERT premium user id=3 with is_premium=true + premium_access row
```

### Loading seeds (local dev)

Add to `application-local.properties` (optional):

```properties
# spring.sql.init.mode=always
# spring.sql.init.data-locations=classpath:seed-part1.sql
```

Each dev uncomments **their** seed file only.

**Files (in repo):**

| File | Dev | What's inside |
|------|-----|---------------|
| `seed-part1.sql` | A | User 1, Admin 2, Recipient 1 |
| `seed-part2.sql` | B | Part 1 data + 3 required questions |
| `seed-part3.sql` | C | Users 1/2/3, premium payment, gift plans 1–3, recommendation, product, sample message |

---

## 9. Gift Plan Status Lifecycle

```
CREATED
  ↓ Flow 4 (Part 2)
REQUIRED_QUESTIONS_ANSWERED
  ↓ Flow 5 (Part 2)
AI_QUESTIONS_GENERATED → AI_QUESTIONS_ANSWERED
  ↓ Flow 6 (Part 2)
RECOMMENDATIONS_GENERATED → GIFT_IDEA_SELECTED
  ↓ Flow 7 (Part 2)
PRODUCT_SELECTED
  ↓ Flow 8–9 (Part 3)
COMPLETED
```

| Flow | Part | Status change |
|------|------|---------------|
| 4 | 2 | → `REQUIRED_QUESTIONS_ANSWERED` |
| 5 | 2 | → `AI_QUESTIONS_GENERATED` → `AI_QUESTIONS_ANSWERED` |
| 6 | 2 | → `RECOMMENDATIONS_GENERATED` → `GIFT_IDEA_SELECTED` |
| 7 | 2 | → `PRODUCT_SELECTED` |
| 9 | 3 | → `COMPLETED` |

---

## 10. External APIs & Services

| Tool | Part | Flow | Config key |
|------|------|------|------------|
| OpenAI | 1, 2, 3 | 10, 5, 6, 8, 12, 14 | `openai.api.key` |
| SearchAPI.io | 2 | 7 | `searchapi.api.key` |
| Moyasar sandbox | 3 | 11 | `moyasar.api.key` |
| Spring Mail | 1, 3 | 14, 15, 11, 13 | `spring.mail.*` |
| Twilio WhatsApp | 1 | 15 | `twilio.*` |
| ZXing | 3 | 13 | (library) |

---

## 11. Integration Phase (After All Parts)

When Parts 1–3 are done, one short integration sprint:

| Task | Owner |
|------|-------|
| Wire `NotificationService.create()` inside Parts 2 & 3 flows | Any |
| Replace `userId` params with JWT (Flow 1 / Phase 2 auth) | Lead |
| Merge seed files → single `seed-dev.sql` | Lead |
| End-to-end demo script | All |
| Remove dev-only test endpoints | Lead |

### End-to-end demo (after integration + auth)

1. Register & login
2. Create recipient
3. Create gift plan → required Q → AI Q → recommendations → select idea
4. Search & select product
5. Generate message → save history
6. Pay premium → surprise plan → gift card → email
7. Create group gift → vote via link
8. Set reminder

---

## 12. Current Implementation Status

| Flow | Part | Status |
|------|------|--------|
| ~~1 Auth~~ | Phase 2 | ⏸ Deferred |
| 2 Recipients | 1 | 🔴 Stub |
| 10 Quality check | 1 | 🔴 Stub |
| 14 Group gifts | 1 | 🔴 Stub |
| 15 Reminders | 1 | 🔴 Stub |
| 16 Notifications | 1 | 🔴 Stub |
| 17 Admin questions | 1 | 🔴 Stub |
| 3 Gift plans | 2 | 🔴 Stub |
| 4 Required Q | 2 | 🔴 Stub |
| 5 AI questions | 2 | 🔴 Stub |
| 6 AI recommendations | 2 | 🔴 Stub |
| 7 Product search | 2 | 🟢 Done |
| 8 Gift messages | 3 | 🔴 Stub |
| 9 Gift history | 3 | 🔴 Stub |
| 11 Premium payment | 3 | 🟢 Partial |
| 12 Surprise plan | 3 | 🔴 Stub |
| 13 Gift card | 3 | 🟡 QR only |

**Shared infrastructure:** entities, repos, `AiService`, email, WhatsApp — 🟢 Done

---

*Document version: 1.2 · Parallel parts · Auth deferred · Tahadaw backend*
