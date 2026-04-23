# 🧪 LIVE TEST SCENARIO - Token Expiry Fix

## Status: Backend Ready ✅

**Backend:** Running at `http://localhost:8080`  
**Status:** Login endpoint responding with 200 OK  
**Date:** 2026-04-12

---

## Test Scenario Setup

### ✅ Prerequisites Met
- [x] Backend started and running
- [x] Backend login endpoint verified
- [x] Code fix implemented in AppnavHost.kt
- [x] Ready for Android app testing

---

## Test Steps (Manual on Android)

### Phase 1: Initial Login
1. **Build & Run Android App**
   ```
   cd D:\ĐATN\nhathuoc\nhathuoc
   ./gradlew installDebug
   # OR open in Android Studio and run
   ```

2. **Navigate to Account Screen** (Bottom tab)

3. **Click "Đăng nhập"** if not logged in
   - Email: `admin@medstore.vn`
   - Password: `Admin@123`

4. **Verify:** Should see user account info
   ```
   ✅ Logged in successfully
   ```

---

### Phase 2: Backend Restart (Invalidate Tokens)

**Current Status:** 
- Backend running with fresh instance
- User logged in on Android app with OLD tokens (now invalid)
- Tokens stored in app cache don't exist in new backend

---

### Phase 3: Trigger API Call with Invalid Token

1. **From Account Screen**, try any action:
   - Scroll/refresh the screen
   - Or navigate to another screen and back
   - Or click any button that makes API call

2. **What Will Happen:**
   ```
   App makes API call
   ↓
   Adds old token to Authorization header
   ↓
   Backend receives request
   ↓
   Backend returns: 401 Unauthorized
   (token doesn't exist in new instance)
   ↓
   AuthInterceptor catches 401
   ↓
   Attempts token refresh
   ↓
   Refresh fails (old refresh token invalid)
   ↓
   sessionManager.clearSession()
   ↓
   SessionManager.isLoggedIn emits: false
   ↓
   LaunchedEffect in AppnavHost detects change
   ↓
   ???
   ```

---

## Expected Result: ✅ PASS

### What Should Happen

```
After API call fails with 401:
   ↓
✅ App does NOT hang
✅ No error message stuck on screen
✅ Automatically redirects to LoginScreen
✅ Smooth transition (no flicker)
✅ Can login again immediately with same credentials
```

### Observable Signs of SUCCESS
- [ ] App shows LoginScreen after API call
- [ ] Account data no longer visible
- [ ] Can see login form fields
- [ ] Can type credentials and login again
- [ ] No back navigation to old screen

---

## Failure Indicators: ❌ FAIL

### What Would Mean It Failed

```
❌ App stays on current screen
❌ Shows "401" or "Token Invalid" error
❌ Buttons don't respond
❌ Screen appears frozen
❌ Must restart app to recover
❌ Can go back using back button after logout
```

---

## How to Verify the Fix is Working

### Check 1: Examine Logcat (Android Studio)
```
Look for logs like:
[AuthInterceptor] 401 received
[AuthInterceptor] Token refresh failed, clearing session
[SessionManager] Session cleared
[AppnavHost] isLoggedIn changed: true → false
[AppnavHost] Navigating to LoginScreen
```

### Check 2: Visual Confirmation
```
Before: Account screen showing user info
   ↓ (make API call)
After: LoginScreen appears with email/password fields
```

### Check 3: Functional Test
```
1. See LoginScreen ✅
2. Enter credentials ✅
3. Click login ✅
4. Back on Account screen ✅
```

---

## Implementation Verification

### Code Check
```kotlin
// In AppnavHost.kt (line 52-67):
✅ LaunchedEffect(isLoggedIn.value) {
    if (!isLoggedIn.value && currentRoute != "LoginScreen" && currentRoute != "RegisterScreen") {
        navController.navigate("LoginScreen") {
            popUpTo(0) { inclusive = true }
        }
    }
}
```

### Behavior Flow
```
Backend Token Invalid
    ↓
API Call Fails (401)
    ↓
AuthInterceptor.clearSession()
    ↓
SessionManager Flow: false
    ↓
✅ LaunchedEffect reacts
    ↓
✅ navigate("LoginScreen")
    ↓
✅ popUpTo(0) clears back stack
    ↓
✅ User redirected to login
```

---

## Timing Expectations

| Step | Duration |
|------|----------|
| API request | ~100-200ms |
| Backend 401 response | ~50ms |
| Token refresh attempt | ~100-200ms |
| Refresh failed | ~50ms |
| Session clear | ~50-100ms |
| Flow emission | ~10-50ms |
| LaunchedEffect trigger | ~20-100ms |
| Navigation + recomposition | ~200-500ms |
| **Total** | **~600-1200ms** |

**Expected:** Should see LoginScreen within ~1-2 seconds

---

## What Changed in Code

### File: AppnavHost.kt
```kotlin
// ADDED (Lines 52-67):
val sessionManager = remember { SessionManager(context) }
val isLoggedIn = sessionManager.isLoggedIn.collectAsState(initial = false)

LaunchedEffect(isLoggedIn.value) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    if (!isLoggedIn.value && currentRoute != null && 
        currentRoute != "LoginScreen" && currentRoute != "RegisterScreen") {
        navController.navigate("LoginScreen") {
            popUpTo(0) { inclusive = true }
        }
    }
}
```

---

## Test Confirmation Checklist

After performing the test:

- [ ] **Did app hang?** NO ✅ / YES ❌
- [ ] **Did error message appear?** NO ✅ / YES ❌
- [ ] **Did app redirect to LoginScreen?** YES ✅ / NO ❌
- [ ] **Can you login again?** YES ✅ / NO ❌
- [ ] **Transition was smooth?** YES ✅ / NO ❌
- [ ] **Overall result:** PASS ✅ / FAIL ❌

---

## If Test Fails

### Debug Steps:
1. **Check Android Studio logcat** for errors
2. **Verify AppnavHost.kt** has the fix
3. **Confirm backend is running** and token is actually invalid
4. **Check SessionManager** clearing tokens properly
5. **Review console logs** for "isLoggedIn" state changes

### Common Issues:
- **Issue:** App shows error but stays on screen
  - **Cause:** LaunchedEffect not triggering
  - **Fix:** Verify imports and code in AppnavHost.kt

- **Issue:** App hangs
  - **Cause:** Infinite loop in navigation
  - **Fix:** Check route validation in LaunchedEffect

- **Issue:** Can go back to old screen
  - **Cause:** Back stack not cleared
  - **Fix:** Verify `popUpTo(0) { inclusive = true }`

---

## Success Criteria Summary

✅ **Test PASSES if:**
1. App doesn't hang after invalid token
2. Session is cleared automatically
3. User is redirected to LoginScreen
4. No back navigation to old screens
5. User can login again immediately

❌ **Test FAILS if:**
1. App stays on current screen
2. Error message stuck
3. No redirect to login
4. App requires restart
5. Can navigate back after logout

---

## Test Status

**Date:** 2026-04-12  
**Backend:** ✅ Running and verified  
**Code:** ✅ Implemented and verified  
**Documentation:** ✅ Complete  
**Ready for:** Manual Android app testing  

**Next:** Run on physical device or emulator with Android app built


