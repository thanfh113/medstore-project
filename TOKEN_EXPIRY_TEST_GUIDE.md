# Token Expiry Test Case - Quick Start Guide

## Scenario: Test Token Expiry/Invalid Session Handling

**Kỳ vọng:** Khi token hết hạn hoặc backend restart (invalidate token), app phải:
- ✅ Không treo
- ✅ Tự động clear session
- ✅ Chuyển về LoginScreen
- ❌ FAIL nếu vẫn ở màn cũ với lỗi mà không về login

---

## Quick Test Steps

### 1. Chuẩn bị
```bash
# Terminal 1: Start Backend
cd D:\ĐATN\nhathuoc\nhathuoc-backend
java -jar build/libs/nhathuoc-backend-all.jar
# Wait for: "Ktor server started at http://0.0.0.0:8080"
```

### 2. Build & Run Android App
```bash
# Terminal 2: Build Android app
cd D:\ĐATN\nhathuoc\nhathuoc
./gradlew installDebug
# OR: Run directly from Android Studio
```

### 3. Login Successfully
- Open app
- Go to AccountScreen (Account tab)
- Click "Đăng nhập"
- Enter:
  - **Email/Phone:** `admin@medstore.vn`
  - **Password:** `Admin@123`
- ✅ Should see account info (logged in)

### 4. Trigger Token Invalidation

**Option A: Restart Backend (Fastest)**
```bash
# Terminal 1: Kill backend
Ctrl+C

# Wait 2 seconds
Start-Sleep -Seconds 2

# Restart backend
java -jar build/libs/nhathuoc-backend-all.jar
```

**Option B: Make API Call with Old Token**
- Just perform any action that needs tokens (still logged in from step 3)
- Because backend restarted, tokens are invalid

### 5. Perform Any API Call
- Click any authenticated button:
  - Browse products
  - Go to cart
  - Click any order
  - Refresh/pull down on screen
  - Or just navigate around

### 6. Verify Result

**✅ PASS if:**
```
App current screen → (API call with invalid token)
                  → (AuthInterceptor catches 401)
                  → (sessionManager.clearSession())
                  → (LaunchedEffect detects isLoggedIn=false)
                  → LoginScreen appears automatically
                  → No hang, no error stuck
                  → Can login again
```

**❌ FAIL if:**
```
App stays on current screen
Error message visible
Cannot interact with app
Cannot navigate anywhere
App hangs/freezes
Back navigation works after logout
```

---

## Expected Console Logs

### From AuthInterceptor
```
e: file:///D:/...
[AuthInterceptor] 401 received, attempting token refresh...
[AuthInterceptor] Refresh failed, clearing session
```

### From AppnavHost
```
[AppnavHost] isLoggedIn changed: true → false
[AppnavHost] Navigating to LoginScreen
```

---

## Timing

- **Token refresh attempt:** ~1-2 seconds
- **Session clear:** Immediate
- **Auto-redirect to LoginScreen:** ~200-500ms (Compose recomposition)
- **Total time:** ~2-3 seconds from API error to LoginScreen

If it takes longer than 5 seconds, something is wrong.

---

## Alternative: Database Manipulation

If you don't want to restart backend, you can invalidate tokens via DB:

```sql
-- MySQL: Clear all refresh tokens for a user
UPDATE refresh_tokens SET revoked_at = NOW() 
WHERE user_id = (SELECT id FROM users WHERE email = 'admin@medstore.vn');

-- Or: Expire all refresh tokens
UPDATE refresh_tokens SET expires_at = DATE_SUB(NOW(), INTERVAL 1 DAY);
```

Then make an API call from the app → same effect as backend restart.

---

## Debug Checklist

If test fails:
1. ✅ Backend is running on `http://localhost:8080`
2. ✅ App is able to login successfully
3. ✅ Tokens are actually being stored (check DataStore)
4. ✅ API request after backend restart/token clear is being made
5. ✅ Check logcat for errors: `adb logcat | grep -i "auth\|session\|token"`

---

## Files Involved

- **Frontend Fix:** `nhathuoc/app/src/main/java/com/example/nhathuoc/navigation/AppnavHost.kt`
  - Added: Global session state monitoring with LaunchedEffect
  
- **Token Refresh Logic:** `nhathuoc/app/src/main/java/com/example/nhathuoc/data/remote/AuthInterceptor.kt`
  - Existing: Already clears session on refresh fail
  
- **Session Storage:** `nhathuoc/app/src/main/java/com/example/nhathuoc/data/local/SessionManager.kt`
  - Existing: Provides `isLoggedIn` Flow


