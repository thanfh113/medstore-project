# Architecture & Flow Diagrams

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                          Android Application                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    AppnavHost (Navigation)                   │   │
│  │                                                              │   │
│  │  ┌────────────────────────────────────────────────────────┐ │   │
│  │  │ ✨ Global Authentication Listener (NEW)               │ │   │
│  │  │                                                        │ │   │
│  │  │ SessionManager.isLoggedIn.collectAsState()           │ │   │
│  │  │     ↓                                                 │ │   │
│  │  │ LaunchedEffect(isLoggedIn.value)                     │ │   │
│  │  │     ↓                                                 │ │   │
│  │  │ if (!isLoggedIn) navigate("LoginScreen")             │ │   │
│  │  │     ↓                                                 │ │   │
│  │  │ popUpTo(0) { inclusive = true }                      │ │   │
│  │  │                                                        │ │   │
│  │  └────────────────────────────────────────────────────────┘ │   │
│  │                          ↑                                    │   │
│  │                          │                                    │   │
│  │                      Listens to                               │   │
│  │                                                              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│         ↑                                    ↓                       │
│         │                                    │                       │
│  ┌──────┴────────────────────────────────────┴────────────────┐    │
│  │                  Screen Destinations                        │    │
│  │  (LoginScreen, MainScreen, HomeScreen, etc.)              │    │
│  └───────────────────────────────────────────────────────────┘    │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────┐    │
│  │                    API Layer                              │    │
│  │  ┌─────────────────────────────────────────────────────┐ │    │
│  │  │  AuthInterceptor (OkHttp)                           │ │    │
│  │  │  • Adds Authorization headers                       │ │    │
│  │  │  • Catches 401 responses                            │ │    │
│  │  │  • Attempts token refresh                           │ │    │
│  │  │  • On failure: sessionManager.clearSession() ✓     │ │    │
│  │  └─────────────────────────────────────────────────────┘ │    │
│  │                          ↓                                 │    │
│  │  ┌─────────────────────────────────────────────────────┐ │    │
│  │  │  SessionManager                                     │ │    │
│  │  │  • Stores tokens in DataStore                       │ │    │
│  │  │  • Provides isLoggedIn: Flow<Boolean>              │ │    │
│  │  │  • clearSession() clears DataStore                │ │    │
│  │  │  • Flow automatically emits false on clear ✓      │ │    │
│  │  └─────────────────────────────────────────────────────┘ │    │
│  │                                                           │    │
│  └───────────────────────────────────────────────────────────┘    │
│                          ↑                                         │
│                          │ HTTP                                    │
│                    ┌─────┴──────┐                                  │
│                    │             │                                │
└────────────────────┼─────────────┼────────────────────────────────┘
                     │             │
              ┌──────┴─────┐ ┌─────┴──────┐
              │  Database  │ │   Backend  │
              │  (DataStore)│ │  (8080)   │
              └────────────┘ └────────────┘
```

---

## Token Refresh Flow - Before vs After

### ❌ BEFORE FIX

```
User API Call
     ↓
AuthInterceptor adds token
     ↓
Backend: 401 Unauthorized
     ↓
AuthInterceptor catches 401
     ↓
Attempts refresh with refreshToken
     ↓
Backend: 401 (token not in DB)
     ↓
sessionManager.clearSession()
     ↓
DataStore cleared
     ↓
❌ Nothing listens to state change
     ↓
App UI still showing MainScreen
     ↓
❌ User stuck with error
```

### ✅ AFTER FIX

```
User API Call
     ↓
AuthInterceptor adds token
     ↓
Backend: 401 Unauthorized
     ↓
AuthInterceptor catches 401
     ↓
Attempts refresh with refreshToken
     ↓
Backend: 401 (token not in DB)
     ↓
sessionManager.clearSession()
     ↓
DataStore cleared
     ↓
✅ SessionManager.isLoggedIn emits: false
     ↓
✅ AppnavHost.LaunchedEffect detects change
     ↓
✅ Condition: !isLoggedIn && currentRoute != "LoginScreen"
     ↓
✅ navController.navigate("LoginScreen")
     ↓
✅ popUpTo(0) clears back stack
     ↓
✅ Compose recomposition
     ↓
✅ LoginScreen appears
     ↓
✅ User can login again
```

---

## Reactive State Flow

```
                    ┌─────────────────────┐
                    │   User Action       │
                    │  (API call made)    │
                    └──────────┬──────────┘
                               ↓
                    ┌─────────────────────┐
                    │ Backend Unreachable │
                    │ or Token Invalid    │
                    └──────────┬──────────┘
                               ↓
                    ┌─────────────────────┐
                    │ 401 Response        │
                    │ (Unauthorized)      │
                    └──────────┬──────────┘
                               ↓
        ┌──────────────────────────────────────────────┐
        │    AuthInterceptor.intercept()               │
        │                                              │
        │  if (response.code == 401) {                │
        │    attemptTokenRefresh()                    │
        │  }                                          │
        └──────────────────────┬───────────────────────┘
                               ↓
        ┌──────────────────────────────────────────────┐
        │    Refresh Token Request                     │
        │                                              │
        │  POST /api/v1/auth/refresh                  │
        │  Body: { refreshToken }                     │
        └──────────────────────┬───────────────────────┘
                               ↓
        ┌──────────────────────────────────────────────┐
        │    Backend Response: 401 or 400              │
        │    (Refresh token is also invalid)          │
        └──────────────────────┬───────────────────────┘
                               ↓
        ┌──────────────────────────────────────────────┐
        │    sessionManager.clearSession()             │
        │                                              │
        │  context.dataStore.edit {                   │
        │    preferences.clear()                      │
        │  }                                          │
        └──────────────────────┬───────────────────────┘
                               ↓
        ┌──────────────────────────────────────────────┐
        │    DataStore Preferences Cleared             │
        │    • ACCESS_TOKEN removed                    │
        │    • REFRESH_TOKEN removed                  │
        │    • All user data cleared                  │
        └──────────────────────┬───────────────────────┘
                               ↓
        ┌──────────────────────────────────────────────┐
        │    SessionManager.isLoggedIn Flow            │
        │    Calculates:                               │
        │                                              │
        │    (ACCESS_TOKEN != null &&                 │
        │     REFRESH_TOKEN != null)                  │
        │                                              │
        │    = false (both are null now)              │
        └──────────────────────┬───────────────────────┘
                               ↓
        ┌──────────────────────────────────────────────┐
        │    Flow.emit(false)                          │
        │    ✨ NEW: LaunchedEffect observes this     │
        └──────────────────────┬───────────────────────┘
                               ↓
        ┌──────────────────────────────────────────────┐
        │    AppnavHost.LaunchedEffect {               │
        │        LaunchedEffect(isLoggedIn.value) {   │
        │            // isLoggedIn changed to false   │
        │            val currentRoute = ...           │
        │            if (!isLoggedIn.value &&         │
        │                currentRoute != LoginScreen) │
        │            {                                │
        │                navigate("LoginScreen")      │
        │            }                                │
        │        }                                    │
        │    }                                        │
        └──────────────────────┬───────────────────────┘
                               ↓
        ┌──────────────────────────────────────────────┐
        │    NavHost Recomposition                     │
        │                                              │
        │    navigate("LoginScreen") {                │
        │        popUpTo(0) { inclusive = true }      │
        │    }                                        │
        └──────────────────────┬───────────────────────┘
                               ↓
        ┌──────────────────────────────────────────────┐
        │    Compose Renders LoginScreen               │
        │                                              │
        │    @Composable LoginScreen() { ... }        │
        │                                              │
        │    ✅ User sees clean login form            │
        │    ✅ No errors or confusion                │
        │    ✅ Can immediately login again           │
        └──────────────────────────────────────────────┘
```

---

## Component Interaction Diagram

```
┌────────────────────────────────────────────────────────────────┐
│                    Composition Hierarchy                        │
└────────────────────────────────────────────────────────────────┘

        MainActivity
             ↓
        DismissKeyboard
             ↓
        NhathuocTheme
             ↓
    ┌────────────────────────┐
    │   AppnavHost ✨ (NEW)   │
    │                        │
    │ ┌──────────────────┐   │
    │ │ SessionManager   │   │
    │ │ (remember)       │   │
    │ └──────────────────┘   │
    │         ↓              │
    │ ┌──────────────────┐   │
    │ │ isLoggedIn Flow  │   │
    │ │ (collectAsState) │   │
    │ └──────────────────┘   │
    │         ↓              │
    │ ┌──────────────────┐   │
    │ │ LaunchedEffect   │   │
    │ │ (listens)        │   │
    │ └──────────────────┘   │
    │         ↓              │
    │ ┌──────────────────┐   │
    │ │   NavHost        │   │
    │ │                  │   │
    │ │ ┌──────────────┐ │   │
    │ │ │ LoginScreen  │ │   │
    │ │ ├──────────────┤ │   │
    │ │ │ MainScreen   │ │   │
    │ │ ├──────────────┤ │   │
    │ │ │HomeScreen    │ │   │
    │ │ ├──────────────┤ │   │
    │ │ │  ...others   │ │   │
    │ │ └──────────────┘ │   │
    │ └──────────────────┘   │
    └────────────────────────┘
             ↓
    ┌────────────────────────┐
    │   Screens show UI      │
    └────────────────────────┘
```

---

## Data Flow: Session Management

```
┌──────────────────────────────────────────┐
│         SessionManager                   │
│  Manages: DataStore<Preferences>         │
└──────────────────────────────────────────┘
           ↓              ↑
      Stores:         Reads:
    • AccessToken    • isLoggedIn: Flow
    • RefreshToken   • getUserId()
    • UserInfo       • getAccessToken()
           ↓              ↑
    ┌──────────────────────────────────────────┐
    │      DataStore Preferences               │
    │  (Encrypted, persistent storage)         │
    └──────────────────────────────────────────┘
           ↑              ↓
      Cleared:        Observed:
    • clearSession() • Flows
                     • collectAsState
                     • LaunchedEffect

When clearSession() is called:
    ↓
preferences.clear()
    ↓
All keys removed
    ↓
isLoggedIn Flow calculates:
  (ACCESS_TOKEN != null && REFRESH_TOKEN != null)
    ↓
Result: false (both are now null)
    ↓
Flow emits: false
    ↓
LaunchedEffect sees: isLoggedIn changed from true → false
    ↓
Action: Navigate to LoginScreen
```

---

## Complete Request/Response Cycle

```
REQUEST PHASE:
┌──────────────────────────────────────────────────────┐
│ 1. User clicks button that requires authentication   │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│ 2. API call made (e.g., GET /api/v1/orders)         │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│ 3. AuthInterceptor intercepts                        │
│    - Gets accessToken from SessionManager            │
│    - Adds: Authorization: Bearer {accessToken}       │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│ 4. Request sent to backend                           │
└──────────────────────────────────────────────────────┘

RESPONSE PHASE:
┌──────────────────────────────────────────────────────┐
│ 5. Backend receives request                          │
│    - Verifies JWT signature                          │
│    - Token not found (backend restarted)             │
│    - Returns: 401 Unauthorized                       │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│ 6. AuthInterceptor receives 401                      │
│    - response.code == 401: true                      │
│    - Enter refresh logic                             │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│ 7. Attempt token refresh                             │
│    POST /api/v1/auth/refresh                        │
│    Body: { refreshToken: "eyJ..." }                 │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│ 8. Backend rejects refresh                           │
│    - Refresh token not in DB                         │
│    - Returns: 401 Unauthorized                       │
└──────────────────────────────────────────────────────┘
                         ↓
RECOVERY PHASE:
┌──────────────────────────────────────────────────────┐
│ 9. AuthInterceptor sees refresh failed               │
│    - refreshResponse == null                         │
│    - Call: sessionManager.clearSession()             │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│ 10. ✨ SessionManager clears DataStore               │
│     - ACCESS_TOKEN removed                           │
│     - REFRESH_TOKEN removed                          │
│     - USER_ID removed, etc.                         │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│ 11. ✨ Flow updates (NEW)                            │
│     - SessionManager.isLoggedIn emits false         │
│     - collectAsState in AppnavHost updates          │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│ 12. ✨ LaunchedEffect triggers (NEW)                 │
│     - LaunchedEffect(isLoggedIn.value) fires        │
│     - Condition check passes                         │
│     - navigate("LoginScreen") called                │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│ 13. ✨ Navigation + Recomposition (NEW)              │
│     - NavHost updates destination                    │
│     - LoginScreen composable renders                │
│     - Back stack cleared (popUpTo)                  │
└──────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────┐
│ 14. ✨ User sees LoginScreen (RESULT)                │
│     - Clean login form displayed                     │
│     - No errors visible                              │
│     - User ready to login again                      │
└──────────────────────────────────────────────────────┘
```

---

## Timing Diagram

```
Time    Event                                           Duration
────────────────────────────────────────────────────────────────
  0ms   User clicks button
         │
         └─→ API call starts
                  │
                  └─→ Network latency (network dependent)
                      
100ms   Backend receives request
         │
         └─→ JWT verification
                  │
                  └─→ Token check: Not found
                      
150ms   Backend responds: 401
         │
         └─→ Network latency (network dependent)

250ms   AuthInterceptor receives 401
         │
         └─→ attemptTokenRefresh() called
                  │
                  └─→ New request: refresh token
                      
350ms   Backend receives refresh request
         │
         └─→ Refresh token check: Not in DB
                  │
                  └─→ Returns: 401
                      
450ms   AuthInterceptor gets refresh failed
         │
         └─→ sessionManager.clearSession()
         │   (50-100ms)
         │
         └─→ DataStore.edit { clear() }
                  
550ms   SessionManager.isLoggedIn Flow reacts
         │
         └─→ Calculates: (null && null) = false
         │   (10-50ms)
         │
         └─→ Emits: false
                  
600ms   LaunchedEffect detects change
         │
         └─→ isLoggedIn.value changed
         │   (20-100ms)
         │
         └─→ navigate("LoginScreen")
                  
700ms   Navigation + Compose Recomposition
         │
         └─→ NavHost switches destination
         │   (200-500ms)
         │
         └─→ LoginScreen renders
                  
1200ms  ✅ COMPLETE - User sees LoginScreen

────────────────────────────────────────────────────────────────
Total time: ~1.2 seconds (including network latency)
User experience: Fast and seamless automatic logout
```


