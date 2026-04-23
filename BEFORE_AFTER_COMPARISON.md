# Before vs After Comparison

## Test Case Requirement
**Kỳ vọng:** Restart backend hoặc make refresh token invalid, bấm refresh ở màn có API call.
- Expected: app không treo, session bị clear và quay về LoginScreen
- If vẫn đứng ở màn cũ với lỗi token mà không về login => FAIL

---

## Behavior Comparison

### ❌ BEFORE FIX

```
┌─────────────────────────────────────────────────────────────┐
│ USER SCENARIO: Token becomes invalid after backend restart  │
└─────────────────────────────────────────────────────────────┘

1. User logged in successfully ✓
   - Email: admin@medstore.vn
   - Access Token: eyJhbGc... (cached)
   - Refresh Token: eyJhbGc... (cached)

2. Backend restarts
   - All tokens in DB are invalidated
   - But app doesn't know yet

3. User interacts with app (makes API call)
   ↓
4. AuthInterceptor intercepts request
   - Adds: Authorization: Bearer eyJhbGc...
   - Sends request
   ↓
5. Backend returns: 401 Unauthorized
   - Access token doesn't match any in DB
   ↓
6. AuthInterceptor catches 401
   - Calls: attemptTokenRefresh(refreshToken)
   ↓
7. Token Refresh Request
   - Sends old refresh token to backend
   - Backend: "This token is not in DB, rejected"
   - Response: 401 OR 400 (refresh failed)
   ↓
8. AuthInterceptor sees refresh failed
   - Calls: sessionManager.clearSession()
   - DataStore.clear() executes
   - Tokens removed from storage
   ✓ SESSION CLEARED
   ↓
9. ❌ BUT... NOTHING IS LISTENING! 
   - No component watches the session state
   - App is still rendering MainScreen
   - Navigation doesn't happen automatically
   ↓
10. User sees:
    - Current screen still visible
    - Network error message
    - Can't click buttons (401 on all requests)
    - Can't navigate back (confused)
    - Can't recover without:
      - Manually restarting app
      - Or using back navigation to logout
    ❌ STUCK IN THIS STATE
    ❌ TEST FAILS - "App treo, không về LoginScreen"

11. User Experience:
    - 😢 Confused and frustrated
    - 😢 Can't understand why screen is frozen
    - 😢 Must restart app manually
    - 😢 Bad UX
```

---

### ✅ AFTER FIX

```
┌─────────────────────────────────────────────────────────────┐
│ SAME SCENARIO: Token becomes invalid after backend restart  │
└─────────────────────────────────────────────────────────────┘

1. User logged in successfully ✓
   - Email: admin@medstore.vn
   - Access Token: eyJhbGc... (cached)
   - Refresh Token: eyJhbGc... (cached)

2. Backend restarts
   - All tokens in DB are invalidated
   - But app doesn't know yet

3. User interacts with app (makes API call)
   ↓
4. AuthInterceptor intercepts request
   - Adds: Authorization: Bearer eyJhbGc...
   - Sends request
   ↓
5. Backend returns: 401 Unauthorized
   - Access token doesn't match any in DB
   ↓
6. AuthInterceptor catches 401
   - Calls: attemptTokenRefresh(refreshToken)
   ↓
7. Token Refresh Request
   - Sends old refresh token to backend
   - Backend: "This token is not in DB, rejected"
   - Response: 401 OR 400 (refresh failed)
   ↓
8. AuthInterceptor sees refresh failed
   - Calls: sessionManager.clearSession()
   - DataStore.clear() executes
   - Tokens removed from storage
   ✓ SESSION CLEARED
   ↓
9. ✅ NOW: SessionManager.isLoggedIn Flow reacts!
   - Flow: "No ACCESS_TOKEN? No REFRESH_TOKEN? -> false"
   - Emits: isLoggedIn = FALSE
   ↓
10. ✅ AppnavHost LaunchedEffect listens!
    - Detects: isLoggedIn.value changed to false
    - Checks: Are we on LoginScreen? No
    - Checks: Are we on RegisterScreen? No
    - Action: Navigate to LoginScreen
    - With: popUpTo(0) { inclusive = true } (clear back stack)
    ↓
11. ✅ Compose Recomposition
    - NavHost switches to LoginScreen destination
    - LoginScreen composable renders
    - Back stack is empty (can't go back)
    ↓
12. User sees:
    - ✅ Automatically redirected to LoginScreen
    - ✅ No error visible
    - ✅ Can immediately login again
    - ✅ No app restart needed
    - ✅ Can't accidentally go back to old screen
    ✅ SMOOTH EXPERIENCE
    ✅ TEST PASSES - "App không treo, quay về LoginScreen"

13. User Experience:
    - 😊 Smooth automatic logout
    - 😊 Ready to login again immediately
    - 😊 Professional, polished feel
    - 😊 No confusion or frustration
    - 😊 Good UX
```

---

## Code-Level Comparison

### ❌ BEFORE: No Global State Listener

```kotlin
// AuthInterceptor.kt - Clears session but nothing listens
if (refreshResponse != null) {
    // Success - use new token
} else {
    // Refresh failed
    runBlocking { sessionManager.clearSession() }  // ← Called, but...
    // ... no component is monitoring this!
}

// AppnavHost.kt - No authentication monitoring
@Composable
fun AppnavHost(navController: NavHostController) {
    val pendingProductContext = remember { mutableStateOf<ChatProductContext?>(null) }
    val context = LocalContext.current
    
    NavHost(
        navController = navController,
        startDestination = "MainScreen"  // ← Always starts here, won't auto-redirect
    ) {
        composable("LoginScreen") { ... }
        composable("MainScreen") { ... }  // ← User stuck here if session cleared
        // ... other screens
    }
}
```

---

### ✅ AFTER: Global State Listener Added

```kotlin
// AuthInterceptor.kt - (UNCHANGED) Clears session
if (refreshResponse != null) {
    // Success - use new token
} else {
    // Refresh failed
    runBlocking { sessionManager.clearSession() }  // ← Called AND now being monitored!
}

// AppnavHost.kt - (NEW) Listens to session state
@Composable
fun AppnavHost(navController: NavHostController) {
    val pendingProductContext = remember { mutableStateOf<ChatProductContext?>(null) }
    val context = LocalContext.current
    
    // ✨ NEW: Global authentication state monitoring
    val sessionManager = remember { SessionManager(context) }
    val isLoggedIn = sessionManager.isLoggedIn.collectAsState(initial = false)
    
    LaunchedEffect(isLoggedIn.value) {
        // When isLoggedIn changes to false, auto-redirect
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (!isLoggedIn.value && currentRoute != null && 
            currentRoute != "LoginScreen" && currentRoute != "RegisterScreen") {
            navController.navigate("LoginScreen") {
                popUpTo(0) { inclusive = true }  // Clear back stack
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = "MainScreen"
    ) {
        composable("LoginScreen") { ... }
        composable("MainScreen") { ... }  // ← Will auto-redirect away if session cleared
        // ... other screens
    }
}
```

---

## State Flow Comparison

### ❌ BEFORE: One-Way Flow (No Listener)
```
sessionManager.clearSession()
        ↓
DataStore.clear()
        ↓
Session state changed to: loggedOut
        ↓
❌ Nobody listening! 
   App state doesn't update
   Navigation doesn't happen
   Screen stays the same
```

### ✅ AFTER: Reactive Flow (With Listener)
```
sessionManager.clearSession()
        ↓
DataStore.clear()
        ↓
Session state changed to: loggedOut
        ↓
SessionManager.isLoggedIn Flow emits: false
        ↓
AppnavHost.LaunchedEffect detects: value changed
        ↓
✅ Executes: navigate("LoginScreen") { popUpTo(0) }
        ↓
Compose recomposes
        ↓
✅ Screen automatically updates to LoginScreen
```

---

## Test Result Impact

| Aspect | Before | After |
|--------|--------|-------|
| **App Hangs?** | ❌ Yes, stuck on screen | ✅ No, smooth redirect |
| **Shows Error?** | ❌ Yes, confusing | ✅ No, clean redirect |
| **Auto Logout?** | ❌ No, manual needed | ✅ Yes, automatic |
| **Session Cleared?** | ✅ Yes (in background) | ✅ Yes (in background) |
| **Navigation Works?** | ❌ No, stuck | ✅ Yes, redirected |
| **User Can Login Again?** | ❌ After app restart | ✅ Immediately |
| **Back Navigation?** | ❌ Goes to old screens | ✅ Empty (as intended) |
| **Test Case Result?** | **❌ FAIL** | **✅ PASS** |

---

## Summary

**Before:** Session cleared silently, but UI didn't react → User stuck
**After:** Session cleared AND UI reacts immediately → User auto-redirected

The fix bridges the gap between **backend state change** and **UI update** by adding a reactive listener that was missing.

