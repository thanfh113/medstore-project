package org.example.project.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.repositories.BannerRepository
import org.example.project.data.repositories.DesktopBannerDto
import org.example.project.data.repositories.DesktopBannerUpsertRequest
import java.io.File

data class BannerUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isUploading: Boolean = false,
    val banners: List<DesktopBannerDto> = emptyList(),
    val editingId: String? = null,
    val imageUrl: String = "",
    val linkUrl: String = "",
    val title: String = "",
    val description: String = "",
    val sortOrder: String = "0",
    val isActive: Boolean = true,
    val startDt: String = "",
    val endDt: String = "",
    val successMessage: String? = null,
    val error: String? = null
)

class BannerViewModel(
    private val repository: BannerRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _uiState = MutableStateFlow(BannerUiState())
    val uiState: StateFlow<BannerUiState> = _uiState.asStateFlow()

    init {
        loadBanners()
    }

    fun loadBanners() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getBanners().fold(
                onSuccess = { banners ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            banners = banners.sortedBy { banner -> banner.sortOrder },
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "Không thể tải banner") }
                }
            )
        }
    }

    fun updateImageUrl(value: String) = _uiState.update { it.copy(imageUrl = value, error = null, successMessage = null) }
    fun updateLinkUrl(value: String) = _uiState.update { it.copy(linkUrl = value, error = null, successMessage = null) }
    fun updateTitle(value: String) = _uiState.update { it.copy(title = value, error = null, successMessage = null) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value, error = null, successMessage = null) }
    fun updateSortOrder(value: String) = _uiState.update {
        it.copy(sortOrder = value.filter { ch -> ch == '-' || ch.isDigit() }, error = null, successMessage = null)
    }
    fun updateStartDt(value: String) = _uiState.update { it.copy(startDt = value, error = null, successMessage = null) }
    fun updateEndDt(value: String) = _uiState.update { it.copy(endDt = value, error = null, successMessage = null) }
    fun toggleActive(value: Boolean) = _uiState.update { it.copy(isActive = value, error = null, successMessage = null) }

    fun editBanner(banner: DesktopBannerDto) {
        _uiState.update {
            it.copy(
                editingId = banner.id,
                imageUrl = banner.imageUrl,
                linkUrl = banner.linkUrl.orEmpty(),
                title = banner.title.orEmpty(),
                description = banner.description.orEmpty(),
                sortOrder = banner.sortOrder.toString(),
                isActive = banner.isActive,
                startDt = banner.startDt.orEmpty(),
                endDt = banner.endDt.orEmpty(),
                error = null,
                successMessage = null
            )
        }
    }

    fun cancelEdit() {
        _uiState.update { it.resetForm() }
    }

    fun submitBanner() {
        scope.launch {
            val state = _uiState.value
            if (state.imageUrl.isBlank()) {
                _uiState.update { it.copy(error = "Cần chọn ảnh hoặc nhập URL ảnh banner") }
                return@launch
            }
            val startParsed = state.startDt.ifBlank { null }?.let {
                runCatching { java.time.LocalDateTime.parse(it.substringBefore('.')) }.getOrNull()
            }
            val endParsed = state.endDt.ifBlank { null }?.let {
                runCatching { java.time.LocalDateTime.parse(it.substringBefore('.')) }.getOrNull()
            }
            if (startParsed != null && endParsed != null && !startParsed.isBefore(endParsed)) {
                _uiState.update { it.copy(error = "Thời gian bắt đầu phải trước thời gian kết thúc") }
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true, error = null, successMessage = null) }
            val request = state.toRequest()
            val result = state.editingId
                ?.let { id -> repository.updateBanner(id, request) }
                ?: repository.createBanner(request)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.resetForm().copy(
                            isSubmitting = false,
                            successMessage = if (state.editingId == null) "Đã tạo banner" else "Đã cập nhật banner"
                        )
                    }
                    loadBanners()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, error = error.message ?: "Không thể lưu banner") }
                }
            )
        }
    }

    fun uploadBannerImage(file: File) {
        scope.launch {
            _uiState.update { it.copy(isUploading = true, error = null, successMessage = null) }
            repository.uploadBannerImage(file).fold(
                onSuccess = { url ->
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            imageUrl = url,
                            successMessage = "Đã upload ảnh banner"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isUploading = false, error = error.message ?: "Không thể upload ảnh") }
                }
            )
        }
    }

    fun toggleBannerFromList(banner: DesktopBannerDto) {
        scope.launch {
            val request = DesktopBannerUpsertRequest(
                imageUrl = banner.imageUrl,
                linkUrl = banner.linkUrl,
                title = banner.title,
                description = banner.description,
                sortOrder = banner.sortOrder,
                isActive = !banner.isActive,
                startDt = banner.startDt,
                endDt = banner.endDt
            )
            repository.updateBanner(banner.id, request).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(successMessage = "Đã ${if (request.isActive) "bật" else "tắt"} banner", error = null)
                    }
                    loadBanners()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message ?: "Không thể đổi trạng thái banner") }
                }
            )
        }
    }

    fun deleteBanner(id: String) {
        scope.launch {
            repository.deleteBanner(id).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "Đã xóa banner", error = null) }
                    loadBanners()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message ?: "Không thể xóa banner") }
                }
            )
        }
    }

    private fun BannerUiState.toRequest(): DesktopBannerUpsertRequest {
        return DesktopBannerUpsertRequest(
            imageUrl = imageUrl.trim(),
            linkUrl = linkUrl.trim().ifBlank { null },
            title = title.trim().ifBlank { null },
            description = description.trim().ifBlank { null },
            sortOrder = sortOrder.toIntOrNull() ?: 0,
            isActive = isActive,
            startDt = startDt.trim().ifBlank { null },
            endDt = endDt.trim().ifBlank { null }
        )
    }

    private fun BannerUiState.resetForm(): BannerUiState {
        return copy(
            editingId = null,
            imageUrl = "",
            linkUrl = "",
            title = "",
            description = "",
            sortOrder = "0",
            isActive = true,
            startDt = "",
            endDt = "",
            error = null,
            successMessage = null
        )
    }
}
