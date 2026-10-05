package org.example.project.presentation.viewmodels

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.example.project.data.repositories.AuthRetryHandler
import org.example.project.data.repositories.AuthRepository
import org.example.project.data.repositories.BannerRepository
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

object NetworkModule {
    private val refreshMutex = Mutex()
    @Volatile
    private var repositoriesConfigured = false

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
                    encodeDefaults = true
                })
            }
        }
    }

    val chatRepository: ChatRepository by lazy { ChatRepository(httpClient) }
    val productRepository: ProductRepository by lazy { ProductRepository(httpClient) }
    val orderRepository: DesktopOrderRepository by lazy { DesktopOrderRepository(httpClient) }
    val dashboardRepository: DesktopDashboardRepository by lazy { DesktopDashboardRepository(httpClient) }
    val posRepository: PosRepository by lazy { PosRepository(httpClient) }
    val couponRepository: CouponAdminRepository by lazy { CouponAdminRepository(httpClient) }
    val financeRepository: FinanceRepository by lazy { FinanceRepository(httpClient) }
    val operationsRepository: OperationsRepository by lazy { OperationsRepository(httpClient) }
    val personnelRepository: PersonnelRepository by lazy { PersonnelRepository(httpClient) }
    val bannerRepository: BannerRepository by lazy { BannerRepository(httpClient) }
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
            personnelRepository,
            operationsRepository,
            bannerRepository
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
            personnelRepository.setAuthRetryHandler(authRetryHandler)
            bannerRepository.setAuthRetryHandler(authRetryHandler)
            repositoriesConfigured = true
        }
    }

    fun chatViewModel(): ChatViewModel {
        ensureRepositoryAuthRetryConfigured()
        return ChatViewModel(chatRepository, productRepository, sessionManager)
    }
    
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
        return OrdersViewModel(orderRepository, posRepository)
    }

    fun dashboardViewModel(): DashboardViewModel {
        ensureRepositoryAuthRetryConfigured()
        return DashboardViewModel(dashboardRepository)
    }

    fun posViewModel(): PosViewModel {
        ensureRepositoryAuthRetryConfigured()
        return PosViewModel(productRepository, posRepository, orderRepository, couponRepository)
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

    fun bannerViewModel(): BannerViewModel {
        ensureRepositoryAuthRetryConfigured()
        return BannerViewModel(bannerRepository)
    }

    fun settingsViewModel(): SettingsViewModel {
        ensureRepositoryAuthRetryConfigured()
        return SettingsViewModel(settingsRepository)
    }
}
