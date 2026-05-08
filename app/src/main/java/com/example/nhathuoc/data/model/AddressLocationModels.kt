package com.example.nhathuoc.data.model

data class AddressProvince(
    val code: String,
    val name: String
)

data class AddressWard(
    val code: String,
    val name: String,
    val districtName: String? = null
)
