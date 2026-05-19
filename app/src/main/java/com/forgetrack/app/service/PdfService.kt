package com.forgetrack.app.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.graphics.Typeface
import android.os.Environment
import com.forgetrack.app.data.model.Job
import com.forgetrack.app.util.Double.Companion.toCurrency
import com.forgetrack.app.util.Long.Companion.formatDuration
import com.forgetrack.app.util.toDateString
import java.io.File
import java.io.FileOutputStream

object PdfService {

    fun generateJobReport(context: Context, job: Job): String {
        val pageWidth = 595
        val pageHeight = 842
        val document = PdfDocument()

        val pageInfo = PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 24f
            color = Color.WHITE
        }
        val headerPaint = Paint().apply {
            color = Color.parseColor("#6C5CE7")
        }
        val textPaint = Paint().apply {
            textSize = 12f
            color = Color.parseColor("#333333")
        }
        val labelPaint = Paint().apply {
            textSize = 10f
            color = Color.parseColor("#666666")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val valuePaint = Paint().apply {
            textSize = 14f
            color = Color.parseColor("#1e293b")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val smallPaint = Paint().apply {
            textSize = 10f
            color = Color.parseColor("#94a3b8")
        }
        val successPaint = Paint().apply {
            textSize = 16f
            color = Color.parseColor("#00b894")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Header gradient background
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 120f, headerPaint)
        canvas.drawText("ForgeTrack Job Report", 40f, 55f, titlePaint)
        canvas.drawText("${job.title} - ${job.scheduledDate.toDateString()}", 40f, 85f, Paint().apply {
            textSize = 12f; color = Color.parseColor("#E0D0FF")
        })

        var y = 150f

        // Job Details Section
        canvas.drawText("JOB DETAILS", 40f, y, labelPaint); y += 20f
        canvas.drawLine(40f, y, pageWidth - 40f, y, Paint().apply { color = Color.parseColor("#6C5CE7"); strokeWidth = 2f }); y += 15f

        drawDetailRow(canvas, "Job Title", job.title, 40f, y, labelPaint, valuePaint); y += 30f
        drawDetailRow(canvas, "Status", job.status.name, 40f, y, labelPaint, valuePaint); y += 30f
        drawDetailRow(canvas, "Client", job.clientName, 40f, y, labelPaint, valuePaint); y += 30f
        drawDetailRow(canvas, "Priority", job.priority.name, 40f, y, labelPaint, valuePaint); y += 30f
        drawDetailRow(canvas, "Date", job.scheduledDate.toDateString(), 40f, y, labelPaint, valuePaint); y += 30f
        drawDetailRow(canvas, "Location", job.address.ifBlank { "Not specified" }, 40f, y, labelPaint, valuePaint); y += 30f

        if (job.totalDuration > 0) {
            drawDetailRow(canvas, "Duration", job.totalDuration.formatDuration(), 40f, y, labelPaint, valuePaint); y += 30f
        }

        y += 10f

        // Financial Summary
        canvas.drawText("FINANCIAL SUMMARY", 40f, y, labelPaint); y += 20f
        canvas.drawLine(40f, y, pageWidth - 40f, y, Paint().apply { color = Color.parseColor("#6C5CE7"); strokeWidth = 2f }); y += 20f
        canvas.drawText("Revenue: ${job.revenue.toCurrency()}", 60f, y, successPaint); y += 25f
        canvas.drawText("Cost: ${job.cost.toCurrency()}", 60f, y, textPaint); y += 25f
        val profit = job.revenue - job.cost
        val profitColor = if (profit >= 0) "#00b894" else "#ff6b6b"
        canvas.drawText("Profit: ${profit.toCurrency()}", 60f, y, Paint().apply {
            textSize = 14f; color = Color.parseColor(profitColor)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }); y += 25f
        canvas.drawText("Margin: ${String.format("%.1f%%", job.profitMargin)}", 60f, y, smallPaint); y += 35f

        // Description
        if (job.description.isNotBlank()) {
            canvas.drawText("DESCRIPTION", 40f, y, labelPaint); y += 20f
            canvas.drawLine(40f, y, pageWidth - 40f, y, Paint().apply { color = Color.parseColor("#6C5CE7"); strokeWidth = 2f }); y += 20f
            val descLines = job.description.chunked(80)
            descLines.forEach { line ->
                canvas.drawText(line, 50f, y, textPaint); y += 18f
            }
            y += 15f
        }

        // Notes
        if (job.notes.isNotBlank()) {
            canvas.drawText("NOTES", 40f, y, labelPaint); y += 20f
            canvas.drawLine(40f, y, pageWidth - 40f, y, Paint().apply { color = Color.parseColor("#6C5CE7"); strokeWidth = 2f }); y += 20f
            job.notes.split("|||").forEach { note ->
                canvas.drawText("• $note", 50f, y, textPaint); y += 18f
            }
            y += 15f
        }

        // Photos
        if (job.signaturePath.isNotBlank()) {
            canvas.drawText("SIGNATURE", 40f, y, labelPaint); y += 20f
            canvas.drawLine(40f, y, pageWidth - 40f, y, Paint().apply { color = Color.parseColor("#6C5CE7"); strokeWidth = 2f }); y += 15f
            canvas.drawText("✓ Client signature captured", 50f, y, Paint().apply {
                textSize = 12f; color = Color.parseColor("#00b894")
            }); y += 30f
        }

        // Footer
        canvas.drawLine(40f, pageHeight - 60f, pageWidth - 40f, pageHeight - 60f, Paint().apply { color = Color.parseColor("#e2e8f0") })
        canvas.drawText("Generated by ForgeTrack - Professional Field Service Tracker", 40f, pageHeight - 40f, smallPaint)
        canvas.drawText(java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()), 40f, pageHeight - 25f, smallPaint)

        document.finishPage(page)

        // Save to file
        val dir = File(context.getExternalFilesDir(null), "PDFs")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "ForgeTrack_${job.title.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()

        return file.absolutePath
    }

    private fun drawDetailRow(canvas: Canvas, label: String, value: String, x: Float, y: Float, labelPaint: Paint, valuePaint: Paint) {
        canvas.drawText(label, x, y, labelPaint)
        canvas.drawText(value, x + 120f, y, valuePaint)
    }
}
