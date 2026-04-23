package com.example.nhathuoc.util

/**
 * Centralized validation utilities for form validation across all screens.
 */
object ValidationUtils {

    fun isValidVietnamesePhone(phone: String): Pair<Boolean, String?> {
        val cleanPhone = phone.trim()
        return if (cleanPhone.matches(Regex("^0[0-9]{9}$"))) {
            true to null
        } else {
            false to "Số điện thoại không hợp lệ. Vui lòng nhập 10 chữ số, bắt đầu bằng 0."
        }
    }

    fun isValidEmail(email: String): Pair<Boolean, String?> {
        val cleanEmail = email.trim()
        return if (cleanEmail.matches(Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"))) {
            true to null
        } else {
            false to "Email không hợp lệ."
        }
    }

    fun isValidPassword(password: String): Pair<Boolean, String?> {
        return when {
            password.isEmpty() -> false to "Vui lòng nhập mật khẩu."
            password.length < 6 -> false to "Mật khẩu phải có ít nhất 6 ký tự."
            else -> true to null
        }
    }

    fun isValidFullName(fullName: String): Pair<Boolean, String?> {
        val cleanName = fullName.trim()
        return when {
            cleanName.isEmpty() -> false to "Vui lòng nhập họ và tên."
            cleanName.length < 2 -> false to "Họ và tên phải có ít nhất 2 ký tự."
            cleanName.length > 100 -> false to "Họ và tên không được vượt quá 100 ký tự."
            else -> true to null
        }
    }

    enum class PasswordStrength {
        WEAK,
        MEDIUM,
        STRONG
    }

    data class PasswordStrengthResult(
        val strength: PasswordStrength,
        val score: Int,
        val feedback: String
    )

    fun assessPasswordStrength(password: String): PasswordStrengthResult {
        var score = 0
        val feedback = mutableListOf<String>()

        if (password.length >= 6) score++
        if (password.length >= 8) score++
        if (password.length >= 12) score++

        if (password.any { it.isUpperCase() }) {
            score++
            feedback.add("Có chữ hoa")
        } else {
            feedback.add("Nên thêm chữ hoa")
        }

        if (password.any { it.isLowerCase() }) {
            score++
            feedback.add("Có chữ thường")
        } else {
            feedback.add("Nên thêm chữ thường")
        }

        if (password.any { it.isDigit() }) {
            score++
            feedback.add("Có chữ số")
        } else {
            feedback.add("Nên thêm chữ số")
        }

        if (password.any { !it.isLetterOrDigit() }) {
            score++
            feedback.add("Có ký tự đặc biệt")
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

    fun passwordsMatch(password: String, confirmPassword: String): Pair<Boolean, String?> {
        return if (password == confirmPassword) {
            true to null
        } else {
            false to "Mật khẩu nhập lại không khớp."
        }
    }

    fun isRequired(value: String): Boolean = value.trim().isNotEmpty()

    fun minLength(value: String, min: Int): Boolean = value.length >= min

    fun maxLength(value: String, max: Int): Boolean = value.length <= max

    fun matches(value: String, pattern: String): Boolean = value.matches(Regex(pattern))

    fun isValidPrice(price: Double): Boolean = price > 0 && price <= 100_000_000

    fun isValidQuantity(quantity: Int): Boolean = quantity > 0 && quantity <= 999
}
