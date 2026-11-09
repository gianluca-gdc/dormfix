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

- `POST /api/auth/login` - log in with `{ "email", "password" }`. Returns the user id, name, role and room number, or 401 with `{ "error": "Invalid email or password" }`.
- `GET /api/hello` - hello world check
- `GET /actuator/health` - shows if the server is up
- `GET /api/categories` - list all categories as `{ id, name }`
- `GET /api/tickets` - list all tickets, newest first
- `GET /api/tickets?residentId=1` - list one resident's tickets ("My Requests"), newest first
- `GET /api/tickets/{id}` - get one ticket (404 `"Request not found."` if it doesn't exist)
- `POST /api/tickets` - create a ticket. Body: `{ "residentId", "categoryId", "roomNumber", "description", "priority" }` (priority is `LOW`, `MEDIUM` or `HIGH`). Returns 201 with the new ticket, which always starts as `OPEN`. Returns 400 if a field is missing or an id doesn't exist, e.g. `"Description is required."` or `"Category not found."`.
- `PATCH /api/tickets/{id}/status` - change a ticket's status. Body: `{ "newStatus", "actorId", "note" }` (note is optional). The change must be allowed by `StatusRules` (staff: OPEN → IN_PROGRESS → RESOLVED; the owning resident: RESOLVED → OPEN), otherwise 400 `"That status change isn't allowed."`. Returns 200 with the updated ticket and adds an entry to its history.
- `GET /api/tickets/{id}/history` - the ticket's status changes, oldest first, as `[{ "oldStatus", "newStatus", "note", "updatedBy", "createdAt" }]` (`note` is `""` when none was given; 404 if the ticket doesn't exist)

Tickets come back as `{ "id", "residentId", "residentName", "category", "roomNumber", "description", "priority", "status", "createdAt" }`, with `createdAt` as an ISO-8601 UTC string like `2026-10-20T14:30:00Z`.

Every error comes back as `{ "error": "<message>" }` with a matching status code (400, 401 or 404).

Add new endpoints here when you make them.

## Test accounts

- Resident: `sam@example.edu` / `sam-password`
- Staff: `dana@example.edu` / `dana-password`

## Git

Make a branch for each feature, open a PR, and have someone else review it before merging into `main`.
