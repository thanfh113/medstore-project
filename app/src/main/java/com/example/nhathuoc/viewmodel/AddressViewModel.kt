package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.AddressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val addressRepository: AddressRepository
) : ViewModel() {

    private val _addresses = MutableStateFlow<List<UserAddress>>(emptyList())
    val addresses: StateFlow<List<UserAddress>> = _addresses.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        viewModelScope.launch {
            when (val result = addressRepository.getUserAddresses()) {
                is NetworkResult.Success   -> _addresses.value = result.data
                is NetworkResult.Error     -> _uiState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _uiState.value = UiState.Error(result.e.message ?: "Lỗi không xác định")
            }
        }
    }

    fun addAddress(request: AddAddressRequest) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = addressRepository.addAddress(request)) {
                is NetworkResult.Success   -> { _uiState.value = UiState.Success(Unit); loadAddresses() }
                is NetworkResult.Error     -> _uiState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _uiState.value = UiState.Error(result.e.message ?: "Lỗi không xác định")
            }
        }
    }

    fun updateAddress(id: String, request: AddAddressRequest) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = addressRepository.updateAddress(id, request)) {
                is NetworkResult.Success   -> { _uiState.value = UiState.Success(Unit); loadAddresses() }
                is NetworkResult.Error     -> _uiState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _uiState.value = UiState.Error(result.e.message ?: "Lỗi không xác định")
            }
        }
    }

    fun deleteAddress(id: String) {
        viewModelScope.launch {
            _addresses.value = _addresses.value.filter { it.id != id }
            when (val result = addressRepository.deleteAddress(id)) {
                is NetworkResult.Success   -> {}
                is NetworkResult.Error     -> { loadAddresses(); _uiState.value = UiState.Error(result.message) }
                is NetworkResult.Exception -> { loadAddresses(); _uiState.value = UiState.Error(result.e.message ?: "Lỗi không xác định") }
            }
        }
    }

    fun clearUiState() { _uiState.value = UiState.Idle }
}
