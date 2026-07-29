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
import java.io.File
import java.io.FileOutputStream

object ReportSharingUtils {

    private const val AUTHORITY = "com.kkySilver.erp.fileprovider"

    fun shareTextReport(
        ctx: Context,
        shopName: String,
        transactions: List<Transaction>,
        delivery: Double,
        returns: Double,
        hold: Double,
        dateRange: String
    ) {
        val sb = StringBuilder()
        sb.append("--- KKY SILVERS ---\n")
        sb.append("கடை: $shopName\n")
        sb.append("காலம்: $dateRange\n")
        sb.append("------------------------------------------\n")
        sb.append(String.format("%-20s: %10.1f g\n", "மொத்த கொடுத்தல்", delivery))
        sb.append(String.format("%-20s: %10.1f g\n", "மொத்த வரவு", returns))
        sb.append(String.format("%-20s: %10.1f g\n", "தற்போதைய இருப்பு", hold))
        sb.append("------------------------------------------\n\n")

        if (transactions.isNotEmpty()) {
            sb.append("பரிவர்த்தனை விவரங்கள்:\n")
            sb.append("```\n")
            sb.append(String.format("%-16s | %-8s | %-10s\n", "தேதி & நேரம்", "வகை", "Pure(g)"))
            sb.append("-----------------|----------|-----------\n")
            transactions.forEach { tx ->
                val type = TransactionType.fromLabel(tx.type).displayLabel
                val dateTime = "${tx.date} ${tx.time}".trim()
                sb.append(String.format("%-16s | %-8s | %10.1f\n", dateTime, type, tx.pureWeight))
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
        transactions: List<Transaction>,
        delivery: Double,
        returns: Double,
        hold: Double,
        dateRange: String
    ) {
        val sortedTransactions = transactions // Already sorted in screen
        val width = 800
        val rowHeight = 60
        val headerHeight = 280
        val footerHeight = 100
        val totalHeight = headerHeight + (sortedTransactions.size * rowHeight) + footerHeight

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
        canvas.drawText("காலம்: $dateRange", 40f, 160f, paint)

        // Summary Boxes
        paint.color = Color.parseColor("#F5F5F5")
        canvas.drawRect(20f, 220f, width - 20f, 330f, paint)

        paint.color = Color.BLACK
        paint.textSize = 24f
        canvas.drawText("கொடுத்தல்: ${delivery}g", 40f, 270f, paint)
        canvas.drawText("வரவு: ${returns}g", 290f, 270f, paint)
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#FF6F00") // HoldAmber
        canvas.drawText("கடை இருப்பு: ${hold}g", 530f, 270f, paint)

        // Table Header
        paint.color = Color.DKGRAY
        paint.textSize = 24f
        paint.isFakeBoldText = true
        val tableTop = 350f
        canvas.drawText("தேதி & நேரம்", 40f, tableTop, paint)
        canvas.drawText("வகை", 260f, tableTop, paint)
        canvas.drawText("எடை(g)", 400f, tableTop, paint)
        canvas.drawText("டச்", 580f, tableTop, paint)
        canvas.drawText("Pure(g)", 700f, tableTop, paint)

        canvas.drawLine(20f, tableTop + 10, width - 20f, tableTop + 10, paint)

        // Rows
        paint.isFakeBoldText = false
        paint.textSize = 20f
        var currentY = tableTop + 50f
        sortedTransactions.forEach { tx ->
            canvas.drawText("${tx.date} ${tx.time}".trim(), 40f, currentY, paint)
            canvas.drawText(TransactionType.fromLabel(tx.type).displayLabel, 260f, currentY, paint)
            canvas.drawText("${tx.weight}", 400f, currentY, paint)
            canvas.drawText("${tx.touch}%", 580f, currentY, paint)
            canvas.drawText("${tx.pureWeight}", 700f, currentY, paint)
            currentY += rowHeight
        }

        saveAndShare(ctx, bitmap, "report_image.png")
    }

    fun sharePdfReport(
        ctx: Context,
        shopName: String,
        transactions: List<Transaction>,
        delivery: Double,
        returns: Double,
        hold: Double,
        dateRange: String
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()

        // Styles
        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            textSize = 9f
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            textSize = 9f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.LTGRAY
        }

        var y = 50f
        canvas.drawText("KKY SILVERS STATEMENT", 200f, y, titlePaint)
        y += 25f
        canvas.drawText("Shop: $shopName", 50f, y, textPaint)
        y += 15f
        canvas.drawText("Period: $dateRange", 50f, y, textPaint)
        y += 15f
        canvas.drawText("Summary: Delivery: ${delivery}g | Return: ${returns}g | HOLD: ${hold}g", 50f, y, headerPaint)
        y += 40f

        // Table Constants
        val colDate = 50f
        val colType = 135f
        val colWeight = 200f
        val colTouch = 260f
        val colPure = 320f
        val colRemarks = 380f

        fun drawHeaderRow(currentY: Float) {
            canvas.drawRect(50f, currentY - 15, 550f, currentY + 5, Paint().apply { color = Color.parseColor("#EEEEEE") })
            canvas.drawText("Date & Time", colDate + 5, currentY, headerPaint)
            canvas.drawText("Type", colType + 5, currentY, headerPaint)
            canvas.drawText("Weight", colWeight + 5, currentY, headerPaint)
            canvas.drawText("Touch", colTouch + 5, currentY, headerPaint)
            canvas.drawText("Pure(g)", colPure + 5, currentY, headerPaint)
            canvas.drawText("Remarks & Images", colRemarks + 5, currentY, headerPaint)
            
            // Draw border for header
            canvas.drawRect(50f, currentY - 15, 550f, currentY + 5, borderPaint)
        }

        drawHeaderRow(y)
        y += 5f // Move to bottom of header row start

        val sortedTransactions = transactions // Already sorted in screen

        sortedTransactions.forEach { tx ->
            val imageUris = ImageUtils.parseImageUris(tx.imageUri)
            val hasImage = imageUris.isNotEmpty()
            val rowHeight = if (hasImage) 220f else 30f

            if (y + rowHeight > 800) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
                drawHeaderRow(y)
                y += 5f
            }

            val startY = y
            val endY = startY + rowHeight

            // Draw content
            canvas.drawText("${tx.date} ${tx.time}".trim(), colDate + 5, startY + 20, textPaint)
            canvas.drawText(TransactionType.fromLabel(tx.type).displayLabel, colType + 5, startY + 20, textPaint)
            canvas.drawText("${tx.weight}g", colWeight + 5, startY + 20, textPaint)
            canvas.drawText("${tx.touch}%", colTouch + 5, startY + 20, textPaint)
            canvas.drawText("${tx.pureWeight}g", colPure + 5, startY + 20, textPaint)
            
            val remarks = if (tx.remarks.length > 25) tx.remarks.take(22) + "..." else tx.remarks
            canvas.drawText(remarks, colRemarks + 5, startY + 20, textPaint)

            if (hasImage) {
                try {
                    val uriStr = imageUris[0]
                    val bitmap = if (uriStr.startsWith("data:image/")) {
                        val base64Data = uriStr.substringAfter("base64,")
                        val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } else null

                    bitmap?.let {
                        val scaled = Bitmap.createScaledBitmap(it, 180, 180, true)
                        canvas.drawBitmap(scaled, colRemarks + 5, startY + 30, paint)
                    }
                } catch (e: Exception) {}
            }

            // Draw Row Borders
            canvas.drawRect(50f, startY, 550f, endY, borderPaint)
            // Vertical separators
            canvas.drawLine(colType, startY, colType, endY, borderPaint)
            canvas.drawLine(colWeight, startY, colWeight, endY, borderPaint)
            canvas.drawLine(colTouch, startY, colTouch, endY, borderPaint)
            canvas.drawLine(colPure, startY, colPure, endY, borderPaint)
            canvas.drawLine(colRemarks, startY, colRemarks, endY, borderPaint)

            y = endY
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
