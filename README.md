---
type: project
created: 2026-06-15
updated: 2026-06-15
tags: [project, portfolio, java, html, css, javascript, whatsapp]
status: done
---

# DirectHelp Hub

A personal support and guidance platform where users request help (career, study, health, motivation) and the admin responds directly via WhatsApp.

## Live Demo

- **Live site:** https://diya1422.github.io/DirectHelp-Hub/
- Local full stack: run `run.bat` then open `http://localhost:9090`

## Setup

### 1. Set Your WhatsApp Number

Open `backend/src/Config.java` and change:
```java
public static final String ADMIN_WHATSAPP = "919876543210";
```
Replace `919876543210` with your number: country code + number, no `+` or spaces.
Example: India +91 98765 43210 becomes `919876543210`.

Also update `frontend/js/main.js` line 8:
```javascript
adminWhatsApp: '919876543210',
```

### 2. Run (requires Java JDK 11+)

```batch
run.bat
```

Then open: `http://localhost:8080`

### 3. Without Java Server

Open `frontend/index.html` in a browser. The WhatsApp redirect works client-side - no server needed.

## Structure

```
directhelp-hub/
├── frontend/               HTML, CSS, JS - the website
│   ├── index.html          Main landing page
│   ├── admin.html          Admin dashboard
│   ├── css/style.css       All styles (glassmorphism, dark/light mode)
│   └── js/
│       ├── main.js         Form, WhatsApp redirect, animations
│       └── admin.js        Admin panel logic
├── backend/src/            Java source files
│   ├── Main.java           Entry point - starts HTTP server
│   ├── Config.java         Constants (port, WhatsApp number, file path)
│   ├── HelpRequest.java    Interface (abstraction)
│   ├── Request.java        Model class (encapsulation)
│   ├── Storage.java        Storage interface (polymorphism)
│   ├── JsonStorage.java    JSON file storage (file handling)
│   ├── RequestService.java User-facing business logic
│   ├── AdminService.java   Admin logic (extends RequestService - inheritance)
│   └── ServerHandler.java  HTTP routing (serves static files + API)
├── data/requests.json      Stored requests (created automatically)
└── run.bat                 Compile and run script (Windows)
```

## Java OOP Concepts Used

| Concept | Where |
|---|---|
| Classes | Request, RequestService, AdminService, JsonStorage |
| Objects | Instantiated in Main.java |
| Encapsulation | Private fields + getters/setters in Request.java |
| Inheritance | AdminService extends RequestService |
| Polymorphism | HelpRequest interface, Storage interface |
| Abstraction | HelpRequest and Storage interfaces |
| File Handling | JsonStorage reads/writes data/requests.json |

## API Endpoints

| Method | Path | Description |
|---|---|---|
| POST | /api/requests | Submit a new help request |
| GET | /api/requests | Get all requests (supports ?search= and ?category=) |
| DELETE | /api/requests/{id} | Delete a request by ID |
| GET | /api/export | Download all requests as JSON |

## Features

- Glassmorphism UI with dark/light mode toggle
- 8 help categories with dropdown navigation
- WhatsApp redirect with auto-formatted message
- Admin panel: view, search, filter, delete, export requests
- JSON file storage with Java (no database needed)
- Fully responsive (mobile friendly)
- Scroll animations, FAQ accordion, resource library search
