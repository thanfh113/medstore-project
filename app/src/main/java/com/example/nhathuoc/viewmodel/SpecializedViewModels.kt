package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.RewardRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for reward points management
 */
class RewardViewModel(
    private val rewardRepository: RewardRepository
) : ViewModel() {

    private val _rewardState = MutableStateFlow<UiState<RewardDto>>(UiState.Idle)
    val rewardState: StateFlow<UiState<RewardDto>> = _rewardState.asStateFlow()

    private val _redeemState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val redeemState: StateFlow<UiState<String>> = _redeemState.asStateFlow()

    private val _historyState = MutableStateFlow<UiState<List<RewardHistoryDto>>>(UiState.Idle)
    val historyState: StateFlow<UiState<List<RewardHistoryDto>>> = _historyState.asStateFlow()

    val points: StateFlow<Int> = rewardState
        .map { (it as? UiState.Success)?.data?.totalPoints ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pointValue: StateFlow<Double> = rewardState
        .map { (it as? UiState.Success)?.data?.let { it.totalPoints * 1000.0 } ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        loadRewards()
    }

    fun loadRewards() {
        viewModelScope.launch {
            _rewardState.value = UiState.Loading

            when (val result = rewardRepository.getRewards()) {
                is NetworkResult.Success -> {
                    _rewardState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _rewardState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _rewardState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi tải điểm thưởng"
                    )
                }
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _historyState.value = UiState.Loading

            when (val result = rewardRepository.getRewardHistory()) {
                is NetworkResult.Success -> {
                    _historyState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _historyState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _historyState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi tải lịch sử"
                    )
                }
            }
        }
    }

    fun redeemPoints(points: Int) {
        viewModelScope.launch {
            _redeemState.value = UiState.Loading

            when (val result = rewardRepository.redeemPoints(points)) {
                is NetworkResult.Success -> {
                    _redeemState.value = UiState.Success(result.data)
                    loadRewards()
                    loadHistory()
                }
                is NetworkResult.Error -> {
                    _redeemState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _redeemState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi đổi điểm"
                    )
                }
            }
        }
    }

    fun clearRedeemState() {
        _redeemState.value = UiState.Idle
    }
}

class RewardViewModelFactory(
    private val rewardRepository: RewardRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RewardViewModel::class.java)) {
            return RewardViewModel(rewardRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * ViewModel for notifications
 */
class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notificationsState = MutableStateFlow<UiState<List<NotificationDto>>>(UiState.Idle)
    val notificationsState: StateFlow<UiState<List<NotificationDto>>> = _notificationsState.asStateFlow()

    private val _markReadState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val markReadState: StateFlow<UiState<String>> = _markReadState.asStateFlow()

    val unreadCount: StateFlow<Int> = notificationsState
        .map { state ->
            if (state is UiState.Success) {
                state.data.count { !it.isRead }
            } else 0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _notificationsState.value = UiState.Loading

            when (val result = notificationRepository.getNotifications()) {
                is NetworkResult.Success -> {
                    _notificationsState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _notificationsState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _notificationsState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi tải thông báo"
                    )
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            _markReadState.value = UiState.Loading

            when (val result = notificationRepository.markAsRead(notificationId)) {
                is NetworkResult.Success -> {
                    _markReadState.value = UiState.Success("Đã đánh dấu là đã đọc")
                    loadNotifications()
                }
                is NetworkResult.Error -> {
                    _markReadState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _markReadState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi đánh dấu"
                    )
                }
            }
        }
    }

    fun clearMarkReadState() {
        _markReadState.value = UiState.Idle
    }
}

class NotificationViewModelFactory(
    private val notificationRepository: NotificationRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            return NotificationViewModel(notificationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * ViewModel for doctor consultations
 */
class ConsultViewModel(
    private val consultRepository: ConsultRepository
) : ViewModel() {

    private val _doctorsState = MutableStateFlow<UiState<List<DoctorDto>>>(UiState.Idle)
    val doctorsState: StateFlow<UiState<List<DoctorDto>>> = _doctorsState.asStateFlow()

    private val _doctorDetailState = MutableStateFlow<UiState<DoctorDto>>(UiState.Idle)
    val doctorDetailState: StateFlow<UiState<DoctorDto>> = _doctorDetailState.asStateFlow()

    private val _consultationsState = MutableStateFlow<UiState<List<ConsultationDto>>>(UiState.Idle)
    val consultationsState: StateFlow<UiState<List<ConsultationDto>>> = _consultationsState.asStateFlow()

    private val _bookState = MutableStateFlow<UiState<ConsultationDto>>(UiState.Idle)
    val bookState: StateFlow<UiState<ConsultationDto>> = _bookState.asStateFlow()

    val searchQuery = MutableStateFlow("")
    val selectedSpecialization = MutableStateFlow<String?>(null)

    init {
        loadDoctors()
    }

    fun loadDoctors() {
        viewModelScope.launch {
            _doctorsState.value = UiState.Loading

            when (val result = consultRepository.getDoctors(
                search = searchQuery.value.ifEmpty { null },
                specialization = selectedSpecialization.value
            )) {
                is NetworkResult.Success -> {
                    _doctorsState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _doctorsState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _doctorsState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi tải bác sĩ"
                    )
                }
            }
        }
    }

    fun getDoctorDetail(doctorId: String) {
        viewModelScope.launch {
            _doctorDetailState.value = UiState.Loading

            when (val result = consultRepository.getDoctorDetail(doctorId)) {
                is NetworkResult.Success -> {
                    _doctorDetailState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _doctorDetailState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _doctorDetailState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi tải thông tin bác sĩ"
                    )
                }
            }
        }
    }

    fun loadConsultations() {
        viewModelScope.launch {
            _consultationsState.value = UiState.Loading

            when (val result = consultRepository.getConsultations()) {
                is NetworkResult.Success -> {
                    _consultationsState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _consultationsState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _consultationsState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi tải tư vấn"
                    )
                }
            }
        }
    }

    fun bookConsultation(
        doctorId: String,
        scheduledAt: String,
        sessionType: String,
        reason: String
    ) {
        viewModelScope.launch {
            _bookState.value = UiState.Loading

            when (val result = consultRepository.bookConsultation(
                doctorId = doctorId,
                scheduledAt = scheduledAt,
                sessionType = sessionType,
                reason = reason
            )) {
                is NetworkResult.Success -> {
                    _bookState.value = UiState.Success(result.data)
                    loadConsultations()
                }
                is NetworkResult.Error -> {
                    _bookState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _bookState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi đặt tư vấn"
                    )
                }
            }
        }
    }

    fun clearBookState() {
        _bookState.value = UiState.Idle
    }
}

class ConsultViewModelFactory(
    private val consultRepository: ConsultRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConsultViewModel::class.java)) {
            return ConsultViewModel(consultRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
