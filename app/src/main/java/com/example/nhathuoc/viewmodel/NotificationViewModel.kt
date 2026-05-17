package com.example.nhathuoc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.NotificationDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<NotificationDto>>>(UiState.Idle)
    val state: StateFlow<UiState<List<NotificationDto>>> = _state.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationDto>>(emptyList())
    val notifications: StateFlow<List<NotificationDto>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications(page: Int = 1, limit: Int = 30) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val response = apiService.getNotifications(page = page, limit = limit)
                if (response.isSuccessful) {
                    val items: List<NotificationDto> = response.body()?.data ?: emptyList()
                    _notifications.value = items
                    _unreadCount.value = items.count { n: NotificationDto -> !n.isRead }
                    _state.value = UiState.Success(items)
                } else {
                    Log.w("NotificationVM", "API error: ${response.code()}")
                    _state.value = UiState.Error("Không thể tải thông báo (${response.code()})")
                }
            } catch (e: Exception) {
                Log.e("NotificationVM", "Exception: ${e.message}")
                _state.value = UiState.Error("Không thể kết nối máy chủ")
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                apiService.markNotificationAsRead(notificationId)
                _notifications.value = _notifications.value.map { n: NotificationDto ->
                    if (n.id == notificationId) n.copy(isRead = true) else n
                }
                _unreadCount.value = _notifications.value.count { n: NotificationDto -> !n.isRead }
            } catch (e: Exception) {
                Log.e("NotificationVM", "markAsRead error: ${e.message}")
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                apiService.markAllNotificationsAsRead()
                _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                _unreadCount.value = 0
            } catch (e: Exception) {
                Log.e("NotificationVM", "markAllAsRead error: ${e.message}")
                // Optimistic local update even if API fails
                _notifications.value = _notifications.value.map { n: NotificationDto -> n.copy(isRead = true) }
                _unreadCount.value = 0
            }
        }
    }

    /** Silently fetch page 1 and prepend only brand-new items — no Loading state, no flicker. */
    fun refreshSilently() {
        if (_state.value is UiState.Loading) return
        viewModelScope.launch {
            try {
                val response = apiService.getNotifications(page = 1, limit = 30)
                if (response.isSuccessful) {
                    val fetched = response.body()?.data ?: return@launch
                    val existingIds = _notifications.value.map { it.id }.toSet()
                    val brandNew = fetched.filter { it.id !in existingIds }
                    if (brandNew.isNotEmpty()) {
                        _notifications.value = brandNew + _notifications.value
                        _unreadCount.value = _notifications.value.count { !it.isRead }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    /** Remove a notification locally (swipe-to-dismiss style) */
    fun dismissNotification(notificationId: String) {
        _notifications.value = _notifications.value.filter { n: NotificationDto -> n.id != notificationId }
        _unreadCount.value = _notifications.value.count { n: NotificationDto -> !n.isRead }
    }
}
