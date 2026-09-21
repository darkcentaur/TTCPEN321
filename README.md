# CPEN 321 M1 - TTCPEN321

Individual M1 implementation for CPEN 321 Fall 2026.

The application contains three independent features:

1. **Google Sign-In + Server Information**  
   Authenticates the user with Google and retrieves server/client information from the cloud-hosted backend.

2. **Live Pixel Updates**  
   Displays a 16×16 pixel-art image assembled in real time using WebSocket updates relayed through the backend.

3. **Timer + Surprise**  
   Allows the user to set a timer and displays a randomly selected surprise message when the timer expires.

---

## Requirements

Install the following before running the project:

- Git
- Docker Desktop / Docker Engine with Docker Compose
- Android Studio
- Android SDK with Android Baklava (API 36)
- Pixel 9 emulator with API 36
- Java

On Windows, if Java is not available on `PATH`, `scripts/run-frontend.ps1` will attempt to use the Java runtime bundled with Android Studio.

---

## Repository Structure

- `frontend/` — Android application written in Kotlin using Jetpack Compose
- `backend/` — Node.js / TypeScript backend
- `scripts/` — scripts for building, running, and testing the application
- `docker-compose.yml` — Docker configuration for the backend and MongoDB

---

# Frontend Setup

## 1. Configure `frontend/local.properties`

Create:

`frontend/local.properties`

with the following contents:

```properties
sdk.dir=<PATH_TO_ANDROID_SDK>
API_BASE_URL=https://34.83.7.203
GOOGLE_CLIENT_ID=<GOOGLE_OAUTH_WEB_CLIENT_ID>
```

### Configuration Notes

#### `sdk.dir`

Replace this with the Android SDK path on the machine running the project.

Example on Windows:

```properties
sdk.dir=C\:\\Users\\<USERNAME>\\AppData\\Local\\Android\\Sdk
```

#### `API_BASE_URL`

Use the deployed M1 backend:

```properties
API_BASE_URL=https://34.83.7.203
```

This value should not need to be changed when grading the submitted M1 application.

#### `GOOGLE_CLIENT_ID`

Replace this with the Google OAuth **Web Client ID** provided in the M1 submission documentation.

Do not commit `frontend/local.properties` to Git.

---

## 2. Emulator Setup

Create or use the following Android emulator:

```text
Device: Pixel 9
Android version: Baklava
API level: 36
```

A Google account should be signed into the emulator before testing Google Sign-In.

---

## 3. Run the Frontend

From the repository root on Windows:

```powershell
.\scripts\run-frontend.ps1
```

On Linux/macOS:

```bash
./scripts/run-frontend.sh
```

The script builds the Android application, installs it on the emulator, and launches it.

---

# Backend Setup

The submitted M1 application uses a backend already deployed on Google Cloud:

```text
https://34.83.7.203
```

For local development or independent backend deployment, follow the steps below.

## 1. Configure `backend/.env`

Copy:

```text
backend/.env.example
```

to:

```text
backend/.env
```

and configure the required values.

Example:

```env
PORT=3000
NODE_ENV=development

MONGODB_URI=mongodb://localhost:27017/cpen321

GOOGLE_CLIENT_ID=<GOOGLE_OAUTH_WEB_CLIENT_ID>

JWT_SECRET=<RANDOM_SECRET>

SERVER_PUBLIC_IP=<SERVER_PUBLIC_IP>
```

A random JWT secret can be generated with:

```bash
openssl rand -hex 32
```

Do not commit `backend/.env` to Git.

When Docker Compose is used, the MongoDB connection is automatically configured to use the MongoDB container.

---

## 2. Run the Backend

From the repository root on Windows:

```powershell
.\scripts\run-backend.ps1
```

On Linux/macOS:

```bash
./scripts/run-backend.sh
```

Docker Compose starts:

- Node.js / TypeScript backend
- MongoDB

The local backend is exposed on:

```text
http://localhost:3000
```

---

# Backend Interfaces

The backend exposes the following interfaces:

```text
GET /health
GET /server-ip
GET /server-time
GET /name
WS  /live
```

The `/live` WebSocket relays pixel updates received from the CPEN 321 course WebSocket server to the Android frontend.

---

# Deployed M1 Backend

Production backend:

```text
https://34.83.7.203
```

Production WebSocket endpoint:

```text
wss://34.83.7.203/live
```

The deployed backend runs on a Google Cloud Compute Engine VM using Docker Compose. Nginx provides HTTPS/WSS access and forwards requests to the Node.js backend.

---

# M1 Verification

Before submission, verify the submitted application using:

```text
Pixel 9 emulator
Android Baklava
API 36
```

Confirm that:

- Google Sign-In succeeds
- Button 1 displays the server public IP `34.83.7.203`
- Server and client times are displayed correctly
- Button 2 displays continuously updating 16×16 pixel art
- Button 3 timer counts down and displays the surprise correctly
- The backend remains reachable through HTTPS