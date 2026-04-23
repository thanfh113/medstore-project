# Offline Stress + Replay Acceptance Report (Desktop + Backend)

## Scope
- Desktop offline-first sync reliability
- Backend idempotent sync push/pull behavior
- Product delete governance (EMPLOYEE request -> ADMIN approve)
- Admin personnel management (create/update/lock/reset)

## Build Verification (done)
- Backend: `compileKotlin` -> PASS
- Desktop: `:composeApp:compileKotlinJvm` -> PASS

## Stress + Replay Script
- Script: `nhathuoc-backend/offline-sync-stress-replay.ps1`
- Purpose:
  - Push many sync changes
  - Replay same batch
  - Validate replay returns `DUPLICATE`
  - Pull and verify server version progression

## Run Commands
```powershell
Set-Location "D:\ĐATN\nhathuoc\nhathuoc-backend"
.\offline-sync-stress-replay.ps1 -BaseUrl "http://127.0.0.1:8080/api/v1" -Credential "employee@medstore.vn" -Password "Employee@123" -PushCount 50
```

## Expected Results
- First push: `APPLIED` >= push count
- Replay push: `DUPLICATE` >= push count
- Pull: returns expected changes and latest server version increases

## Functional Acceptance Matrix

### 1) Employee Delete Request -> Admin Approve
- Employee click delete on product -> app sends delete request, not direct delete
- Admin sees pending delete requests in product screen panel
- Admin approve -> product removed
- Admin reject -> product remains

### 2) Admin Account Management
- Create user (ADMIN/EMPLOYEE)
- Update user profile/role
- Lock/unlock account
- Reset password

### 3) Offline Sync UX
- Sidebar shows sync state and pending outbox count
- Sync audit screen supports filter/search by product/reason/time
- Inventory conflict records include `resolved by sync at`

## Evidence Checklist (fill during run)
- [ ] Screenshot: Employee submit delete request
- [ ] Screenshot: Admin pending request list
- [ ] Screenshot: Approve + product removed
- [ ] Screenshot: Personnel create/update/lock/reset
- [ ] Screenshot: Sync audit filters + resolved timestamp
- [ ] Log: stress + replay output saved

## Final Gate
- [ ] All checklist items passed
- [ ] No critical crash in offline -> reconnect -> replay
- [ ] Governance rules match role matrix

