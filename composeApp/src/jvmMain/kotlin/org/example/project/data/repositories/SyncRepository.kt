package org.example.project.data.repositories

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.example.project.data.local.InventorySyncAuditRecord
import org.example.project.data.local.OfflineStore
import org.example.project.data.local.OutboxChange
import org.example.project.data.local.PosCartDraft
import org.example.project.data.models.Product
import org.example.project.data.network.PushSyncChange
import org.example.project.data.network.PushSyncRequest
import org.example.project.data.network.SyncApiService
import java.time.Instant
import java.util.UUID

class SyncRepository(
    private val offlineStore: OfflineStore,
    private val syncApiService: SyncApiService
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val deviceId: String = offlineStore.getOrCreateDeviceId()

    fun setAuthToken(token: String?) {
        syncApiService.setAuthToken(token)
    }

    fun setAuthRetryHandler(handler: AuthRetryHandler?) {
        syncApiService.setAuthRetryHandler(handler)
    }

    fun cacheProducts(products: List<Product>) {
        offlineStore.saveProducts(products)
    }

    fun loadCachedProducts(): List<Product> = offlineStore.loadProducts()

    fun savePosDraft(draft: PosCartDraft) {
        offlineStore.savePosDraft(draft)
    }

    fun loadPosDraft(): PosCartDraft? = offlineStore.loadPosDraft()

    fun clearPosDraft() {
        offlineStore.clearPosDraft()
    }

    fun pendingOutboxCount(): Int = offlineStore.loadOutbox().size

    fun getInventorySyncAudits(limit: Int = 300): List<InventorySyncAuditRecord> {
        return offlineStore.loadInventoryAuditsDesc(limit)
    }

    fun cacheOrdersSummary(orders: List<InternalOrderSummaryDto>) {
        offlineStore.saveOrdersSummary(orders)
    }

    fun loadCachedOrders(status: String? = null): List<InternalOrderSummaryDto> {
        return offlineStore.loadOrdersSummary().filter { status == null || it.status.equals(status, ignoreCase = true) }
    }

    fun cacheOrderDetail(detail: InternalOrderDetailDto) {
        offlineStore.saveOrderDetail(detail)
    }

    fun loadCachedOrderDetail(orderId: String): InternalOrderDetailDto? {
        return offlineStore.loadOrderDetail(orderId)
    }

    fun markCachedOrderStatus(orderId: String, status: String) {
        val updated = offlineStore.loadOrdersSummary().map {
            if (it.id == orderId) it.copy(status = status.uppercase()) else it
        }
        offlineStore.saveOrdersSummary(updated)
    }

    fun queueOrderStatusUpdate(orderId: String, status: String): Result<Unit> {
        return runCatching {
            val payload = buildJsonObject {
                put("orderId", orderId)
                put("status", status.uppercase())
            }
            val mutationId = UUID.randomUUID().toString()
            offlineStore.appendOutbox(
                OutboxChange(
                    id = UUID.randomUUID().toString(),
                    entityType = "ORDER_STATUS",
                    entityId = orderId,
                    operation = "UPDATE",
                    payloadJson = payload.toString(),
                    clientMutationId = mutationId,
                    createdAtEpochMs = System.currentTimeMillis()
                )
            )
            markCachedOrderStatus(orderId, status)
        }
    }

    fun queueProductUpsert(product: Product, operation: String): Result<Unit> {
        return runCatching {
            val payload = json.encodeToString(Product.serializer(), product)
            val mutationId = UUID.randomUUID().toString()
            offlineStore.appendOutbox(
                OutboxChange(
                    id = UUID.randomUUID().toString(),
                    entityType = "PRODUCT",
                    entityId = product.id,
                    operation = operation,
                    payloadJson = payload,
                    clientMutationId = mutationId,
                    createdAtEpochMs = System.currentTimeMillis()
                )
            )
            val existing = offlineStore.loadProducts().toMutableList()
            val index = existing.indexOfFirst { it.id == product.id }
            if (index >= 0) {
                existing[index] = product
            } else {
                existing += product
            }
            offlineStore.saveProducts(existing)
        }
    }

    fun queueProductDelete(productId: String): Result<Unit> {
        return runCatching {
            val mutationId = UUID.randomUUID().toString()
            offlineStore.appendOutbox(
                OutboxChange(
                    id = UUID.randomUUID().toString(),
                    entityType = "PRODUCT",
                    entityId = productId,
                    operation = "DELETE",
                    payloadJson = null,
                    clientMutationId = mutationId,
                    createdAtEpochMs = System.currentTimeMillis()
                )
            )
            val existing = offlineStore.loadProducts().filterNot { it.id == productId }
            offlineStore.saveProducts(existing)
        }
    }

    fun queuePosOrder(
        items: List<Pair<String, Int>>,
        paymentMethod: String,
        couponCode: String?
    ): Result<Unit> {
        return runCatching {
            val payloadElement = buildJsonObject {
                put("paymentMethod", paymentMethod)
                put("couponCode", couponCode)
                putJsonArray("items") {
                    items.forEach { (productId, quantity) ->
                        add(buildJsonObject {
                            put("productId", productId)
                            put("quantity", quantity)
                        })
                    }
                }
            }
            val mutationId = UUID.randomUUID().toString()
            offlineStore.appendOutbox(
                OutboxChange(
                    id = UUID.randomUUID().toString(),
                    entityType = "POS_ORDER",
                    entityId = UUID.randomUUID().toString(),
                    operation = "CREATE",
                    payloadJson = payloadElement.toString(),
                    clientMutationId = mutationId,
                    createdAtEpochMs = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun syncIncremental(): Result<Unit> {
        val pushResult = pushOutbox()
        if (pushResult.isFailure) return pushResult
        return pullChanges()
    }

    private suspend fun pushOutbox(): Result<Unit> {
        val pending = offlineStore.loadOutbox()
        if (pending.isEmpty()) return Result.success(Unit)

        val request = PushSyncRequest(
            deviceId = deviceId,
            changes = pending.map { change ->
                PushSyncChange(
                    entityType = change.entityType,
                    entityId = change.entityId,
                    operation = change.operation,
                    payload = change.payloadJson?.let { json.decodeFromString(JsonElement.serializer(), it) },
                    clientMutationId = change.clientMutationId
                )
            }
        )

        return syncApiService.pushChanges(request).map { ack ->
            val acceptedMutationIds = ack.accepted
                .mapNotNull { it.clientMutationId }
                .toSet()
            offlineStore.removeOutboxByMutationIds(acceptedMutationIds)
            val checkpoint = offlineStore.loadCheckpoint()
            val next = checkpoint.copy(lastServerVersion = maxOf(checkpoint.lastServerVersion, ack.latestServerVersion))
            offlineStore.saveCheckpoint(next)
        }
    }

    private suspend fun pullChanges(): Result<Unit> {
        val checkpoint = offlineStore.loadCheckpoint()
        return syncApiService.pullChanges(
            deviceId = deviceId,
            sinceVersion = checkpoint.lastServerVersion
        ).map { response ->
            applyPullChanges(response.data)
            val next = checkpoint.copy(lastServerVersion = maxOf(checkpoint.lastServerVersion, response.latestServerVersion))
            offlineStore.saveCheckpoint(next)
        }
    }

    private fun applyPullChanges(changes: List<org.example.project.data.network.PullSyncChangeItem>) {
        if (changes.isEmpty()) return
        val cachedProducts = offlineStore.loadProducts().toMutableList()
        var productsChanged = false

        changes.forEach { item ->
            when (item.entityType.uppercase()) {
                "PRODUCT" -> {
                    when (item.operation.uppercase()) {
                        "DELETE" -> {
                            val removed = cachedProducts.removeAll { it.id == item.entityId }
                            productsChanged = productsChanged || removed
                        }
                        "CREATE", "UPDATE" -> {
                            val payload = item.payloadJson ?: return@forEach
                            val product = runCatching {
                                json.decodeFromString(Product.serializer(), payload)
                            }.getOrNull() ?: return@forEach

                            val index = cachedProducts.indexOfFirst { it.id == product.id }
                            if (index >= 0) {
                                cachedProducts[index] = product
                            } else {
                                cachedProducts += product
                            }
                            productsChanged = true
                        }
                    }
                }
                "ORDER" -> applyOrderPullChange(item)
                "ORDER_STATUS" -> applyOrderStatusPullChange(item)
                "INVENTORY" -> {
                    // Conflict policy: server stock is authoritative. Any local divergence is overwritten and audited.
                    val inventoryChanged = applyInventoryPullChange(item, cachedProducts)
                    productsChanged = productsChanged || inventoryChanged
                }
            }
        }

        if (productsChanged) {
            offlineStore.saveProducts(cachedProducts)
        }
    }

    private fun applyOrderPullChange(item: org.example.project.data.network.PullSyncChangeItem) {
        val payload = item.payloadJson
        val parsed = payload
            ?.let(::parseOrderSummaryFromFlexiblePayload)
            ?: InternalOrderSummaryDto(
                id = item.entityId,
                orderCode = item.entityId,
                customerId = "WALK_IN",
                customerName = "Walk-in customer",
                customerPhone = null,
                orderChannel = "POS",
                status = "PENDING",
                paymentMethod = null,
                paymentStatus = "UNPAID",
                total = 0.0,
                note = null,
                createdAt = Instant.now().toString(),
                updatedAt = Instant.now().toString()
            )

        val current = offlineStore.loadOrdersSummary().toMutableList()
        when (item.operation.uppercase()) {
            "DELETE" -> current.removeAll { it.id == parsed.id || it.id == item.entityId }
            "CREATE", "UPDATE" -> {
                val index = current.indexOfFirst { it.id == parsed.id }
                if (index >= 0) current[index] = parsed else current += parsed
            }
        }
        offlineStore.saveOrdersSummary(current)
    }

    private fun parseOrderSummaryFromFlexiblePayload(payloadJson: String): InternalOrderSummaryDto? {
        val root = runCatching { json.parseToJsonElement(payloadJson).jsonObject }.getOrNull() ?: return null
        val body = unwrapOrderObject(root)

        val id = body.stringValue("id", "orderId") ?: return null
        val orderCode = body.stringValue("orderCode", "code", "order_code") ?: id
        val customerId = body.stringValue("customerId", "userId", "user_id")
            ?: if (body.boolValue("isWalkIn") == true) "WALK_IN" else "UNKNOWN"
        val customerName = body.stringValue("customerName", "customer_name", "buyerName", "fullName")
        val customerPhone = body.stringValue("customerPhone", "phone", "customer_phone")
        val status = normalizeOrderStatus(body.stringValue("status", "orderStatus", "order_status") ?: "PENDING")
        val paymentMethod = body.stringValue("paymentMethod", "payment_method", "method")
        val paymentStatus = normalizePaymentStatus(body.stringValue("paymentStatus", "payment_status") ?: "UNPAID")
        val total = body.doubleValue("total", "grandTotal", "finalAmount", "amount")
        val note = body.stringValue("note", "notes")
        val createdAt = body.stringValue("createdAt", "created_at") ?: Instant.now().toString()
        val updatedAt = body.stringValue("updatedAt", "updated_at") ?: createdAt

        return InternalOrderSummaryDto(
            id = id,
            orderCode = orderCode,
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            orderChannel = body.stringValue("orderChannel", "order_channel") ?: "POS",
            status = status,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            total = total,
            note = note,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun unwrapOrderObject(root: JsonObject): JsonObject {
        val firstLayer = root["data"]?.asObjectOrNull() ?: root
        return firstLayer["order"]?.asObjectOrNull()
            ?: firstLayer["payload"]?.asObjectOrNull()
            ?: firstLayer
    }

    private fun normalizeOrderStatus(raw: String): String {
        return when (raw.uppercase()) {
            "CONFIRMED", "PREPARING" -> "PROCESSING"
            "IN_TRANSIT" -> "SHIPPING"
            else -> raw.uppercase()
        }
    }

    private fun normalizePaymentStatus(raw: String): String {
        return when (raw.uppercase()) {
            "PAID", "COMPLETED", "SUCCESS" -> "COMPLETED"
            "PENDING" -> "UNPAID"
            else -> raw.uppercase()
        }
    }

    private fun JsonObject.stringValue(vararg keys: String): String? {
        return keys.asSequence()
            .mapNotNull { key -> this[key]?.jsonPrimitive?.contentOrNull }
            .firstOrNull { it.isNotBlank() }
    }

    private fun JsonObject.doubleValue(vararg keys: String): Double? {
        return keys.asSequence()
            .mapNotNull { key -> this[key]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() }
            .firstOrNull()
    }

    private fun JsonObject.boolValue(vararg keys: String): Boolean? {
        return keys.asSequence()
            .mapNotNull { key -> this[key]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() }
            .firstOrNull()
    }

    private fun JsonElement.asObjectOrNull(): JsonObject? = this as? JsonObject
    private val JsonPrimitive.contentOrNull: String?
        get() = runCatching { content }.getOrNull()

    private fun applyOrderStatusPullChange(item: org.example.project.data.network.PullSyncChangeItem) {
        val payload = item.payloadJson ?: return
        val status = runCatching {
            json.parseToJsonElement(payload).jsonObject["status"]?.jsonPrimitive?.content
        }.getOrNull()?.uppercase() ?: return
        val orderId = runCatching {
            json.parseToJsonElement(payload).jsonObject["orderId"]?.jsonPrimitive?.content
        }.getOrNull() ?: item.entityId

        markCachedOrderStatus(orderId, status)
    }

    private fun applyInventoryPullChange(
        item: org.example.project.data.network.PullSyncChangeItem,
        cachedProducts: MutableList<Product>
    ): Boolean {
        val payload = item.payloadJson ?: return false
        val jsonObject = runCatching { json.parseToJsonElement(payload).jsonObject }.getOrNull() ?: return false
        val productId = jsonObject["productId"]?.jsonPrimitive?.content ?: item.entityId
        val serverStock = listOf("stockQuantity", "stock", "quantityOnHand")
            .asSequence()
            .mapNotNull { key -> jsonObject[key]?.jsonPrimitive?.content?.toIntOrNull() }
            .firstOrNull()
            ?: return false

        val index = cachedProducts.indexOfFirst { it.id == productId }
        if (index < 0) return false

        val current = cachedProducts[index]
        if (current.stockQuantity != serverStock) {
            offlineStore.appendInventoryAudit(
                InventorySyncAuditRecord(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    beforeStock = current.stockQuantity,
                    serverStock = serverStock,
                    reason = "SYNC_PULL_${item.operation.uppercase()}",
                    sourceDeviceId = item.sourceDeviceId,
                    createdAtEpochMs = System.currentTimeMillis(),
                    resolvedBySyncAtEpochMs = System.currentTimeMillis()
                )
            )
        }

        cachedProducts[index] = current.copy(stockQuantity = serverStock)
        return true
    }
}

