# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Layout

```
eco-web-api/
├── backend/       ← Spring Boot API (Java 21, Maven)
├── frontend/      ← Static admin panel (HTML/CSS/JS)
└── README.md
```

The two sub-projects are independent. `backend/` is a full Spring Boot webapp with its own Thymeleaf admin panel. `frontend/` is an optional standalone SPA that calls backend REST APIs — no build step, open `login.html` directly or serve with any static server.

## Backend

See `backend/CLAUDE.md` for full backend documentation (build, run, architecture, conventions).
