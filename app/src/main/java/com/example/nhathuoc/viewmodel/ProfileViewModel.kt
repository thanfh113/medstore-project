package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.ProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for user profile management
 */
class ProfileViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<UiState<UserProfileDto>>(UiState.Idle)
    val profileState: StateFlow<UiState<UserProfileDto>> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<UserProfileDto>>(UiState.Idle)
    val updateState: StateFlow<UiState<UserProfileDto>> = _updateState.asStateFlow()

    private val _addressesState = MutableStateFlow<UiState<List<UserAddressDto>>>(UiState.Idle)
    val addressesState: StateFlow<UiState<List<UserAddressDto>>> = _addressesState.asStateFlow()

    private val _addAddressState = MutableStateFlow<UiState<UserAddressDto>>(UiState.Idle)
    val addAddressState: StateFlow<UiState<UserAddressDto>> = _addAddressState.asStateFlow()

    private val _deleteAddressState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val deleteAddressState: StateFlow<UiState<String>> = _deleteAddressState.asStateFlow()

    val userName: StateFlow<String> = profileState
        .map { (it as? UiState.Success)?.data?.fullName ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userPhone: StateFlow<String> = profileState
        .map { (it as? UiState.Success)?.data?.phone ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    init {
        loadProfile()
        loadAddresses()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading

            when (val result = profileRepository.getProfile()) {
                is NetworkResult.Success -> {
                    _profileState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _profileState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _profileState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi tải hồ sơ"
                    )
                }
            }
        }
    }

    fun updateProfile(
        fullName: String,
        email: String,
        dateOfBirth: String? = null,
        phone: String? = null
    ) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading

            when (val result = profileRepository.updateProfile(fullName, email, dateOfBirth, phone)) {
                is NetworkResult.Success -> {
                    _updateState.value = UiState.Success(result.data)
                    loadProfile()
                }
                is NetworkResult.Error -> {
                    _updateState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _updateState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi cập nhật hồ sơ"
                    )
                }
            }
        }
    }

    fun uploadAvatar(imagePath: String) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading

            when (val result = profileRepository.uploadAvatar(imagePath)) {
                is NetworkResult.Success -> {
                    _updateState.value = UiState.Success(result.data)
                    loadProfile()
                }
                is NetworkResult.Error -> {
                    _updateState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _updateState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi tải ảnh"
                    )
                }
            }
        }
    }

    fun loadAddresses() {
        viewModelScope.launch {
            _addressesState.value = UiState.Loading

            when (val result = profileRepository.getAddresses()) {
                is NetworkResult.Success -> {
                    _addressesState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _addressesState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _addressesState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi tải địa chỉ"
                    )
                }
            }
        }
    }

    fun addAddress(
        fullName: String,
        phone: String,
        address: String,
        district: String,
        city: String,
        type: AddressType
    ) {
        viewModelScope.launch {
            _addAddressState.value = UiState.Loading

            when (val result = profileRepository.addAddress(fullName, phone, address, district, city, type)) {
                is NetworkResult.Success -> {
                    _addAddressState.value = UiState.Success(result.data)
                    loadAddresses()
                }
                is NetworkResult.Error -> {
                    _addAddressState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _addAddressState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi thêm địa chỉ"
                    )
                }
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _deleteAddressState.value = UiState.Loading

            when (val result = profileRepository.deleteAddress(addressId)) {
                is NetworkResult.Success -> {
                    _deleteAddressState.value = UiState.Success(result.data)
                    loadAddresses()
                }
                is NetworkResult.Error -> {
                    _deleteAddressState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _deleteAddressState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi xóa địa chỉ"
                    )
                }
            }
        }
    }

    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            when (profileRepository.setDefaultAddress(addressId)) {
                is NetworkResult.Success -> {
                    loadAddresses()
                }
                else -> {}
            }
        }
    }

    fun clearUpdateState() {
        _updateState.value = UiState.Idle
    }

    fun clearAddAddressState() {
        _addAddressState.value = UiState.Idle
    }

    fun clearDeleteAddressState() {
        _deleteAddressState.value = UiState.Idle
    }
}

class ProfileViewModelFactory(
    private val profileRepository: ProfileRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
