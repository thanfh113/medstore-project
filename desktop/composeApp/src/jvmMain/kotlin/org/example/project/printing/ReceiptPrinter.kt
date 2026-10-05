package org.example.project.printing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.project.data.repositories.InternalOrderDetailDto
import java.awt.Color
import java.awt.Desktop
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.time.LocalDate
import java.util.Locale
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

data class PosReceiptData(
    val order: InternalOrderDetailDto,
    val cashReceived: Double? = null,
    val cashChange: Double? = null,
    val paymentReference: String? = null,
    val paidAt: String? = null
)

class ReceiptPdfArchiver {
    suspend fun archiveReceipt(data: PosReceiptData): Result<File> = withContext(Dispatchers.IO) {
        try {
            val lines = ReceiptFormatter.format(data)
            val targetDir = invoiceDirectoryFor(data)
            val outputFile = createUniqueFile(targetDir, data.order.orderCode)
            val pageImages = ReceiptPageRenderer.render(lines)
            writePdf(outputFile, pageImages)
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.message ?: "Khong the luu hoa don PDF"))
        }
    }

    fun openInvoiceDirectory(invoiceFile: File? = null): Result<File> {
        val targetDir = invoiceFile?.parentFile ?: invoiceRootDirectory()
        return runCatching {
            targetDir.mkdirs()
            if (!Desktop.isDesktopSupported()) {
                throw IllegalStateException("Desktop khong ho tro mo thu muc hoa don")
            }
            Desktop.getDesktop().open(targetDir)
            targetDir
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(IllegalStateException(it.message ?: "Khong the mo thu muc hoa don")) }
        )
    }

    fun invoiceRootDirectory(): File {
        return File(System.getProperty("user.home"), ".nhathuoc-desktop/invoices").apply { mkdirs() }
    }

    private fun invoiceDirectoryFor(data: PosReceiptData): File {
        val invoiceDate = parseInvoiceDate(data.paidAt ?: data.order.paidAt ?: data.order.createdAt)
        return File(invoiceRootDirectory(), invoiceDate.toString()).apply { mkdirs() }
    }

    private fun createUniqueFile(targetDir: File, orderCode: String): File {
        val sanitizedCode = orderCode.replace(Regex("[^A-Za-z0-9_-]"), "_")
        var index = 0
        while (true) {
            val suffix = if (index == 0) "" else "-$index"
            val candidate = File(targetDir, "invoice-$sanitizedCode$suffix.pdf")
            if (!candidate.exists()) return candidate
            index++
        }
    }
}

private object ReceiptFormatter {
    private const val lineWidth = 40

    fun format(data: PosReceiptData): List<String> {
        val order = data.order
        val lines = mutableListOf<String>()

        lines += center("MEDSTORE")
        lines += center("HÓA ĐƠN POS")
        lines += divider()
        lines += "Mã đơn: ${order.orderCode}"
        lines += "Ngày tạo: ${formatTimestamp(order.createdAt)}"
        data.paidAt?.takeIf { it.isNotBlank() }?.let {
            lines += "Thanh toán lúc: ${formatTimestamp(it)}"
        }

        val customerName = order.customerName
            ?.takeIf { it.isNotBlank() }
            ?: if (order.customerId == "WALK_IN") "Khách tại quầy" else order.customerId
        lines += "Khách: $customerName"
        order.customerPhone?.takeIf { it.isNotBlank() }?.let { lines += "ĐT: $it" }
        order.cashierName?.takeIf { it.isNotBlank() }?.let { lines += "Thu ngân: $it" }
        lines += "Thanh toán: ${order.paymentMethod ?: "UNKNOWN"}"
        (data.paymentReference ?: order.paymentReference)?.takeIf { it.isNotBlank() }?.let { lines += "Mã GD: $it" }

        lines += divider()
        order.items.forEachIndexed { index, item ->
            if (index > 0) lines += ""
            lines += wrap(item.name)
            lines += formatLine(
                left = "${item.quantity} x ${formatMoney(item.price)} / ${item.unit}",
                right = formatMoney(item.price * item.quantity)
            )
        }

        lines += divider()
        lines += formatLine("Tạm tính", formatMoney(order.subtotal ?: order.total ?: 0.0))
        if (order.discount > 0.0) {
            lines += formatLine("Giảm giá", "-${formatMoney(order.discount)}")
        }
        if (order.shippingFee > 0.0) {
            lines += formatLine("Phí giao", formatMoney(order.shippingFee))
        }
        lines += formatLine("Tổng cộng", formatMoney(order.total ?: 0.0))
        (data.cashReceived ?: order.cashReceived)?.let { lines += formatLine("Tiền khách đưa", formatMoney(it)) }
        (data.cashChange ?: order.cashChange)?.let { lines += formatLine("Tiền thối", formatMoney(it)) }

        order.note?.takeIf { it.isNotBlank() }?.let {
            lines += divider()
            lines += "Ghi chú:"
            lines += wrap(it)
        }

        lines += divider()
        lines += center("Cảm ơn Quý khách")
        return lines
    }

    private fun wrap(text: String): List<String> {
        val normalized = text.trim()
        if (normalized.isEmpty()) return listOf("")

        val words = normalized.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var current = ""

        for (word in words) {
            if (word.length >= lineWidth) {
                if (current.isNotEmpty()) {
                    lines += current
                    current = ""
                }
                lines += word.chunked(lineWidth)
                continue
            }

            val candidate = if (current.isEmpty()) word else "$current $word"
            if (candidate.length <= lineWidth) {
                current = candidate
            } else {
                lines += current
                current = word
            }
        }

        if (current.isNotEmpty()) lines += current
        return lines
    }

    private fun formatLine(left: String, right: String): String {
        val cleanLeft = left.trim()
        val cleanRight = right.trim()
        if (cleanLeft.length + cleanRight.length + 1 >= lineWidth) {
            val maxLeft = max(1, lineWidth - cleanRight.length - 1)
            val croppedLeft = cleanLeft.take(maxLeft)
            return croppedLeft.padEnd(lineWidth - cleanRight.length, ' ') + cleanRight
        }
        return cleanLeft.padEnd(lineWidth - cleanRight.length, ' ') + cleanRight
    }

    private fun formatMoney(amount: Double): String {
        val formatter = DecimalFormat("#,##0.##")
        return formatter.format(amount)
    }

    private fun formatTimestamp(raw: String): String {
        return raw.replace('T', ' ').take(19)
    }

    private fun divider(): String = "-".repeat(lineWidth)

    private fun center(text: String): String {
        val clean = text.trim().take(lineWidth)
        val leftPadding = ((lineWidth - clean.length) / 2).coerceAtLeast(0)
        return " ".repeat(leftPadding) + clean
    }
}

private object ReceiptPageRenderer {
    private const val pageWidthPx = 900
    private const val horizontalPadding = 60
    private const val topPadding = 48
    private const val bottomPadding = 48
    private const val lineHeight = 26
    private const val maxLinesPerPage = 44
    private val font = Font("Dialog", Font.PLAIN, 18)

    fun render(lines: List<String>): List<BufferedImage> {
        val chunks = lines.chunked(maxLinesPerPage)
        return chunks.map { renderPage(it) }
    }

    private fun renderPage(lines: List<String>): BufferedImage {
        val pageHeightPx = topPadding + bottomPadding + (lines.size * lineHeight)
        val image = BufferedImage(pageWidthPx, pageHeightPx, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        try {
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, image.width, image.height)
            graphics.color = Color.BLACK
            graphics.font = font
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            var y = topPadding
            lines.forEach { line ->
                graphics.drawString(line, horizontalPadding, y)
                y += lineHeight
            }
        } finally {
            graphics.dispose()
        }
        return image
    }
}

private fun writePdf(outputFile: File, pageImages: List<BufferedImage>) {
    require(pageImages.isNotEmpty()) { "Khong co noi dung hoa don de luu PDF" }

    val objects = mutableListOf<ByteArray>()
    val pageObjectIds = mutableListOf<Int>()
    val pagesRootId = 2

    fun addObject(content: ByteArray): Int {
        objects += content
        return objects.size
    }

    addObject("<< /Type /Catalog /Pages $pagesRootId 0 R >>".toPdfBytes())
    addObject(ByteArray(0))

    pageImages.forEachIndexed { index, image ->
        val jpegBytes = image.toJpegBytes()
        val imageObjectId = addObject(
            buildStreamObject(
                """
                << /Type /XObject
                /Subtype /Image
                /Width ${image.width}
                /Height ${image.height}
                /ColorSpace /DeviceRGB
                /BitsPerComponent 8
                /Filter /DCTDecode
                /Length ${jpegBytes.size}
                >>
                """.trimIndent(),
                jpegBytes
            )
        )

        val pageWidthPt = image.width / 2.0
        val pageHeightPt = image.height / 2.0
        val contentBytes = """
            q
            ${formatPdfNumber(pageWidthPt)} 0 0 ${formatPdfNumber(pageHeightPt)} 0 0 cm
            /Im${index + 1} Do
            Q
        """.trimIndent().toPdfBytes()
        val contentObjectId = addObject(
            buildStreamObject(
                "<< /Length ${contentBytes.size} >>",
                contentBytes
            )
        )

        val pageObjectId = addObject(
            """
            << /Type /Page
            /Parent $pagesRootId 0 R
            /MediaBox [0 0 ${formatPdfNumber(pageWidthPt)} ${formatPdfNumber(pageHeightPt)}]
            /Resources << /XObject << /Im${index + 1} $imageObjectId 0 R >> >>
            /Contents $contentObjectId 0 R
            >>
            """.trimIndent().toPdfBytes()
        )
        pageObjectIds += pageObjectId
    }

    objects[pagesRootId - 1] = """
        << /Type /Pages
        /Count ${pageObjectIds.size}
        /Kids [${pageObjectIds.joinToString(" ") { "$it 0 R" }}]
        >>
    """.trimIndent().toPdfBytes()

    FileOutputStream(outputFile).use { output ->
        output.write("%PDF-1.4\n".toPdfBytes())
        output.write("%\u00E2\u00E3\u00CF\u00D3\n".toPdfBytes())

        val offsets = mutableListOf<Int>()
        var position = "%PDF-1.4\n".toPdfBytes().size + "%\u00E2\u00E3\u00CF\u00D3\n".toPdfBytes().size
        objects.forEachIndexed { index, content ->
            offsets += position
            val objectHeader = "${index + 1} 0 obj\n".toPdfBytes()
            val objectFooter = "\nendobj\n".toPdfBytes()
            output.write(objectHeader)
            output.write(content)
            output.write(objectFooter)
            position += objectHeader.size + content.size + objectFooter.size
        }

        val xrefStart = position
        output.write("xref\n".toPdfBytes())
        output.write("0 ${objects.size + 1}\n".toPdfBytes())
        output.write("0000000000 65535 f \n".toPdfBytes())
        offsets.forEach { offset ->
            output.write(String.format(Locale.US, "%010d 00000 n \n", offset).toPdfBytes())
        }
        output.write(
            """
            trailer
            << /Size ${objects.size + 1} /Root 1 0 R >>
            startxref
            $xrefStart
            %%EOF
            """.trimIndent().toPdfBytes()
        )
    }
}

private fun buildStreamObject(dictionary: String, stream: ByteArray): ByteArray {
    val header = "$dictionary\nstream\n".toPdfBytes()
    val footer = "\nendstream".toPdfBytes()
    return ByteArray(header.size + stream.size + footer.size).also { bytes ->
        System.arraycopy(header, 0, bytes, 0, header.size)
        System.arraycopy(stream, 0, bytes, header.size, stream.size)
        System.arraycopy(footer, 0, bytes, header.size + stream.size, footer.size)
    }
}

private fun BufferedImage.toJpegBytes(): ByteArray {
    val output = ByteArrayOutputStream()
    ImageIO.write(this, "jpg", output)
    return output.toByteArray()
}

private fun String.toPdfBytes(): ByteArray = toByteArray(StandardCharsets.ISO_8859_1)

private fun formatPdfNumber(value: Double): String = String.format(Locale.US, "%.2f", value)

private fun parseInvoiceDate(raw: String?): LocalDate {
    return raw
        ?.takeIf { it.length >= 10 }
        ?.let { value -> runCatching { LocalDate.parse(value.take(10)) }.getOrNull() }
        ?: LocalDate.now()
}
