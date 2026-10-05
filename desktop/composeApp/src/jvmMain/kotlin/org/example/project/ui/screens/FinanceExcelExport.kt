package org.example.project.ui.screens

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.example.project.data.repositories.FinanceSummaryDto

internal fun buildFinanceXlsx(d: FinanceSummaryDto): ByteArray {
    val wb = XSSFWorkbook()
    val moneyFmt = wb.createDataFormat().getFormat("#,##0")

    // ── Fonts ─────────────────────────────────────────────────────────────────
    fun xFont(bold: Boolean = false, pts: Short = 10, r: Int = -1, g: Int = -1, b: Int = -1): XSSFFont =
        (wb.createFont() as XSSFFont).also { f ->
            f.bold = bold
            f.fontHeightInPoints = pts
            if (r >= 0) f.setColor(XSSFColor(java.awt.Color(r, g, b), null))
        }

    val fWhiteBold  = xFont(bold = true, r = 255, g = 255, b = 255)
    val fTitleBold  = xFont(bold = true, pts = 14, r = 255, g = 255, b = 255)
    val fGreenBold  = xFont(bold = true, r = 0x1B, g = 0x5E, b = 0x20)
    val fRedBold    = xFont(bold = true, r = 0xC5, g = 0x30, b = 0x30)
    val fBold       = xFont(bold = true)
    val fGrayLabel  = xFont(bold = true, r = 0x71, g = 0x80, b = 0x96)

    // ── Style factory ─────────────────────────────────────────────────────────
    fun mkStyle(
        bgR: Int = -1, bgG: Int = -1, bgB: Int = -1,
        font: XSSFFont? = null,
        ha: HorizontalAlignment = HorizontalAlignment.LEFT,
        money: Boolean = false,
        borders: Boolean = false
    ): XSSFCellStyle = (wb.createCellStyle() as XSSFCellStyle).also { s ->
        if (bgR >= 0) {
            s.setFillForegroundColor(XSSFColor(java.awt.Color(bgR, bgG, bgB), null))
            s.fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        if (font != null) s.setFont(font)
        s.alignment = ha
        if (money) s.dataFormat = moneyFmt
        if (borders) {
            s.borderTop = BorderStyle.THIN; s.borderBottom = BorderStyle.THIN
            s.borderLeft = BorderStyle.THIN; s.borderRight = BorderStyle.THIN
        }
    }

    // ── Pre-created styles ────────────────────────────────────────────────────
    val sTitle       = mkStyle(bgR = 0x15, bgG = 0x5D, bgB = 0x30, font = fTitleBold)
    val sSec         = mkStyle(bgR = 0x2D, bgG = 0x37, bgB = 0x48, font = xFont(bold = true, pts = 11, r = 255, g = 255, b = 255))
    val sTblHdr      = mkStyle(bgR = 0x1F, bgG = 0x8C, bgB = 0x4B, font = fWhiteBold, ha = HorizontalAlignment.CENTER, borders = true)
    val sDefault     = mkStyle()
    val sAlt         = mkStyle(bgR = 0xF0, bgG = 0xF4, bgB = 0xF8)
    val sBold        = mkStyle(font = fBold)
    val sAltBold     = mkStyle(bgR = 0xF0, bgG = 0xF4, bgB = 0xF8, font = fBold)
    val sLabel       = mkStyle(font = fGrayLabel, ha = HorizontalAlignment.RIGHT)
    val sCenter      = mkStyle(ha = HorizontalAlignment.CENTER)
    val sAltCenter   = mkStyle(bgR = 0xF0, bgG = 0xF4, bgB = 0xF8, ha = HorizontalAlignment.CENTER)
    val sMoney       = mkStyle(money = true, ha = HorizontalAlignment.RIGHT)
    val sAltMoney    = mkStyle(bgR = 0xF0, bgG = 0xF4, bgB = 0xF8, money = true, ha = HorizontalAlignment.RIGHT)
    val sMoneyPos    = mkStyle(font = fGreenBold, money = true, ha = HorizontalAlignment.RIGHT)
    val sMoneyNeg    = mkStyle(font = fRedBold, money = true, ha = HorizontalAlignment.RIGHT)
    val sAltMoneyPos = mkStyle(bgR = 0xF0, bgG = 0xF4, bgB = 0xF8, font = fGreenBold, money = true, ha = HorizontalAlignment.RIGHT)
    val sAltMoneyNeg = mkStyle(bgR = 0xF0, bgG = 0xF4, bgB = 0xF8, font = fRedBold, money = true, ha = HorizontalAlignment.RIGHT)
    val sValPos      = mkStyle(font = fGreenBold, money = true)
    val sValNeg      = mkStyle(font = fRedBold, money = true)

    val now = java.time.LocalDateTime.now()
    val dateStr = "%02d/%02d/%04d %02d:%02d".format(now.dayOfMonth, now.monthValue, now.year, now.hour, now.minute)

    // ── Row/cell helpers ──────────────────────────────────────────────────────
    fun XSSFFont.toStyle(ha: HorizontalAlignment = HorizontalAlignment.CENTER) =
        mkStyle(font = this, ha = ha)

    fun XSSFWorkbook.makeSheet(name: String, headers: List<String>, widths: List<Int>): org.apache.poi.xssf.usermodel.XSSFSheet {
        val sheet = createSheet(name) as org.apache.poi.xssf.usermodel.XSSFSheet
        val row = sheet.createRow(0)
        row.heightInPoints = 22f
        headers.forEachIndexed { i, text ->
            row.createCell(i).apply { setCellValue(text); cellStyle = sTblHdr }
            sheet.setColumnWidth(i, widths[i] * 256)
        }
        return sheet
    }

    fun Row.txt(col: Int, v: String, alt: Boolean = false) =
        createCell(col).apply { setCellValue(v); cellStyle = if (alt) sAlt else sDefault }
    fun Row.bold(col: Int, v: String, alt: Boolean = false) =
        createCell(col).apply { setCellValue(v); cellStyle = if (alt) sAltBold else sBold }
    fun Row.ctr(col: Int, v: Int, alt: Boolean = false) =
        createCell(col).apply { setCellValue(v.toDouble()); cellStyle = if (alt) sAltCenter else sCenter }
    fun Row.num(col: Int, v: Double, alt: Boolean = false) =
        createCell(col).apply { setCellValue(v); cellStyle = if (alt) sAltMoney else sMoney }
    fun Row.profit(col: Int, v: Double, alt: Boolean = false) =
        createCell(col).apply {
            setCellValue(v)
            cellStyle = when {
                v >= 0 && alt -> sAltMoneyPos
                v >= 0        -> sMoneyPos
                alt           -> sAltMoneyNeg
                else          -> sMoneyNeg
            }
        }

    // ── Sheet 1: Tổng quan ────────────────────────────────────────────────────
    (wb.createSheet("Tổng quan") as org.apache.poi.xssf.usermodel.XSSFSheet).apply {
        createRow(0).apply {
            heightInPoints = 32f
            createCell(0).apply { setCellValue("BÁO CÁO TÀI CHÍNH — NHÀ THUỐC MEDSTORE"); cellStyle = sTitle }
            (1..4).forEach { createCell(it).cellStyle = sTitle }
        }
        addMergedRegion(CellRangeAddress(0, 0, 0, 4))
        createRow(1).apply {
            createCell(0).apply { setCellValue("Kỳ báo cáo:"); cellStyle = sLabel }
            createCell(1).apply { setCellValue(d.periodLabel); cellStyle = sBold }
        }
        createRow(2).apply {
            createCell(0).apply { setCellValue("Xuất lúc:"); cellStyle = sLabel }
            createCell(1).setCellValue(dateStr)
        }

        fun sectionHeader(rowIdx: Int, title: String) {
            createRow(rowIdx).apply {
                createCell(0).apply { setCellValue(title); cellStyle = sSec }
                (1..4).forEach { createCell(it).cellStyle = sSec }
            }
            addMergedRegion(CellRangeAddress(rowIdx, rowIdx, 0, 4))
        }
        fun metricRow(rowIdx: Int, label: String, value: Double, valStyle: XSSFCellStyle) {
            createRow(rowIdx).apply {
                createCell(0).apply { setCellValue(label); cellStyle = sLabel }
                createCell(1).apply { setCellValue(value); cellStyle = valStyle }
            }
        }

        sectionHeader(4, "DOANH THU")
        metricRow(5, "Doanh thu gộp", d.grossRevenue, sValPos)
        metricRow(6, "Kênh online", d.onlineRevenue, sMoney)
        metricRow(7, "Kênh POS", d.posRevenue, sMoney)
        metricRow(8, "Giá trị TB/đơn", d.averageOrderValue, sMoney)

        sectionHeader(10, "CHI PHÍ & LỢI NHUẬN")
        metricRow(11, "Chiết khấu", d.totalDiscount, sValNeg)
        metricRow(12, "Chi phí vốn", d.totalExpenses, sValNeg)
        metricRow(13, "Hoàn tiền", d.totalRefunds, sValNeg)
        metricRow(14, "Lợi nhuận thuần", d.netProfit, if (d.netProfit >= 0) sValPos else sValNeg)

        sectionHeader(16, "THỐNG KÊ ĐƠN HÀNG")
        listOf(
            17 to ("Tổng đơn hàng" to d.totalOrderCount),
            18 to ("Đơn thành công" to d.successfulOrderCount),
            19 to ("Đơn hủy" to d.cancelledOrderCount),
            20 to ("Đơn hoàn tiền" to d.refundedOrderCount)
        ).forEach { (rowIdx, pair) ->
            createRow(rowIdx).apply {
                createCell(0).apply { setCellValue(pair.first); cellStyle = sLabel }
                createCell(1).apply { setCellValue(pair.second.toDouble()); cellStyle = sCenter }
            }
        }

        setColumnWidth(0, 24 * 256)
        setColumnWidth(1, 20 * 256)
    }

    // ── Sheet 2: Theo thời gian ───────────────────────────────────────────────
    if (d.timeBreakdown.isNotEmpty()) {
        wb.makeSheet(
            "Theo thời gian",
            listOf("Thời gian", "Tổng đơn", "Đơn đạt", "SL bán", "Doanh thu (VNĐ)", "Lãi gộp (VNĐ)"),
            listOf(18, 10, 10, 10, 20, 20)
        ).apply {
            d.timeBreakdown.sortedBy { it.sortKey }.forEachIndexed { i, item ->
                val alt = i % 2 == 1
                createRow(i + 1).apply {
                    bold(0, item.label, alt)
                    ctr(1, item.orderCount, alt)
                    ctr(2, item.successfulOrderCount, alt)
                    ctr(3, item.quantitySold, alt)
                    num(4, item.grossRevenue, alt)
                    profit(5, item.netProfit, alt)
                }
            }
        }
    }

    // ── Sheet 3: Theo loại sản phẩm ──────────────────────────────────────────
    if (d.categoryReports.isNotEmpty()) {
        wb.makeSheet(
            "Theo loại sản phẩm",
            listOf("Loại sản phẩm", "Tồn kho", "Đã bán", "Doanh thu (VNĐ)", "Giá vốn (VNĐ)", "Lãi gộp (VNĐ)"),
            listOf(26, 10, 10, 20, 20, 20)
        ).apply {
            d.categoryReports.sortedByDescending { it.revenue }.forEachIndexed { i, item ->
                val alt = i % 2 == 1
                createRow(i + 1).apply {
                    bold(0, item.categoryName, alt)
                    ctr(1, item.stockQuantity, alt)
                    ctr(2, item.quantitySold, alt)
                    num(3, item.revenue, alt)
                    num(4, item.cost, alt)
                    profit(5, item.netProfit, alt)
                }
            }
        }
    }

    // ── Sheet 4: Từng sản phẩm ────────────────────────────────────────────────
    if (d.productReports.isNotEmpty()) {
        wb.makeSheet(
            "Từng sản phẩm",
            listOf("Sản phẩm", "Loại", "Tồn", "Bán", "Doanh thu (VNĐ)", "Lãi gộp (VNĐ)"),
            listOf(34, 20, 8, 8, 20, 20)
        ).apply {
            d.productReports.sortedByDescending { it.revenue }.forEachIndexed { i, item ->
                val alt = i % 2 == 1
                createRow(i + 1).apply {
                    txt(0, item.productName, alt)
                    txt(1, item.categoryName, alt)
                    ctr(2, item.stockQuantity, alt)
                    ctr(3, item.quantitySold, alt)
                    num(4, item.revenue, alt)
                    profit(5, item.netProfit, alt)
                }
            }
        }
    }

    // ── Sheet 5: Top bán chạy ─────────────────────────────────────────────────
    if (d.topSellingProducts.isNotEmpty()) {
        wb.makeSheet(
            "Top bán chạy",
            listOf("#", "Tên sản phẩm", "Số lượng bán", "Doanh thu (VNĐ)"),
            listOf(6, 36, 14, 20)
        ).apply {
            d.topSellingProducts.forEachIndexed { i, p ->
                val alt = i % 2 == 1
                createRow(i + 1).apply {
                    ctr(0, i + 1, alt)
                    createCell(1).apply {
                        setCellValue(p.productName)
                        cellStyle = if (i < 3) (if (alt) sAltBold else sBold) else (if (alt) sAlt else sDefault)
                    }
                    ctr(2, p.quantitySold, alt)
                    num(3, p.revenue, alt)
                }
            }
        }
    }

    return java.io.ByteArrayOutputStream().also { wb.write(it) }.toByteArray()
}
