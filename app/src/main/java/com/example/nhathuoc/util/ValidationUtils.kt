package com.example.nhathuoc.util

/**
 * Centralized validation utilities for form validation across all screens
 */
object ValidationUtils {

    // Vietnamese phone validation: 0[3,5,7,8,9] + 8 digits
    fun isValidVietnamesePhone(phone: String): Pair<Boolean, String?> {
        val cleanPhone = phone.trim()
        return if (cleanPhone.matches(Regex("^(0[3,5,7,8,9])+([0-9]{8})$"))) {
            Pair(true, null)
        } else {
            Pair(false, "Số điện thoại không hợp lệ (định dạng: 0xxxxxxxxx)")
        }
    }

    // Email validation
    fun isValidEmail(email: String): Pair<Boolean, String?> {
        val cleanEmail = email.trim()
        return if (cleanEmail.matches(Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"))) {
            Pair(true, null)
        } else {
            Pair(false, "Email không hợp lệ")
        }
    }

    // Password length validation
    fun isValidPassword(password: String): Pair<Boolean, String?> {
        return when {
            password.isEmpty() -> Pair(false, "Vui lòng nhập mật khẩu")
            password.length < 6 -> Pair(false, "Mật khẩu phải có ít nhất 6 ký tự")
            else -> Pair(true, null)
        }
    }

    // Full name validation
    fun isValidFullName(fullName: String): Pair<Boolean, String?> {
        val cleanName = fullName.trim()
        return when {
            cleanName.isEmpty() -> Pair(false, "Vui lòng nhập họ và tên")
            cleanName.length < 2 -> Pair(false, "Họ và tên phải có ít nhất 2 ký tự")
            cleanName.length > 100 -> Pair(false, "Họ và tên không được vượt quá 100 ký tự")
            else -> Pair(true, null)
        }
    }

    // Password strength assessment
    enum class PasswordStrength {
        WEAK,      // < 6 chars
        MEDIUM,    // 6-11 chars
        STRONG     // 12+ chars
    }

    data class PasswordStrengthResult(
        val strength: PasswordStrength,
        val score: Int,  // 0-3
        val feedback: String
    )

    fun assessPasswordStrength(password: String): PasswordStrengthResult {
        var score = 0
        val feedback = mutableListOf<String>()

        // Length check
        if (password.length >= 6) score++
        if (password.length >= 8) score++
        if (password.length >= 12) score++

        // Complexity checks
        if (password.any { it.isUpperCase() }) {
            score++
            feedback.add("✓ Chứa chữ hoa")
        } else {
            feedback.add("• Thêm chữ hoa")
        }

        if (password.any { it.isLowerCase() }) {
            score++
            feedback.add("✓ Chứa chữ thường")
        } else {
            feedback.add("• Thêm chữ thường")
        }

        if (password.any { it.isDigit() }) {
            score++
            feedback.add("✓ Chứa số")
        } else {
            feedback.add("• Thêm số")
        }

        if (password.any { !it.isLetterOrDigit() }) {
            score++
            feedback.add("✓ Chứa ký tự đặc biệt")
        }

        val strength = when {
            password.length < 6 -> PasswordStrength.WEAK
            password.length < 12 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }

        return PasswordStrengthResult(
            strength = strength,
            score = minOf(score, 3),
            feedback = feedback.take(3).joinToString("\n")
        )
    }

    // Confirm password validation
    fun passwordsMatch(password: String, confirmPassword: String): Pair<Boolean, String?> {
        return if (password == confirmPassword) {
            Pair(true, null)
        } else {
            Pair(false, "Mật khẩu không khớp")
        }
    }

    // Generic validators
    fun isRequired(value: String): Boolean = value.trim().isNotEmpty()

    fun minLength(value: String, min: Int): Boolean = value.length >= min

    fun maxLength(value: String, max: Int): Boolean = value.length <= max

    fun matches(value: String, pattern: String): Boolean = value.matches(Regex(pattern))

    // Price validation
    fun isValidPrice(price: Double): Boolean = price > 0 && price <= 100_000_000

    fun isValidQuantity(quantity: Int): Boolean = quantity > 0 && quantity <= 999
}
