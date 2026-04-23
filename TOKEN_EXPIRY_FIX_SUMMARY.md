# Token Expiry/Invalid Session Fix - Summary

## Problem
When a user's refresh token becomes invalid or expired (e.g., after backend restart), the Android app would:
- Show an error message
- **NOT redirect to LoginScreen**
- Remain stuck on the current screen with no way to recover
- **FAIL the test case**

## Root Cause
The Android app was missing a **global authentication state monitor**. While the `AuthInterceptor` correctly cleared the session when token refresh failed, no component was listening to this state change to trigger navigation.

The desktop app had this feature (See `App.kt`):
```kotlin
val session by NetworkModule.sessionManager.session.collectAsState()
if (session == null) {
    LoginScreen(...)
}
```

But Android app did NOT have this.

## Solution Implemented
Added **global authentication state monitoring** in `AppnavHost.kt`:

### Changes Made:
1. **Added Imports:**
   ```kotlin
   import androidx.compose.runtime.LaunchedEffect
   import com.example.nhathuoc.data.local.SessionManager
   ```

2. **Added Global Session Monitoring:**
   ```kotlin
   @Composable
   fun AppnavHost(navController: NavHostController) {
       val pendingProductContext = remember { mutableStateOf<ChatProductContext?>(null) }
       val context = LocalContext.current
       
       // NEW: Global authentication state monitoring
       val sessionManager = remember { SessionManager(context) }
       val isLoggedIn = sessionManager.isLoggedIn.collectAsState(initial = false)
       
       LaunchedEffect(isLoggedIn.value) {
           // If logged out and not already on LoginScreen or RegisterScreen, navigate to LoginScreen
           val currentRoute = navController.currentBackStackEntry?.destination?.route
           if (!isLoggedIn.value && currentRoute != null && 
               currentRoute != "LoginScreen" && currentRoute != "RegisterScreen") {
               navController.navigate("LoginScreen") {
                   // Clear back stack so user can't go back to previous authenticated screens
                   popUpTo(0) { inclusive = true }
               }
           }
       }
       
       NavHost(...)
   }
   ```

## How It Works
1. **Monitors Session State:** `SessionManager.isLoggedIn` is a Flow that emits `true` when both access and refresh tokens exist
2. **Listens for Changes:** When `AuthInterceptor` calls `sessionManager.clearSession()`, this Flow automatically emits `false`
3. **Triggers Navigation:** The `LaunchedEffect` detects the change and immediately navigates to LoginScreen
4. **Clears History:** Uses `popUpTo(0) { inclusive = true }` to clear the back stack, preventing user from going back to authenticated screens
5. **Skips Navigation for Login/Register:** Won't navigate if already on those screens (avoid loops)

## Test Case: Token Expiry/Invalid Session

### Setup
1. **Start Backend:** `cd nhathuoc-backend && java -jar build/libs/nhathuoc-backend-all.jar`
2. **Build & Run Android App**
3. **Login successfully** with credentials:
   - Email: `admin@medstore.vn`
   - Password: `Admin@123`

### Test Steps
1. **Perform an authenticated API call** while logged in (e.g., browse products, go to cart, etc.)
2. **Invalidate tokens by restarting backend:**
   ```powershell
   # Kill current backend process
   Stop-Process -ProcessName java -Force
   
   # Restart backend
   java -jar build/libs/nhathuoc-backend-all.jar
   ```
   ✅ Or manually make the refresh token invalid via DB update
   
3. **Trigger another API call** in the app (pull to refresh, click a button that requires auth, etc.)

### Expected Result - ✅ PASS
- ❌ App shows error but doesn't hang
- ✅ Session is automatically cleared (behind the scenes)
- ✅ App automatically redirects to **LoginScreen**
- ✅ User can login again with the same credentials
- ✅ **NO tracing/freezing, smooth transition**

### What Would Fail - ❌ FAIL
- ❌ App shows error AND stays on current screen
- ❌ App doesn't redirect to LoginScreen
- ❌ User remains stuck with invalid token
- ❌ Back button works after logout (back stack not cleared)

## Additional Notes

### Flow Architecture
```
Backend (token invalidates)
     ↓
API Request (returns 401)
     ↓
AuthInterceptor (catches 401)
     ↓
Tries token refresh
     ↓
Refresh fails (returns error)
     ↓
sessionManager.clearSession()  ← Session Flow emits FALSE
     ↓
AppnavHost LaunchedEffect (listens to isLoggedIn)
     ↓
Detects FALSE → navigate("LoginScreen")
     ↓
User is back at login screen
```

### Files Modified
- `D:\ĐATN\nhathuoc\nhathuoc\app\src\main\java\com\example\nhathuoc\navigation\AppnavHost.kt`
  - Added global authentication monitoring with LaunchedEffect
  - Ensures any session clear triggers immediate logout flow

### Related Components (Not Modified)
- `AuthInterceptor.kt` - Already correctly clears session on token refresh failure
- `SessionManager.kt` - Already provides `isLoggedIn` Flow
- `AuthViewModel.kt` - Already handles login/logout state

## Verification

The fix is automatically validated when:
1. App is built and runs
2. User logs in, then backend tokens are invalidated
3. Any API call is made
4. App redirects to LoginScreen without hanging

No additional configuration needed! ✅

