package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.AddressProvince
import com.example.nhathuoc.data.model.AddressWard
import com.example.nhathuoc.data.repository.ProvinceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddressPickerState(
    val provinces: List<AddressProvince> = emptyList(),
    val wards: List<AddressWard> = emptyList(),
    val selectedProvince: AddressProvince? = null,
    val selectedWard: AddressWard? = null,
    val isLoadingProvinces: Boolean = false,
    val isLoadingWards: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddressPickerViewModel @Inject constructor(
    private val provinceRepository: ProvinceRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AddressPickerState())
    val state: StateFlow<AddressPickerState> = _state.asStateFlow()

    init {
        loadProvinces()
    }

    fun loadProvinces() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingProvinces = true, error = null) }
            provinceRepository.getProvinces()
                .onSuccess { provinces ->
                    _state.update {
                        it.copy(
                            provinces = provinces,
                            isLoadingProvinces = false,
                            error = if (provinces.isEmpty()) "Không tải được danh sách tỉnh/thành" else null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingProvinces = false,
                            error = error.message ?: "Không tải được danh sách tỉnh/thành"
                        )
                    }
                }
        }
    }

    fun selectProvince(province: AddressProvince) {
        _state.update {
            it.copy(
                selectedProvince = province,
                selectedWard = null,
                wards = emptyList(),
                isLoadingWards = true,
                error = null
            )
        }

        viewModelScope.launch {
            provinceRepository.getWards(province.code)
                .onSuccess { wards ->
                    _state.update {
                        it.copy(
                            wards = wards,
                            isLoadingWards = false,
                            error = if (wards.isEmpty()) "Không tải được danh sách xã/phường" else null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingWards = false,
                            error = error.message ?: "Không tải được danh sách xã/phường"
                        )
                    }
                }
        }
    }

    fun selectWard(ward: AddressWard) {
        _state.update { it.copy(selectedWard = ward) }
    }
}
