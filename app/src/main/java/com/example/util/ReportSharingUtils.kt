package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithBalance
import java.io.File
import java.io.FileOutputStream

object ReportSharingUtils {

    private const val AUTHORITY = "com.kkySilver.erp.fileprovider"

    private fun getReportLabel(txType: String): String {
        val type = TransactionType.fromLabel(txType)
        return when (type) {
            TransactionType.DELIVERY -> "ஜதை கொடுத்தல்"
            TransactionType.RETURN_KACHA -> "வெள்ளி வரவு"
            else -> type.displayLabel
        }
    }

    fun shareTextReport(
        ctx: Context,
        shopName: String,
        items: List<TransactionWithBalance>,
        delivery: Double,
        returns: Double,
        hold: Double,
        dateRange: String
    ) {
        val sortedItems = items.sortedWith(compareBy({ it.tx.date }, { it.tx.time }))
        val sb = StringBuilder()
        sb.append("--- KKY SILVERS ---\n")
        sb.append("கடை: $shopName\n")
        if (dateRange != "முழு விவரம்") {
            sb.append("காலம்: $dateRange\n")
        }
        sb.append("------------------------------------------\n")
        sb.append(String.format("%-20s: %10.1f g\n", "மொத்த ஜதை கொடுத்தல்", delivery))
        sb.append(String.format("%-20s: %10.1f g\n", "மொத்த வெள்ளி வரவு", returns))
        sb.append(String.format("%-20s: %10.1f g\n", "தற்போதைய இருப்பு", hold))
        sb.append("------------------------------------------\n\n")

        if (sortedItems.isNotEmpty()) {
            sb.append("பரிவர்த்தனை விவரங்கள்:\n")
            sb.append("```\n")
            sb.append(String.format("%-10s | %-8s | %-10s | %-10s\n", "தேதி", "வகை", "Pure(g)", "இருப்பு(g)"))
            sb.append("-----------|----------|------------|-----------\n")
            sortedItems.forEach { item ->
                val tx = item.tx
                val type = getReportLabel(tx.type)
                val date = tx.date
                sb.append(String.format("%-10s | %-8s | %10.1f | %10.1f\n", date, type, tx.pureWeight, item.balanceAtThisPoint))
            }
            sb.append("```\n")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        ctx.startActivity(Intent.createChooser(intent, "அறிக்கை பகிரவும்"))
    }

    fun shareImageReport(
        ctx: Context,
        shopName: String,
        items: List<TransactionWithBalance>,
        delivery: Double,
        returns: Double,
        hold: Double,
        dateRange: String
    ) {
        val sortedItems = items.sortedWith(compareBy({ it.tx.date }, { it.tx.time }))
        val width = 1000 // Increased width for extra column
        val rowHeight = 60
        val headerHeight = 280
        val footerHeight = 100
        val totalHeight = headerHeight + (sortedItems.size * rowHeight) + footerHeight

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        canvas.drawColor(Color.WHITE)

        // Header
        paint.color = Color.parseColor("#1A237E") // DeliveryBlue approx
        canvas.drawRect(0f, 0f, width.toFloat(), 210f, paint)

        paint.color = Color.WHITE
        paint.textSize = 40f
        paint.isFakeBoldText = true
        canvas.drawText("KKY SILVERS - Salem", 40f, 70f, paint)

        paint.textSize = 28f
        paint.isFakeBoldText = false
        canvas.drawText("கடை: $shopName", 40f, 120f, paint)
        if (dateRange != "முழு விவரம்") {
            canvas.drawText("காலம்: $dateRange", 40f, 160f, paint)
        }

        // Summary Boxes
        paint.color = Color.parseColor("#F5F5F5")
        canvas.drawRect(20f, 220f, width - 20f, 330f, paint)

        paint.color = Color.BLACK
        paint.textSize = 24f
        canvas.drawText("ஜதை கொடுத்தல்: ${delivery}g", 40f, 270f, paint)
        canvas.drawText("வெள்ளி வரவு: ${returns}g", 400f, 270f, paint)
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#FF6F00") // HoldAmber
        canvas.drawText("கடை இருப்பு: ${hold}g", 740f, 270f, paint)

        // Table Header
        paint.color = Color.DKGRAY
        paint.textSize = 24f
        paint.isFakeBoldText = true
        val tableTop = 350f
        canvas.drawText("தேதி", 40f, tableTop, paint)
        canvas.drawText("வகை", 180f, tableTop, paint)
        canvas.drawText("எடை(g)", 380f, tableTop, paint)
        canvas.drawText("டச்", 540f, tableTop, paint)
        canvas.drawText("Pure(g)", 680f, tableTop, paint)
        canvas.drawText("இருப்பு(g)", 830f, tableTop, paint)

        canvas.drawLine(20f, tableTop + 10, width - 20f, tableTop + 10, paint)

        // Rows
        paint.isFakeBoldText = false
        paint.textSize = 20f
        var currentY = tableTop + 50f
        sortedItems.forEach { item ->
            val tx = item.tx
            canvas.drawText(tx.date, 40f, currentY, paint)
            canvas.drawText(getReportLabel(tx.type), 180f, currentY, paint)
            canvas.drawText("${tx.weight}", 380f, currentY, paint)
            canvas.drawText("${tx.touch}%", 540f, currentY, paint)
            canvas.drawText("${tx.pureWeight}", 680f, currentY, paint)
            canvas.drawText("${item.balanceAtThisPoint}", 830f, currentY, paint)
            currentY += rowHeight
        }

        saveAndShare(ctx, bitmap, "report_image.png")
    }

    fun sharePdfReport(
        ctx: Context,
        shopName: String,
        items: List<TransactionWithBalance>,
        delivery: Double,
        returns: Double,
        hold: Double,
        dateRange: String
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Styles
        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            textSize = 8f // Reduced for extra columns
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            textSize = 8f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
            color = Color.LTGRAY
        }

        var y = 50f
        canvas.drawText("KKY SILVERS STATEMENT", 200f, y, titlePaint)
        y += 25f
        canvas.drawText("Shop: $shopName", 50f, y, textPaint)
        if (dateRange != "முழு விவரம்") {
            y += 15f
            canvas.drawText("Period: $dateRange", 50f, y, textPaint)
        }
        y += 15f
        canvas.drawText("Summary: Jathai Delivery: ${delivery}g | Silver Return: ${returns}g | HOLD: ${hold}g", 50f, y, headerPaint)
        y += 40f

        // Table Constants
        val startX = 40f
        val endX = 555f
        val colDate = 40f
        val colType = 95f
        val colWeight = 185f
        val colTouch = 245f
        val colPure = 295f
        val colBalance = 355f
        val colRemarks = 425f

        fun drawCellText(text: String, startX: Float, nextStartX: Float, currentY: Float, paint: Paint, align: Paint.Align = Paint.Align.LEFT) {
            val width = nextStartX - startX
            val textWidth = paint.measureText(text)
            val x = when (align) {
                Paint.Align.CENTER -> startX + (width - textWidth) / 2f
                Paint.Align.RIGHT -> nextStartX - textWidth - 5f
                else -> startX + 5f
            }
            canvas.drawText(text, x, currentY, paint)
        }

        fun drawHeaderRow(currentY: Float) {
            canvas.drawRect(startX, currentY - 15, endX, currentY + 5, Paint().apply { color = Color.parseColor("#EEEEEE") })
            drawCellText("Date", colDate, colType, currentY, headerPaint, Paint.Align.CENTER)
            drawCellText("Type", colType, colWeight, currentY, headerPaint, Paint.Align.CENTER)
            drawCellText("Weight", colWeight, colTouch, currentY, headerPaint, Paint.Align.CENTER)
            drawCellText("Touch", colTouch, colPure, currentY, headerPaint, Paint.Align.CENTER)
            drawCellText("Pure(g)", colPure, colBalance, currentY, headerPaint, Paint.Align.CENTER)
            drawCellText("Balance(g)", colBalance, colRemarks, currentY, headerPaint, Paint.Align.CENTER)
            drawCellText("Remarks & Images", colRemarks, endX, currentY, headerPaint, Paint.Align.LEFT)
            
            // Draw border for header
            canvas.drawRect(startX, currentY - 15, endX, currentY + 5, borderPaint)
        }

        drawHeaderRow(y)
        y += 5f // Move to bottom of header row start

        val sortedItems = items.sortedWith(compareBy({ it.tx.date }, { it.tx.time }))

        sortedItems.forEach { item ->
            val tx = item.tx
            val imageUris = ImageUtils.parseImageUris(tx.imageUri)
            val imageCount = imageUris.size
            val imagePadding = 10f
            val imageDrawHeight = 160f
            val rowHeight = if (imageCount > 0) {
                30f + (imageCount * (imageDrawHeight + imagePadding))
            } else 30f

            if (y + rowHeight > 800) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
                drawHeaderRow(y)
                y += 5f
            }

            val startY = y
            val nextY = startY + rowHeight

            // Draw text content with improved alignment
            drawCellText(tx.date, colDate, colType, startY + 20, textPaint, Paint.Align.CENTER)
            drawCellText(getReportLabel(tx.type), colType, colWeight, startY + 20, textPaint, Paint.Align.CENTER)
            drawCellText("${tx.weight}g", colWeight, colTouch, startY + 20, textPaint, Paint.Align.RIGHT)
            drawCellText("${tx.touch}%", colTouch, colPure, startY + 20, textPaint, Paint.Align.RIGHT)
            drawCellText("${tx.pureWeight}g", colPure, colBalance, startY + 20, textPaint, Paint.Align.RIGHT)
            drawCellText("${item.balanceAtThisPoint}g", colBalance, colRemarks, startY + 20, textPaint, Paint.Align.RIGHT)
            
            val remarks = if (tx.remarks.length > 25) tx.remarks.take(22) + "..." else tx.remarks
            drawCellText(remarks, colRemarks, endX, startY + 20, textPaint, Paint.Align.LEFT)

            // Draw Images
            if (imageCount > 0) {
                var imgY = startY + 35f
                val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                val maxImgWidth = endX - colRemarks - 10f
                
                imageUris.forEach { uriStr ->
                    try {
                        var bitmap: Bitmap? = null
                        if (uriStr.startsWith("data:image/")) {
                            val base64Data = uriStr.substringAfter("base64,")
                            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } else if (uriStr.startsWith("content://") || uriStr.startsWith("file://")) {
                            val inputStream = ctx.contentResolver.openInputStream(Uri.parse(uriStr))
                            bitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream?.close()
                        }

                        bitmap?.let {
                            val scale = Math.min(maxImgWidth / it.width, imageDrawHeight / it.height)
                            val drawWidth = it.width * scale
                            val drawHeight = it.height * scale
                            
                            val left = colRemarks + 5f + (maxImgWidth - drawWidth) / 2f
                            val finalDestRect = RectF(left, imgY, left + drawWidth, imgY + drawHeight)
                            
                            canvas.drawBitmap(it, null, finalDestRect, imagePaint)
                            imgY += drawHeight + imagePadding
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // Draw Row Borders
            canvas.drawRect(startX, startY, endX, nextY, borderPaint)
            // Vertical separators
            canvas.drawLine(colType, startY, colType, nextY, borderPaint)
            canvas.drawLine(colWeight, startY, colWeight, nextY, borderPaint)
            canvas.drawLine(colTouch, startY, colTouch, nextY, borderPaint)
            canvas.drawLine(colPure, startY, colPure, nextY, borderPaint)
            canvas.drawLine(colBalance, startY, colBalance, nextY, borderPaint)
            canvas.drawLine(colRemarks, startY, colRemarks, nextY, borderPaint)

            y = nextY
        }

        pdfDocument.finishPage(page)

        val file = File(ctx.cacheDir, "reports")
        if (!file.exists()) file.mkdirs()
        val pdfFile = File(file, "statement_${System.currentTimeMillis()}.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            pdfDocument.close()
            shareFile(ctx, pdfFile, "application/pdf")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveAndShare(ctx: Context, bitmap: Bitmap, fileName: String) {
        val cachePath = File(ctx.cacheDir, "reports")
        cachePath.mkdirs()
        val file = File(cachePath, fileName)
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()

        shareFile(ctx, file, "image/png")
    }

    private fun shareFile(ctx: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(ctx, AUTHORITY, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(Intent.createChooser(intent, "அறிக்கை பகிரவும்"))
    }
}
