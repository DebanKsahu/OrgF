package com.github.orgf.core.agent.tool

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min

class PdfTextExtractor(
    private val platformContext: Context
) {

    init {
        PDFBoxResourceLoader.init(platformContext)
    }

    suspend fun extractSmallText(pdfFile: File): String = withContext(Dispatchers.IO) {
        try {
            val selectedPages = if (PDDocument.load(pdfFile).numberOfPages<=10) {
                null
            } else {
                (1..11).toList()
            }
            val parsedText = extractDigitalText(pdfFile = pdfFile, selectedPages = selectedPages)
            if (parsedText.length >= 200) {
                return@withContext parsedText
            } else {
                return@withContext extractScannedText(pdfFile = pdfFile, selectedPages = selectedPages)
            }
        } catch (e: Exception) {
            return@withContext ""
        }
    }

    suspend fun extractText(pdfFile: File): String = withContext(Dispatchers.IO) {
        try {
            val parsedText = extractDigitalText(pdfFile = pdfFile, selectedPages = null)
            if (parsedText.length >= 200) {
                return@withContext parsedText
            } else {
                return@withContext extractScannedText(pdfFile = pdfFile, selectedPages = null)
            }
        } catch (e: Exception) {
            return@withContext ""
        }
    }

    private fun extractDigitalText(pdfFile: File, selectedPages: List<Int>?): String {
        return try {
            val pdfDocument = PDDocument.load(pdfFile)
            val textStripper = PDFTextStripper()

            val totalPages = pdfDocument.numberOfPages
            val extractedText = StringBuilder()


            val pagesToProcess = if (selectedPages == null || selectedPages?.size==0) {
                (0 until min(15,totalPages)).toList()
            } else {
                selectedPages
            }

            for (pageIndex in pagesToProcess) {
                if (pageIndex<0 || pageIndex>=totalPages) continue
                textStripper.startPage = pageIndex+1
                textStripper.endPage = pageIndex+1

                val pageText = textStripper.getText(pdfDocument)
                extractedText.append(pageText).append("\n\n")
            }

            pdfDocument.close()
            extractedText.toString().trim()
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun extractScannedText(pdfFile: File, selectedPages: List<Int>?): String {
        return try {
            val scannerClient = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val extractedText = StringBuilder()

            val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)
            val totalPages = pdfRenderer.pageCount

            val pagesToProcess = if (selectedPages == null || selectedPages?.size==0) {
                (0 until min(totalPages,15)).toList()
            } else {
                selectedPages
            }

            for (pageIndex in pagesToProcess) {
                if (pageIndex<0 || pageIndex>= totalPages) continue

                val currPage = pdfRenderer.openPage(pageIndex)
                val bitMap = createBitmap(currPage.width, currPage.height)
                currPage.render(bitMap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                val image = InputImage.fromBitmap(bitMap, 0)

                val result = scannerClient.process(image).await()
                extractedText.append(result?.text).append("\n\n")

                bitMap.recycle()
                currPage.close()
            }

            pdfRenderer.close()
            fileDescriptor.close()

            extractedText.toString().trim()
        } catch (e: Exception) {
            ""
        }
    }
}