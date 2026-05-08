package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.AddressProvince
import com.example.nhathuoc.data.model.AddressWard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProvinceRepository @Inject constructor(
    private val json: Json
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun getProvinces(): Result<List<AddressProvince>> = withContext(Dispatchers.IO) {
        runCatching {
            requestJson("https://provinces.open-api.vn/api/v2/p/")
                ?.let(::parseProvinceList)
                ?.takeIf { it.isNotEmpty() }
                ?: emptyList()
        }
    }

    suspend fun getWards(provinceCode: String): Result<List<AddressWard>> = withContext(Dispatchers.IO) {
        runCatching {
            requestJson("https://provinces.open-api.vn/api/v2/p/$provinceCode?depth=2")
                ?.let(::parseWardList)
                ?.takeIf { it.isNotEmpty() }
                ?: emptyList()
        }
    }

    private fun requestJson(url: String): JsonElement? {
        val request = Request.Builder().url(url).get().build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            response.body?.string()?.takeIf { it.isNotBlank() }?.let(json::parseToJsonElement)
        }
    }

    private fun parseProvinceList(element: JsonElement): List<AddressProvince> {
        val array = element.arrayPayload() ?: return emptyList()
        return array.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            val code = obj.flexString("code") ?: obj.flexString("province_code") ?: return@mapNotNull null
            val name = obj.flexString("name") ?: obj.flexString("full_name") ?: return@mapNotNull null
            AddressProvince(code = code, name = name)
        }.distinctBy { it.code }
    }

    private fun parseWardList(element: JsonElement): List<AddressWard> {
        val root = element as? JsonObject ?: return emptyList()
        val directWards = root.array("wards") ?: root.array("communes")
        if (directWards != null) {
            return directWards.mapNotNull { item -> parseWard(item, districtName = null) }
                .distinctBy { it.code }
        }

        val districts = root.array("districts") ?: return emptyList()
        return districts.flatMap { district ->
            val districtObj = district as? JsonObject ?: return@flatMap emptyList()
            val districtName = districtObj.flexString("name") ?: districtObj.flexString("full_name")
            val wards = districtObj.array("wards") ?: districtObj.array("communes") ?: return@flatMap emptyList()
            wards.mapNotNull { ward -> parseWard(ward, districtName) }
        }.distinctBy { it.code }
    }

    private fun parseWard(element: JsonElement, districtName: String?): AddressWard? {
        val obj = element as? JsonObject ?: return null
        val code = obj.flexString("code") ?: obj.flexString("ward_code") ?: return null
        val name = obj.flexString("name") ?: obj.flexString("full_name") ?: return null
        return AddressWard(code = code, name = name, districtName = districtName)
    }

    private fun JsonElement.arrayPayload(): JsonArray? {
        return when (this) {
            is JsonArray -> this
            is JsonObject -> this.array("data") ?: this.array("results") ?: this.array("provinces")
            else -> null
        }
    }

    private fun JsonObject.array(key: String): JsonArray? = this[key]?.let { it as? JsonArray }

    private fun JsonObject.flexString(key: String): String? {
        val value = this[key] ?: return null
        return when (value) {
            is JsonPrimitive -> value.contentOrNull
            else -> null
        }?.trim()?.takeIf { it.isNotBlank() }
    }
}
