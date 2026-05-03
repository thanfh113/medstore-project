package org.example.project.presentation.viewmodels

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.example.project.data.local.OfflineStore
import org.example.project.data.network.SyncApiService
import org.example.project.data.repositories.AuthRetryHandler
import org.example.project.data.repositories.AuthRepository
import org.example.project.data.repositories.ChatRepository
import org.example.project.data.repositories.CouponAdminRepository
import org.example.project.data.repositories.DesktopDashboardRepository
import org.example.project.data.repositories.DesktopOrderRepository
import org.example.project.data.repositories.FinanceRepository
import org.example.project.data.repositories.OperationsRepository
import org.example.project.data.repositories.PersonnelRepository
import org.example.project.data.repositories.PosRepository
import org.example.project.data.repositories.ProductRepository
import org.example.project.data.repositories.SettingsRepository
import org.example.project.data.repositories.SyncRepository

object NetworkModule {
    enum class SyncUiStateType {
        SYNCING,
        OFFLINE,
        PENDING_SYNC,
        SYNC_FAIL,
        SYNCED
    }

    data class SyncUiState(
        val type: SyncUiStateType,
        val message: String
    )

    private val refreshMutex = Mutex()
    private const val SYNC_DELAY_MIN_MS = 15_000L
    private const val SYNC_DELAY_MAX_MS = 300_000L
    @Volatile
    private var repositoriesConfigured = false
    private val _syncUiState = MutableStateFlow(SyncUiState(SyncUiStateType.SYNCED, "Dong bo on dinh"))
    val syncUiState: StateFlow<SyncUiState> = _syncUiState.asStateFlow()
    private val _pendingOutboxCount = MutableStateFlow(0)
    val pendingOutboxCount: StateFlow<Int> = _pendingOutboxCount.asStateFlow()

    val httpClient: HttpClient by lazy {
        HttpClient(CIO) {
            install(WebSockets) {
                pingInterval = 20_000L
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }

    private val offlineStore: OfflineStore by lazy { OfflineStore() }
    private val syncApiService: SyncApiService by lazy { SyncApiService(httpClient) }
    val syncRepository: SyncRepository by lazy { SyncRepository(offlineStore, syncApiService) }

    val chatRepository: ChatRepository by lazy { ChatRepository(httpClient) }
    val productRepository: ProductRepository by lazy { ProductRepository(httpClient, syncRepository) }
    val orderRepository: DesktopOrderRepository by lazy { DesktopOrderRepository(httpClient, syncRepository) }
    val dashboardRepository: DesktopDashboardRepository by lazy { DesktopDashboardRepository(httpClient) }
    val posRepository: PosRepository by lazy { PosRepository(httpClient) }
    val couponRepository: CouponAdminRepository by lazy { CouponAdminRepository(httpClient) }
    val financeRepository: FinanceRepository by lazy { FinanceRepository(httpClient) }
    val operationsRepository: OperationsRepository by lazy { OperationsRepository(httpClient) }
    val personnelRepository: PersonnelRepository by lazy { PersonnelRepository(httpClient) }
    val authRepository: AuthRepository by lazy { AuthRepository(httpClient) }
    val settingsRepository: SettingsRepository by lazy { 
        SettingsRepository(httpClient, tokenProvider = { authRepository.getCachedAccessToken() }) 
    }
    val sessionManager: SessionManager by lazy {
        SessionManager(
            productRepository,
            orderRepository,
            dashboardRepository,
            posRepository,
            couponRepository,
            financeRepository,
            syncRepository,
            personnelRepository,
            operationsRepository
        )
    }

    private val authRetryHandler: AuthRetryHandler by lazy {
        object : AuthRetryHandler {
            override suspend fun refreshAccessToken(): String? {
                return refreshMutex.withLock {
                    val currentSession = sessionManager.session.value ?: return@withLock null
                    val refreshToken = currentSession.refreshToken.takeIf { it.isNotBlank() } ?: return@withLock null

                    val refreshed = authRepository.refreshAccessToken(refreshToken).getOrNull()
                        ?: run {
                            sessionManager.clear()
                            authRepository.clearTokens()
                            return@withLock null
                        }

                    val updated = sessionManager.updateTokens(
                        accessToken = refreshed.accessToken,
                        refreshToken = refreshed.refreshToken
                    )
                    if (!updated) {
                        authRepository.clearTokens()
                        return@withLock null
                    }

                    authRepository.setTokens(refreshed.accessToken, refreshed.refreshToken)
                    refreshed.accessToken
                }
            }

            override fun onAuthFailed() {
                sessionManager.clear()
                authRepository.clearTokens()
            }
        }
    }

    private fun ensureRepositoryAuthRetryConfigured() {
        if (repositoriesConfigured) return
        synchronized(this) {
            if (repositoriesConfigured) return
            productRepository.setAuthRetryHandler(authRetryHandler)
            orderRepository.setAuthRetryHandler(authRetryHandler)
            dashboardRepository.setAuthRetryHandler(authRetryHandler)
            posRepository.setAuthRetryHandler(authRetryHandler)
            couponRepository.setAuthRetryHandler(authRetryHandler)
            financeRepository.setAuthRetryHandler(authRetryHandler)
            operationsRepository.setAuthRetryHandler(authRetryHandler)
            syncRepository.setAuthRetryHandler(authRetryHandler)
            personnelRepository.setAuthRetryHandler(authRetryHandler)
            repositoriesConfigured = true
        }
    }

    fun chatViewModel(): ChatViewModel = ChatViewModel(chatRepository, sessionManager)
    
    fun productsViewModel(): ProductsViewModel {
        ensureRepositoryAuthRetryConfigured()
        return ProductsViewModel(productRepository)
    }

    fun loginViewModel(): LoginViewModel {
        ensureRepositoryAuthRetryConfigured()
        return LoginViewModel(authRepository, sessionManager)
    }

    fun ordersViewModel(): OrdersViewModel {
        ensureRepositoryAuthRetryConfigured()
        return OrdersViewModel(orderRepository)
    }

    fun dashboardViewModel(): DashboardViewModel {
        ensureRepositoryAuthRetryConfigured()
        return DashboardViewModel(dashboardRepository)
    }

    fun posViewModel(): PosViewModel {
        ensureRepositoryAuthRetryConfigured()
        return PosViewModel(productRepository, posRepository, orderRepository, couponRepository, syncRepository)
    }

    fun couponAdminViewModel(): CouponAdminViewModel {
        ensureRepositoryAuthRetryConfigured()
        return CouponAdminViewModel(couponRepository)
    }

    fun financeDashboardViewModel(): FinanceDashboardViewModel {
        ensureRepositoryAuthRetryConfigured()
        return FinanceDashboardViewModel(financeRepository)
    }

    fun personnelViewModel(): PersonnelViewModel {
        ensureRepositoryAuthRetryConfigured()
        return PersonnelViewModel(personnelRepository)
    }

    fun operationsViewModel(): OperationsViewModel {
        ensureRepositoryAuthRetryConfigured()
        return OperationsViewModel(operationsRepository)
    }

    fun syncInventoryAuditViewModel(): SyncInventoryAuditViewModel {
        ensureRepositoryAuthRetryConfigured()
        return SyncInventoryAuditViewModel(syncRepository)
    }

    fun settingsViewModel(): SettingsViewModel {
        ensureRepositoryAuthRetryConfigured()
        return SettingsViewModel(settingsRepository)
    }

    suspend fun syncNow(): Result<Unit> {
        ensureRepositoryAuthRetryConfigured()
        if (sessionManager.session.value == null) return Result.success(Unit)
        _syncUiState.value = SyncUiState(SyncUiStateType.SYNCING, "Dang dong bo")
        val result = syncRepository.syncIncremental()
        val pending = syncRepository.pendingOutboxCount()
        _pendingOutboxCount.value = pending
        _syncUiState.value = when {
            result.isSuccess && pending > 0 -> SyncUiState(SyncUiStateType.PENDING_SYNC, "Cho dong bo ($pending)")
            result.isSuccess -> SyncUiState(SyncUiStateType.SYNCED, "Dong bo on dinh")
            pending > 0 && result.exceptionOrNull()?.message?.contains("connect", ignoreCase = true) == true -> {
                SyncUiState(SyncUiStateType.OFFLINE, "Dang offline, cho dong bo ($pending)")
            }
            else -> SyncUiState(SyncUiStateType.SYNC_FAIL, result.exceptionOrNull()?.message ?: "Dong bo that bai")
        }
        return result
    }

    suspend fun runSyncWorkerStep(previousDelayMs: Long): Long {
        val result = syncNow()
        return if (result.isSuccess) {
            SYNC_DELAY_MIN_MS
        } else {
            (previousDelayMs * 2).coerceIn(SYNC_DELAY_MIN_MS, SYNC_DELAY_MAX_MS)
        }
    }
}
