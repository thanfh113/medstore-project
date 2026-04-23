package org.example.project.data.local

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.example.project.data.models.Product
import org.example.project.data.repositories.InternalOrderDetailDto
import org.example.project.data.repositories.InternalOrderSummaryDto
import java.io.File
import java.util.UUID

@Serializable
data class OutboxChange(
    val id: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payloadJson: String? = null,
    val clientMutationId: String,
    val createdAtEpochMs: Long
)

@Serializable
data class SyncCheckpoint(
    val lastServerVersion: Long = 0L
)

@Serializable
data class PosCartDraftItem(
    val productId: String,
    val quantity: Int
)

@Serializable
data class PosCartDraft(
    val items: List<PosCartDraftItem> = emptyList(),
    val couponCode: String = "",
    val appliedDiscount: Double = 0.0,
    val paymentMethod: String = "CASH"
)

@Serializable
data class InventorySyncAuditRecord(
    val id: String,
    val productId: String,
    val beforeStock: Int,
    val serverStock: Int,
    val policy: String = "SERVER_AUTHORITATIVE",
    val reason: String,
    val sourceDeviceId: String? = null,
    val createdAtEpochMs: Long,
    val resolvedBySyncAtEpochMs: Long? = null
)

class OfflineStore {
    private val json = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
    }
    private val lock = Any()
    private val rootDir: File = File(System.getProperty("user.home"), ".nhathuoc-desktop/offline").apply {
        mkdirs()
    }

    private val productsFile = File(rootDir, "products_cache.json")
    private val outboxFile = File(rootDir, "outbox.json")
    private val checkpointFile = File(rootDir, "checkpoint.json")
    private val posDraftFile = File(rootDir, "pos_cart_draft.json")
    private val ordersSummaryFile = File(rootDir, "orders_summary_cache.json")
    private val orderDetailsFile = File(rootDir, "order_details_cache.json")
    private val inventoryAuditFile = File(rootDir, "inventory_sync_audit.json")
    private val deviceIdFile = File(rootDir, "device_id.txt")

    fun getOrCreateDeviceId(): String = synchronized(lock) {
        if (deviceIdFile.exists()) {
            val existing = deviceIdFile.readText().trim()
            if (existing.isNotEmpty()) return existing
        }
        val generated = "desktop-${UUID.randomUUID()}"
        deviceIdFile.writeText(generated)
        generated
    }

    fun saveProducts(products: List<Product>) = synchronized(lock) {
        write(productsFile, ListSerializer(Product.serializer()), products)
    }

    fun loadProducts(): List<Product> = synchronized(lock) {
        read(productsFile, ListSerializer(Product.serializer())) ?: emptyList()
    }

    fun loadOutbox(): List<OutboxChange> = synchronized(lock) {
        read(outboxFile, ListSerializer(OutboxChange.serializer())) ?: emptyList()
    }

    fun appendOutbox(change: OutboxChange) = synchronized(lock) {
        val current = loadOutbox().toMutableList()
        current += change
        write(outboxFile, ListSerializer(OutboxChange.serializer()), current)
    }

    fun removeOutboxByMutationIds(clientMutationIds: Set<String>) {
        synchronized(lock) {
            if (clientMutationIds.isEmpty()) return@synchronized
            val remain = loadOutbox().filterNot { it.clientMutationId in clientMutationIds }
            write(outboxFile, ListSerializer(OutboxChange.serializer()), remain)
        }
    }

    fun loadCheckpoint(): SyncCheckpoint = synchronized(lock) {
        read(checkpointFile, SyncCheckpoint.serializer()) ?: SyncCheckpoint()
    }

    fun saveCheckpoint(checkpoint: SyncCheckpoint) = synchronized(lock) {
        write(checkpointFile, SyncCheckpoint.serializer(), checkpoint)
    }

    fun loadPosDraft(): PosCartDraft? = synchronized(lock) {
        read(posDraftFile, PosCartDraft.serializer())
    }

    fun savePosDraft(draft: PosCartDraft) = synchronized(lock) {
        write(posDraftFile, PosCartDraft.serializer(), draft)
    }

    fun clearPosDraft() = synchronized(lock) {
        if (posDraftFile.exists()) {
            posDraftFile.delete()
        }
    }

    fun saveOrdersSummary(orders: List<InternalOrderSummaryDto>) = synchronized(lock) {
        write(ordersSummaryFile, ListSerializer(InternalOrderSummaryDto.serializer()), orders)
    }

    fun loadOrdersSummary(): List<InternalOrderSummaryDto> = synchronized(lock) {
        read(ordersSummaryFile, ListSerializer(InternalOrderSummaryDto.serializer())) ?: emptyList()
    }

    fun saveOrderDetail(detail: InternalOrderDetailDto) = synchronized(lock) {
        val current = loadOrderDetails().toMutableList()
        val index = current.indexOfFirst { it.id == detail.id }
        if (index >= 0) {
            current[index] = detail
        } else {
            current += detail
        }
        write(orderDetailsFile, ListSerializer(InternalOrderDetailDto.serializer()), current)
    }

    fun loadOrderDetail(orderId: String): InternalOrderDetailDto? = synchronized(lock) {
        loadOrderDetails().firstOrNull { it.id == orderId }
    }

    fun appendInventoryAudit(record: InventorySyncAuditRecord) = synchronized(lock) {
        val current = loadInventoryAudits().toMutableList()
        current += record
        write(inventoryAuditFile, ListSerializer(InventorySyncAuditRecord.serializer()), current)
    }

    fun loadInventoryAuditsDesc(limit: Int = 500): List<InventorySyncAuditRecord> = synchronized(lock) {
        loadInventoryAudits()
            .sortedByDescending { it.createdAtEpochMs }
            .take(limit)
    }

    private fun loadOrderDetails(): List<InternalOrderDetailDto> {
        return read(orderDetailsFile, ListSerializer(InternalOrderDetailDto.serializer())) ?: emptyList()
    }

    private fun loadInventoryAudits(): List<InventorySyncAuditRecord> {
        return read(inventoryAuditFile, ListSerializer(InventorySyncAuditRecord.serializer())) ?: emptyList()
    }

    private fun <T> read(file: File, serializer: KSerializer<T>): T? {
        if (!file.exists()) return null
        return runCatching {
            json.decodeFromString(serializer, file.readText())
        }.getOrNull()
    }

    private fun <T> write(file: File, serializer: KSerializer<T>, value: T) {
        runCatching {
            file.writeText(json.encodeToString(serializer, value))
        }
    }
}

