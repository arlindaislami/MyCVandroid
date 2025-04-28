package com.example.mycvandroid

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.view.View
import java.io.File
import java.io.FileOutputStream

fun exportScreenAsPDF(context: Context, view: View, onFinished: () -> Unit) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
    val page = document.startPage(pageInfo)

    val bitmap = captureViewAsBitmap(view)
    val canvas = page.canvas
    canvas.drawBitmap(bitmap, 0f, 0f, null)

    document.finishPage(page)

    val filePath = File(context.getExternalFilesDir(null), "CV_preview.pdf")
    try {
        val outputStream = FileOutputStream(filePath)
        document.writeTo(outputStream)
        outputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        document.close()
        onFinished()
    }
}
