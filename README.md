# 🤖 Secure Admin OllamaChat Model Dashboard

A full-stack AI chat application with Google OAuth2 authentication, role-based access control, a Super Admin dashboard, and real-time streaming chat powered by Ollama LLMs.

---

## 🚀 Tech Stack

### Backend
- **Java 21** + **Spring Boot 3** (WebFlux / Netty — fully reactive)
- **Spring Security** — JWT + Google OAuth2
- **Spring AI** — Ollama integration
- **MySQL** — JPA / Hibernate
- **Lombok**, **Actuator**, **Prometheus**

### Frontend
- **Angular 17+** (Standalone components)
- **TypeScript**
- **SSE** (Server-Sent Events) for real-time streaming chat

---

## ✨ Features

- 🔐 **Google OAuth2 Login** — one-click sign-in with Google
- 🧠 **AI Chat** — real-time streaming responses via Ollama LLMs (supports `gemma2:2b`, `mistral`, `llama3.2`, etc.)
- 🏏 **Cricket Chat** — dedicated cricket-focused AI assistant
- 👑 **Super Admin Dashboard** — role management, user control, analytics
- 🔒 **Role-Based Access Control** — `ROLE_USER` and `ROLE_SUPER_ADMIN`
- 📊 **Analytics** — total users, active/disabled counts, total chats
- ⚡ **Reactive** — built on Spring WebFlux + Netty (no servlet container)

---

## 🏗️ Project Structure

```
Secure-Admin-OllamaChat-Model-DashBoard/
├── BackEnd/
│   └── aiSpprin/demo/
│       └── src/main/java/com/ai/demo/
│           ├── controller/        # REST controllers (Chat, Auth, SuperAdmin)
│           ├── security/          # JWT, OAuth2, SecurityConfig, SuperAdminChecker
│           ├── service/           # AuthService, ChatService
│           ├── model/             # User, ChatHistory entities
│           ├── repository/        # JPA repositories
│           ├── dto/               # Request/Response DTOs
│           └── exception/         # Global exception handler
└── FrontEnd/
    └── imageGenProj/src/app/
        ├── auth/                  # AuthService, Guards, OAuth2 callback
        ├── pages/
        │   ├── chat/              # Chat page + SSE streaming
        │   ├── cricket/           # Cricket chat page
        │   ├── login/             # Google OAuth2 login page
        │   └── super-admin/       # Super Admin dashboard
        ├── service/               # ApiService
        └── shared/
            └── sidemenu/          # Navigation sidebar
```

---

## ⚙️ Setup & Installation

### Prerequisites
- Java 21+
- Node.js 18+
- MySQL 8+
- [Ollama](https://ollama.com) installed and running

### 1. Pull an Ollama Model

```bash
ollama pull gemma2:2b
```

### 2. Backend Setup

```bash
cd BackEnd/aiSpprin/demo
```

Configure `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/aibox
    username: root
    password: root

  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET

app:
  super-admin:
    email-domains: motivitylabs.com
    emails: youremail@gmail.com    # your personal super admin email
  frontend:
    url: http://localhost:4200

  ai:
    ollama:
      chat:
        options:
          model: gemma2:2b
```

Run the backend:

```bash
./mvnw spring-boot:run
```

### 3. Frontend Setup

```bash
cd FrontEnd/imageGenProj
npm install
ng serve
```

App will be available at `http://localhost:4200`

---

## 🔑 Authentication & Roles

| Role | Access |
|------|--------|
| `ROLE_USER` | Chat, Cricket Chat |
| `ROLE_SUPER_ADMIN` | Everything + Super Admin Dashboard |

### How roles are assigned
- Any email matching `app.super-admin.email-domains` (e.g. `@motivitylabs.com`) → **ROLE_SUPER_ADMIN**
- Any specific email listed in `app.super-admin.emails` → **ROLE_SUPER_ADMIN**
- All other Google accounts → **ROLE_USER**
- Super admins can promote any user to `ROLE_SUPER_ADMIN` from the dashboard

---

## 👑 Super Admin Dashboard

Accessible at `/super-admin` (only visible to super admins in the sidebar).

| Feature | Description |
|---------|-------------|
| 📋 View all users | Full user list with roles and status |
| 🔼 Promote user | Assign `ROLE_SUPER_ADMIN` to any user |
| 🔽 Demote user | Revert super admin back to `ROLE_USER` |
| 🚫 Disable user | Block user from logging in |
| ✅ Enable user | Re-enable a disabled user |
| 🗑️ Delete user | Permanently remove user from DB |
| 📊 Analytics | Total users, active, disabled, total chats |

---

## 🌐 API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/auth/register` | Register with email/password |
| `POST` | `/api/v1/auth/login` | Login with email/password |
| `POST` | `/api/v1/auth/refresh` | Refresh JWT token |

### Chat
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/chat?prompt=...` | Streaming AI chat (SSE) |
| `GET` | `/api/v1/chat/cricket?prompt=...` | Streaming cricket chat (SSE) |

### Super Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/super-admin/users` | Get all users |
| `PUT` | `/api/v1/super-admin/users/{id}/role` | Assign role |
| `PUT` | `/api/v1/super-admin/users/{id}/disable` | Disable user |
| `PUT` | `/api/v1/super-admin/users/{id}/enable` | Enable user |
| `DELETE` | `/api/v1/super-admin/users/{id}` | Delete user |
| `GET` | `/api/v1/super-admin/analytics` | Get analytics |

---

## 🔧 Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `APP_PORT` | `8080` | Server port |
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3307` | MySQL port |
| `DB_NAME` | `aibox` | Database name |
| `DB_USERNAME` | `root` | DB username |
| `DB_PASSWORD` | `root` | DB password |
| `JWT_SECRET` | `...` | JWT signing secret (min 256 bits) |
| `JWT_EXPIRATION_MS` | `86400000` | JWT expiry (24h) |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama server URL |
| `OLLAMA_MODEL` | `gemma2:2b` | Ollama model name |
| `FRONTEND_URL` | `http://localhost:4200` | Angular app URL |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | CORS origins |
| `SUPER_ADMIN_EMAIL_DOMAINS` | `motivitylabs.com` | Super admin domains |
| `SUPER_ADMIN_EMAILS` | `` | Super admin specific emails |

---

## 📦 Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project → **APIs & Services** → **Credentials**
3. Create **OAuth 2.0 Client ID** (Web application)
4. Add Authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
5. Copy **Client ID** and **Client Secret** into `application.yml`

---

## 📈 Monitoring

Actuator endpoints available at:
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/metrics`
- `http://localhost:8080/actuator/prometheus`

---

## 👤 Author

**Yash Sutar**  
[GitHub](https://github.com/sutaryash32)