package com.example.nhathuoc.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

/**
 * Utility functions for price formatting and conversion
 * Handles the transition from String-based prices to numeric prices
 */
object PriceUtils {

    private val vietnameseFormatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))

    /**
     * Format a numeric price to Vietnamese currency format
     * @param price Numeric price value
     * @return Formatted string like "123.000đ"
     */
    fun formatPrice(price: Double): String {
        return "${vietnameseFormatter.format(price.toLong())}đ"
    }

    /**
     * Format a numeric price to Vietnamese currency format
     * @param price Numeric price value
     * @return Formatted string like "123.000đ"
     */
    fun formatPrice(price: Long): String {
        return "${vietnameseFormatter.format(price)}đ"
    }

    /**
     * Parse Vietnamese formatted price string to Double
     * @param priceString String like "123.000đ" or "123,000đ"
     * @return Numeric price value
     */
    fun parsePrice(priceString: String): Double {
        return try {
            // Remove đ and any whitespace
            val cleanPrice = priceString.replace("đ", "").trim()
            // Replace dots and commas used as thousand separators
            val numericString = cleanPrice.replace(".", "").replace(",", "")
            numericString.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * Safe conversion from String price to Double with fallback
     * @param priceString Current string-based price
     * @return Parsed price or 0.0 if parsing fails
     */
    fun safeParsePrice(priceString: String?): Double {
        if (priceString.isNullOrBlank()) return 0.0
        return parsePrice(priceString)
    }

    /**
     * Extension function to format Double as Vietnamese price
     */
    fun Double.toVietnamPrice(): String = formatPrice(this)

    /**
     * Extension function to format Long as Vietnamese price
     */
    fun Long.toVietnamPrice(): String = formatPrice(this)

    /**
     * Extension function to parse Vietnamese price string
     */
    fun String.parsePriceToDouble(): Double = parsePrice(this)

    /**
     * Validate if a string is a valid price format
     * @param priceString String to validate
     * @return true if valid price format
     */
    fun isValidPriceFormat(priceString: String): Boolean {
        return try {
            parsePrice(priceString)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Calculate total from unit price and quantity
     * @param unitPrice Price per unit
     * @param quantity Number of items
     * @return Total price
     */
    fun calculateTotal(unitPrice: Double, quantity: Int): Double {
        return unitPrice * quantity
    }

    /**
     * Calculate total from string price and quantity with fallback
     * @param unitPriceString String-based unit price
     * @param quantity Number of items
     * @return Total price
     */
    fun calculateTotalFromString(unitPriceString: String, quantity: Int): Double {
        val unitPrice = safeParsePrice(unitPriceString)
        return calculateTotal(unitPrice, quantity)
    }

    /**
     * Apply discount to price
     * @param originalPrice Original price
     * @param discountPercent Discount percentage (0-100)
     * @return Discounted price
     */
    fun applyDiscount(originalPrice: Double, discountPercent: Int): Double {
        return if (discountPercent in 0..100) {
            originalPrice * (1.0 - discountPercent / 100.0)
        } else {
            originalPrice
        }
    }

    /**
     * Calculate discount amount
     * @param originalPrice Original price
     * @param discountedPrice Discounted price
     * @return Discount amount
     */
    fun calculateDiscountAmount(originalPrice: Double, discountedPrice: Double): Double {
        return originalPrice - discountedPrice
    }

    // Constants for common price scenarios
    const val FREE_PRICE = 0.0
    const val MIN_PRICE = 1000.0 // Minimum price in VND
    const val MAX_PRICE = 100_000_000.0 // Maximum reasonable price in VND

    /**
     * Validate if price is within reasonable limits
     * @param price Price to validate
     * @return true if price is valid
     */
    fun isValidPrice(price: Double): Boolean {
        return price >= FREE_PRICE && price <= MAX_PRICE
    }
}