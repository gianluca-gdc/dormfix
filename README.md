# DormFix

Maintenance request tracker for dorms (CPS353 project). Residents submit repair requests and check on them, and maintenance staff update them as they go.

Frontend is Kotlin + Compose for Web (`web/`), backend is Java + Spring Boot (`server/`), and the database is SQLite. The `dormfix.db` file gets created on its own when the server starts.

## Setup

You need JDK 21 (Gradle will grab it if you don't have it). IntelliJ is recommended.

## Running

Start the backend:

```
./gradlew :server:bootRun
```

Then in a second terminal, start the frontend:

```
./gradlew :web:wasmJsBrowserDevelopmentRun
```

Go to http://localhost:3000 and you should see "Hello from the DormFix backend!" The first run is slow since it's downloading stuff. On Windows use `gradlew.bat`.

Tests: `./gradlew :server:test`

## Endpoints

- `GET /api/hello` - hello world check
- `GET /actuator/health` - shows if the server is up
- `GET /api/categories` - list all categories as `{ id, name }`
- `GET /api/tickets` - list all tickets, newest first
- `GET /api/tickets/{id}` - get one ticket (404 if it doesn't exist)
- `POST /api/tickets` - create a ticket. Body: `{ "residentId", "categoryId", "roomNumber", "description", "priority" }` (priority is `LOW`, `MEDIUM` or `HIGH`). Returns 201 with the new ticket, which always starts as `OPEN`. Returns 400 with `{ "error": "..." }` if a field is missing or an id doesn't exist.

Tickets come back as `{ "id", "residentName", "category", "roomNumber", "description", "priority", "status", "createdAt" }`, with `createdAt` as an ISO-8601 UTC string like `2026-10-20T14:30:00Z`.

Add new endpoints here when you make them.

## Git

Make a branch for each feature, open a PR, and have someone else review it before merging into `main`.