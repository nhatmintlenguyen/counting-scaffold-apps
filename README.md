# S.C.P — Precision Scaffold Counting

Android application for high-fidelity computer vision scaffold inventory auditing.  
Built with **Jetpack Compose · Material 3 · MVVM · Room · Retrofit · EncryptedSharedPreferences**.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Project Structure](#project-structure)
3. [Feature: Authentication](#feature-authentication)
4. [Feature: Home Screen & Recent Scans](#feature-home-screen--recent-scans)
5. [Feature: Take Photo](#feature-take-photo)
6. [Feature: Upload Gallery / Files](#feature-upload-gallery--files)
7. [Local Storage](#local-storage)
8. [Networking Layer](#networking-layer)
9. [Setup & Configuration](#setup--configuration)
10. [Dependency Catalog](#dependency-catalog)

---

## Architecture Overview

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│  Compose Screens  ←→  ViewModels  (StateFlow/UiState)  │
└────────────────────────────┬───────────────────────────┘
                             │
┌────────────────────────────▼───────────────────────────┐
│                   Repository Layer                     │
│   AuthRepository          ScanRepository               │
└──────────┬─────────────────────────────┬───────────────┘
           │                             │
┌──────────▼──────────┐   ┌─────────────▼───────────────┐
│   Network (Retrofit) │   │   Local DB (Room)            │
│   AuthApiService     │   │   ScanDao / AppDatabase      │
│   AuthInterceptor    │   │   ScanRecord entity          │
│   RetrofitClient     │   └─────────────────────────────┘
└──────────────────────┘
           │
┌──────────▼──────────┐
│   TokenManager      │
│ EncryptedSharedPrefs│
└─────────────────────┘
```

The app follows **MVVM + Repository** pattern:
- **Screen** — only renders state and forwards events upward
- **ViewModel** — holds `StateFlow<UiState>`, calls repository, never touches Android context
- **Repository** — single source of truth; abstracts network vs. local DB
- **Data sources** — Retrofit (remote) and Room (local)

---

## Project Structure

```
app/src/main/java/com/example/dadn_app/
│
├── MainActivity.kt                  ← App entry; TokenManager.init(); auto-login routing
│
├── core/
│   ├── network/
│   │   ├── AuthApiService.kt        ← Retrofit interface (/api/login, /api/register)
│   │   ├── AuthInterceptor.kt       ← OkHttp interceptor; attaches Bearer token
│   │   └── RetrofitClient.kt        ← Singleton OkHttpClient + Retrofit instance
│   └── utils/
│       └── TokenManager.kt          ← EncryptedSharedPreferences wrapper (access/refresh token)
│
├── data/
│   ├── local/
│   │   ├── ScanRecord.kt            ← Room @Entity (one row = one scan job)
│   │   ├── ScanDao.kt               ← @Dao (getAll: Flow, insert)
│   │   └── AppDatabase.kt           ← Singleton Room database
│   ├── models/
│   │   └── AuthDtos.kt              ← Request/Response data classes (LoginRequest, AuthResponse…)
│   └── repository/
│       ├── AuthRepository.kt        ← login(), register() → returns AuthResult sealed class
│       └── ScanRepository.kt        ← scans: Flow, insert()
│
└── ui/
    ├── screens/
    │   ├── AppNavGraph.kt           ← Root NavHost (login → register → main)
    │   ├── AuthComponents.kt        ← Shared AuthTextField composable
    │   ├── LoginScreen.kt           ← Login UI + validation
    │   ├── RegisterScreen.kt        ← Register UI + validation
    │   └── MainScreen.kt            ← Home (Inventory tab), Current Scan, Settings
    ├── theme/
    │   ├── Color.kt                 ← Full Material 3 color token palette
    │   ├── Theme.kt                 ← Dadn_appTheme (lightColorScheme)
    │   └── Type.kt                  ← Typography
    └── viewmodel/
        ├── AuthViewModel.kt         ← AuthUiState (isLoading, errorMessage, isSuccess)
        └── HomeViewModel.kt         ← scans: StateFlow<List<ScanRecord>>, addScan()
```

---

## Feature: Authentication

### Register Screen (`RegisterScreen.kt`)

**Fields:** Full Name · Email · Password · Confirm Password

**Inline validation (before any network call):**
| Field | Rule |
|---|---|
| Full Name | Not blank, ≥ 2 characters |
| Email | Not blank, matches `android.util.Patterns.EMAIL_ADDRESS` |
| Password | Not blank, ≥ 6 characters |
| Confirm Password | Matches Password field |

**Network call:**  
`POST /api/register` → `{ "full_name": "…", "email": "…", "password": "…" }`

**On success:** tokens saved → navigated to Main screen, auth screens removed from back stack.

---

### Login Screen (`LoginScreen.kt`)

**Fields:** Email · Password (with show/hide toggle)

**Network call:**  
`POST /api/login` → `{ "email": "…", "password": "…" }`

**On success:** tokens saved → navigated to Main screen.

**Error handling:** backend error message shown in a red `Snackbar` at the bottom of the screen.

---

### AuthViewModel (`AuthViewModel.kt`)

```
AuthUiState
  isLoading: Boolean      → shows CircularProgressIndicator, disables button
  errorMessage: String?   → triggers Snackbar, then cleared via clearError()
  isSuccess: Boolean      → triggers navigation via LaunchedEffect
```

---

### Auto-Login (`MainActivity.kt`)

On every app launch:
```kotlin
TokenManager.init(applicationContext)
val start = if (TokenManager.isLoggedIn) AppRoutes.MAIN else AppRoutes.LOGIN
```
If a token exists → user lands directly on the Home screen without seeing Login.

---

## Feature: Home Screen & Recent Scans

**File:** `MainScreen.kt` — `HomeScreen()` composable

- Observes `HomeViewModel.scans: StateFlow<List<ScanRecord>>` via `collectAsState()`
- The `StateFlow` is backed by a Room `Flow` — any DB insert automatically refreshes the list
- Shows `EmptyScansPlaceholder` when no scans exist
- Each `RecentScanCard` shows: name, datetime, file type badge, status badge, count

**Status badge colours:**
| Status | Background | Text |
|---|---|---|
| Pending | Amber | Dark amber |
| Success | Cyan | Dark cyan |
| Archived | Grey | Grey |

---

## Feature: Take Photo

1. Tap **Take Photo** card
2. App checks `CAMERA` permission via `ContextCompat.checkSelfPermission`
3. If not granted → `RequestPermission` launcher → system dialog
4. If permanently denied → `AlertDialog` with instructions to open Settings
5. If granted → `createCaptureUri()` creates a temp `.jpg` in `cacheDir/camera/`  
   wrapped in a `FileProvider` URI (declared in `AndroidManifest.xml`)
6. `TakePicture` contract launches the system camera
7. On success → `ScanRecord(status="Pending", imageUri=…)` inserted into Room  
   → navigate to **Current Scan** tab

---

## Feature: Upload Gallery / Files

1. Tap **Upload Gallery** card → `UploadSourceSheet` bottom sheet appears
2. **Photos** option → `PickVisualMedia(ImageOnly)` — Android system PhotoPicker  
   No permission needed on API 33+
3. **Browse Files** option → `OpenDocument` with MIME filter  
   `["image/jpeg","image/png","image/webp","image/gif","image/bmp","image/heic"]`  
   Shows only image files in Downloads, Google Drive, local storage, etc.
4. On pick → `uriToFileType()` extracts the extension → `ScanRecord` inserted → navigate to Current Scan

---

## Local Storage

### Room Database (`scp_db`)

| Column | Type | Notes |
|---|---|---|
| `id` | Int (PK, auto) | Auto-incremented primary key |
| `name` | String | "Field Capture" / "Gallery Import" / "File Import" |
| `datetime` | String | Formatted at insert time: "Apr 06, 2026 • 09:45 AM" |
| `count` | Int | 0 until AI processes the image |
| `fileType` | String | "JPG", "PNG", etc. |
| `status` | String | "Pending" → "Success" / "Archived" |
| `imageUri` | String | `content://` or `file://` URI string |

### TokenManager (`EncryptedSharedPreferences`)

| Key | Value |
|---|---|
| `access_token` | Short-lived JWT, sent in every API request header |
| `refresh_token` | Long-lived JWT, used to renew access token (future feature) |

Data is AES-256-GCM encrypted on disk. The master key is stored in the Android Keystore.

---

## Networking Layer

### RetrofitClient (`RetrofitClient.kt`)

```kotlin
BASE_URL = "http://10.0.2.2:8000/"   // ← Change this to your server
```

`10.0.2.2` is the Android Emulator's alias for `localhost` on the host machine.

### AuthInterceptor (`AuthInterceptor.kt`)

Added to `OkHttpClient` — automatically appends:
```
Authorization: Bearer <accessToken>
```
to every request as long as `TokenManager.accessToken` is non-null.

### Expected API contract

**POST `/api/login`**
```json
Request:  { "email": "user@example.com", "password": "secret" }
Response: { "access_token": "…", "refresh_token": "…", "user": { "id": 1, "full_name": "…", "email": "…" } }
Error:    { "message": "Invalid credentials" }
```

**POST `/api/register`**
```json
Request:  { "full_name": "John Doe", "email": "user@example.com", "password": "secret" }
Response: { "access_token": "…", "refresh_token": "…", "user": { … } }
Error:    { "message": "Email already in use" }
```

---

## Setup & Configuration

### 1. Backend URL

Open `core/network/RetrofitClient.kt` and change `BASE_URL`:
```kotlin
private const val BASE_URL = "http://10.0.2.2:8000/"   // emulator → localhost
// or
private const val BASE_URL = "https://api.yourserver.com/"
```

### 2. Clone and run

```bash
git clone <repo-url>
cd dadn_app
# Open in Android Studio → Sync Project with Gradle Files → Run
```

No additional configuration required. All dependencies are declared in `gradle/libs.versions.toml`.

### 3. Internet permission

Already declared in `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## Dependency Catalog

Key entries in `gradle/libs.versions.toml`:

| Alias | Library | Purpose |
|---|---|---|
| `retrofit-core` | Retrofit 2.11 | HTTP client |
| `retrofit-converter-gson` | Gson converter | JSON serialisation |
| `okhttp-core` | OkHttp 4.12 | HTTP engine |
| `okhttp-logging-interceptor` | OkHttp logging | Logcat request/response logging |
| `security-crypto` | androidx.security 1.1.0-alpha06 | EncryptedSharedPreferences |
| `room-runtime` | Room 2.6.1 | SQLite ORM |
| `room-ktx` | Room KTX | Coroutine support for Room |
| `room-compiler` | Room KSP | Code generation (via KSP) |
| `navigation-compose` | Navigation 2.8.9 | Compose navigation |
| `lifecycle-viewmodel-compose` | ViewModel 2.10 | `viewModel()` in Composable |
| `compose-material-icons-extended` | Material Icons | Extended icon set |
| `coil-compose` | Coil 2.7 | Image loading |
| `camerax-*` | CameraX 1.4.2 | Camera capture |
