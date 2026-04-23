# Desktop E2E Checklist - POS + Online Orders + Role Matrix

Backend base: `http://127.0.0.1:8080/api/v1`

## Test Accounts
- ADMIN: `admin@medstore.vn / Admin@123`
- EMPLOYEE: `employee@medstore.vn / Employee@123`

## A. Startup and Login
- [ ] Backend up and health returns 200 (`GET /`)
- [ ] Desktop app opens login screen
- [ ] ADMIN login success
- [ ] EMPLOYEE login success
- [ ] Invalid password shows error and blocks login

## B. Role-based Menu Visibility
- [ ] ADMIN sees: `Tong quan`, `POS`, `Don hang`, `San pham`, `Chat`, `Coupon`, `Tai chinh`, `Nhan su`, `Cai dat`
- [ ] EMPLOYEE sees: `POS`, `Don hang`, `San pham`, `Chat`
- [ ] EMPLOYEE cannot access `Coupon` and `Tai chinh`
- [ ] Force navigate to `coupons`/`finance` as EMPLOYEE (state/deep-link simulation) -> app auto-fallback to `orders`

## C. Product Management (ADMIN and EMPLOYEE)
- [ ] Load product list from backend
- [ ] Create product with image
- [ ] Update product and update image
- [ ] Delete product
- [ ] Verify backend log has create/update/delete requests

## D. POS Flow (Offline-at-counter business flow)
- [ ] Open `POS` screen and load product list
- [ ] Add products to cart and adjust quantities
- [ ] Apply valid coupon code (order-level)
- [ ] Create POS order (`POST /internal/pos/orders`) with `order_channel=POS`
- [ ] For CASH: confirm cash payment (`POST /internal/pos/orders/{id}/confirm-cash`)
- [ ] Verify order becomes `DELIVERED`, `paymentStatus=COMPLETED`
- [ ] Verify stock is reduced after cash confirmation
- [ ] For non-CASH (`MOMO`/`VNPAY`): create POS order success, app clears cart and shows pending payment message

## E. Online Order Handling (Internal OMS)
- [ ] Load online order list (`GET /internal/orders`)
- [ ] View order detail (`GET /internal/orders/{id}`)
- [ ] Update status (`POST /internal/orders/{id}/status`)
- [ ] Confirm no crash when orders include POS walk-in customer (`user_id` null)

## F. Coupon Admin
- [ ] ADMIN creates coupon (`POST /internal/coupons`)
- [ ] Coupon appears in list (`GET /internal/coupons`)
- [ ] Validate coupon with order total (`POST /internal/coupons/validate`)
- [ ] Usage limits and min-order rules are enforced
- [ ] EMPLOYEE create coupon request is rejected (403)

## G. Finance Admin Dashboard
- [ ] ADMIN opens `Tai chinh` screen
- [ ] Finance summary loads (`GET /admin/finance`)
- [ ] Values are present: `grossRevenue`, `onlineRevenue`, `posRevenue`, `totalExpenses`, `netProfit`
- [ ] Verify `netProfit = grossRevenue - totalExpenses`

## H. Auth Expiration / Refresh
- [ ] Force expired access token
- [ ] First protected request gets 401 then auto-refresh succeeds
- [ ] Request retries successfully without manual relogin
- [ ] If refresh fails: session clears and app returns to login

## I. Log Correlation (Pass/Fail Evidence)
For each failed step, capture:
- Step name
- UI error text
- API endpoint + status + response body
- Backend log lines with timestamp
- Root cause and short fix proposal

## Exit Criteria
- [ ] All mandatory items in sections A-G pass for ADMIN
- [ ] EMPLOYEE permissions follow role matrix
- [ ] Token refresh behavior in section H is confirmed
- [ ] No critical crash in POS + online order workflows

