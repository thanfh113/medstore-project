package org.example.project.presentation.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.example.project.data.repositories.AuthResponseDto
import org.example.project.data.repositories.CouponAdminRepository
import org.example.project.data.repositories.DesktopDashboardRepository
import org.example.project.data.repositories.DesktopOrderRepository
import org.example.project.data.repositories.FinanceRepository
import org.example.project.data.repositories.OperationsRepository
import org.example.project.data.repositories.PersonnelRepository
import org.example.project.data.repositories.PosRepository
import org.example.project.data.repositories.ProductRepository
import org.example.project.data.repositories.SyncRepository

data class DesktopSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val phone: String,
    val fullName: String?,
    val email: String?,
    val role: String
)

class SessionManager(
    private val productRepository: ProductRepository,
    private val orderRepository: DesktopOrderRepository,
    private val dashboardRepository: DesktopDashboardRepository,
    private val posRepository: PosRepository,
    private val couponRepository: CouponAdminRepository,
    private val financeRepository: FinanceRepository,
    private val syncRepository: SyncRepository,
    private val personnelRepository: PersonnelRepository,
    private val operationsRepository: OperationsRepository
) {
    private val _session = MutableStateFlow<DesktopSession?>(null)
    val session: StateFlow<DesktopSession?> = _session.asStateFlow()

    fun save(auth: AuthResponseDto) {
        val newSession = DesktopSession(
            accessToken = auth.accessToken,
            refreshToken = auth.refreshToken,
            userId = auth.user.id,
            phone = auth.user.phone,
            fullName = auth.user.fullName,
            email = auth.user.email,
            role = auth.user.role
        )
        productRepository.setAuthToken(newSession.accessToken)
        orderRepository.setAuthToken(newSession.accessToken)
        dashboardRepository.setAuthToken(newSession.accessToken)
        posRepository.setAuthToken(newSession.accessToken)
        couponRepository.setAuthToken(newSession.accessToken)
        financeRepository.setAuthToken(newSession.accessToken)
        syncRepository.setAuthToken(newSession.accessToken)
        personnelRepository.setAuthToken(newSession.accessToken)
        operationsRepository.setAuthToken(newSession.accessToken)
        _session.value = newSession
    }

    fun updateTokens(accessToken: String, refreshToken: String): Boolean {
        val current = _session.value ?: return false
        val updated = current.copy(accessToken = accessToken, refreshToken = refreshToken)
        productRepository.setAuthToken(accessToken)
        orderRepository.setAuthToken(accessToken)
        dashboardRepository.setAuthToken(accessToken)
        posRepository.setAuthToken(accessToken)
        couponRepository.setAuthToken(accessToken)
        financeRepository.setAuthToken(accessToken)
        syncRepository.setAuthToken(accessToken)
        personnelRepository.setAuthToken(accessToken)
        operationsRepository.setAuthToken(accessToken)
        _session.value = updated
        return true
    }

    fun clear() {
        productRepository.setAuthToken(null)
        orderRepository.setAuthToken(null)
        dashboardRepository.setAuthToken(null)
        posRepository.setAuthToken(null)
        couponRepository.setAuthToken(null)
        financeRepository.setAuthToken(null)
        syncRepository.setAuthToken(null)
        personnelRepository.setAuthToken(null)
        operationsRepository.setAuthToken(null)
        _session.value = null
    }

    fun updateRole(role: String) {
        _session.update { current -> current?.copy(role = role) }
    }
}
