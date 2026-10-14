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

Add new endpoints here when you make them.

## Git

Make a branch for each feature, open a PR, and have someone else review it before merging into `main`.