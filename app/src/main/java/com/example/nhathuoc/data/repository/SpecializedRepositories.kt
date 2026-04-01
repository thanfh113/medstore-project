package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.data.remote.RetrofitClient
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository for profile operations
 */
class ProfileRepository {
    private val apiService: ApiService by lazy {
        RetrofitClient.createService<ApiService>()
    }

    suspend fun getProfile(): NetworkResult<UserProfileDto> {
        return try {
            val mockResponse = UserProfileDto(
                id = "user-123",
                fullName = "Nguyễn Văn A",
                email = "user@example.com",
                phone = "0987654321",
                avatar = null,
                role = "USER"
            )
            NetworkResult.Success(mockResponse)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun updateProfile(
        fullName: String,
        email: String,
        dateOfBirth: String? = null,
        phone: String? = null
    ): NetworkResult<UserProfileDto> {
        return try {
            val mockResponse = UserProfileDto(
                id = "user-123",
                fullName = fullName,
                email = email,
                phone = phone ?: "0987654321",
                dateOfBirth = dateOfBirth
            )
            NetworkResult.Success(mockResponse)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun uploadAvatar(imagePath: String): NetworkResult<UserProfileDto> {
        return try {
            val mockResponse = UserProfileDto(
                id = "user-123",
                fullName = "Nguyễn Văn A",
                email = "user@example.com",
                phone = "0987654321",
                avatar = "https://res.cloudinary.com/..."
            )
            NetworkResult.Success(mockResponse)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getAddresses(): NetworkResult<List<UserAddressDto>> {
        return try {
            val mockAddresses = listOf(
                UserAddressDto(
                    id = "addr-1",
                    fullName = "Nguyễn Văn A",
                    phone = "0987654321",
                    address = "123 Đường Lê Lợi, Quận 1",
                    district = "Quận 1",
                    city = "TP.HCM",
                    type = AddressType.HOME,
                    isDefault = true
                )
            )
            NetworkResult.Success(mockAddresses)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun addAddress(
        fullName: String,
        phone: String,
        address: String,
        district: String,
        city: String,
        type: AddressType
    ): NetworkResult<UserAddressDto> {
        return try {
            val newAddress = UserAddressDto(
                id = "addr-${System.currentTimeMillis()}",
                fullName = fullName,
                phone = phone,
                address = address,
                district = district,
                city = city,
                type = type,
                isDefault = false
            )
            NetworkResult.Success(newAddress)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun deleteAddress(addressId: String): NetworkResult<String> {
        return try {
            NetworkResult.Success("Đã xóa địa chỉ")
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun setDefaultAddress(addressId: String): NetworkResult<UserAddressDto> {
        return try {
            val mockAddress = UserAddressDto(
                id = addressId,
                fullName = "User",
                phone = "123456",
                address = "Address",
                district = "",
                city = "",
                type = AddressType.HOME,
                isDefault = true
            )
            NetworkResult.Success(mockAddress)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}

/**
 * Repository for reward operations
 */
class RewardRepository {
    private val apiService: ApiService by lazy {
        RetrofitClient.createService<ApiService>()
    }

    suspend fun getRewards(): NetworkResult<RewardDto> {
        return try {
            val mockResponse = RewardDto(
                id = "reward-123",
                userId = "user-123",
                totalPoints = 500,
                tier = "SILVER",
                nextTierPoints = 1000
            )
            NetworkResult.Success(mockResponse)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getRewardHistory(): NetworkResult<List<RewardHistoryDto>> {
        return try {
            val mockHistory = listOf(
                RewardHistoryDto("h1", "EARNING", 100, "Mua hàng đơn #123"),
                RewardHistoryDto("h2", "EARNING", 50, "Mua hàng đơn #122"),
                RewardHistoryDto("h3", "REDEMPTION", 25, "Đổi giảm giá")
            )
            NetworkResult.Success(mockHistory)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun redeemPoints(points: Int): NetworkResult<String> {
        return try {
            NetworkResult.Success("Đã đổi $points điểm thành công")
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}

/**
 * Repository for notifications
 */
class NotificationRepository {
    private val apiService: ApiService by lazy {
        RetrofitClient.createService<ApiService>()
    }

    suspend fun getNotifications(): NetworkResult<List<NotificationDto>> {
        return try {
            val mockNotifications = listOf(
                NotificationDto("n1", "user-123", "Đơn hàng đã giao", "Đơn hàng #123 đã giao thành công", "ORDER"),
                NotificationDto("n2", "user-123", "Khuyến mãi mới", "Giảm 20% cho các sản phẩm chọn lọc", "PROMOTION"),
                NotificationDto("n3", "user-123", "Thông báo hệ thống", "Cập nhật ứng dụng phiên bản mới", "SYSTEM", isRead = true)
            )
            NetworkResult.Success(mockNotifications)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun markAsRead(notificationId: String): NetworkResult<String> {
        return try {
            NetworkResult.Success("Đã đánh dấu là đã đọc")
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}

/**
 * Repository for consultations and doctor management
 */
class ConsultRepository {
    private val apiService: ApiService by lazy {
        RetrofitClient.createService<ApiService>()
    }

    suspend fun getDoctors(
        search: String? = null,
        specialization: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): NetworkResult<List<DoctorDto>> {
        return try {
            val mockDoctors = listOf(
                DoctorDto(
                    id = "doc-1",
                    name = "Dr. Trần Minh Tuấn",
                    specialization = "Tim mạch",
                    description = "Bác sĩ có 15 năm kinh nghiệm",
                    rating = 4.8,
                    reviewCount = 120
                ),
                DoctorDto(
                    id = "doc-2",
                    name = "Dr. Nguyễn Thị Hồng",
                    specialization = "Ngoại da",
                    description = "Bác sĩ chuyên khoa ngoại da",
                    rating = 4.9,
                    reviewCount = 200
                )
            )
            NetworkResult.Success(mockDoctors)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getDoctorDetail(doctorId: String): NetworkResult<DoctorDto> {
        return try {
            val mockDoctor = DoctorDto(
                id = doctorId,
                name = "Dr. Trần Minh Tuấn",
                specialization = "Tim mạch",
                description = "Bác sĩ có 15 năm kinh nghiệm",
                experience = "15 năm",
                qualification = "Cử nhân Y khoa, Thạc sĩ Tim mạch",
                workingHours = "09:00 - 17:00, Thứ 2-6"
            )
            NetworkResult.Success(mockDoctor)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getConsultations(): NetworkResult<List<ConsultationDto>> {
        return try {
            val mockConsultations = listOf(
                ConsultationDto(
                    id = "cons-1",
                    consultationCode = "CONS-123456",
                    userId = "user-123",
                    doctorId = "doc-1",
                    doctorName = "Dr. Trần Minh Tuấn",
                    scheduledAt = "2026-04-15T14:30:00",
                    status = "SCHEDULED",
                    sessionType = "VIDEO",
                    reason = "Check heart health",
                    paymentStatus = "PAID"
                )
            )
            NetworkResult.Success(mockConsultations)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun bookConsultation(
        doctorId: String,
        scheduledAt: String,
        sessionType: String,
        reason: String
    ): NetworkResult<ConsultationDto> {
        return try {
            val mockConsult = ConsultationDto(
                id = "cons-${System.currentTimeMillis()}",
                consultationCode = "CONS-${System.currentTimeMillis().toString().takeLast(6)}",
                userId = "user-123",
                doctorId = doctorId,
                doctorName = "Dr. Trần Minh Tuấn",
                scheduledAt = scheduledAt,
                status = "SCHEDULED",
                sessionType = sessionType,
                reason = reason,
                fee = 500000.0,
                paymentStatus = "UNPAID"
            )
            NetworkResult.Success(mockConsult)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getConsultationMessages(consultationId: String): NetworkResult<List<ConsultationMessageDto>> {
        return try {
            val mockMessages = listOf(
                ConsultationMessageDto(
                    id = "msg-1",
                    consultationId = consultationId,
                    senderId = "user-123",
                    senderName = "You",
                    senderType = "USER",
                    message = "Xin chào bác sĩ"
                ),
                ConsultationMessageDto(
                    id = "msg-2",
                    consultationId = consultationId,
                    senderId = "doc-1",
                    senderName = "Dr. Trần Minh Tuấn",
                    senderType = "DOCTOR",
                    message = "Xin chào, tư vấn bắt đầu"
                )
            )
            NetworkResult.Success(mockMessages)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun sendMessage(consultationId: String, message: String): NetworkResult<ConsultationMessageDto> {
        return try {
            val mockMessage = ConsultationMessageDto(
                id = "msg-${System.currentTimeMillis()}",
                consultationId = consultationId,
                senderId = "user-123",
                senderName = "You",
                senderType = "USER",
                message = message
            )
            NetworkResult.Success(mockMessage)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun rateConsultation(consultationId: String, rating: Double, review: String?): NetworkResult<String> {
        return try {
            NetworkResult.Success("Cảm ơn đánh giá của bạn!")
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}
