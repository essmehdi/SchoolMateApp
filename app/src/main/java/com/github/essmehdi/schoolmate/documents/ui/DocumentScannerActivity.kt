package com.github.essmehdi.schoolmate.documents.ui

import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.github.essmehdi.schoolmate.databinding.ActivityDocumentScannerBinding
import com.github.essmehdi.schoolmate.documents.adapters.ScannedPagesAdapter
import com.github.essmehdi.schoolmate.documents.viewmodels.DocumentScannerViewModel
import com.websitebeaver.documentscanner.DocumentScanner
import java.io.File

class DocumentScannerActivity : AppCompatActivity() {

  private lateinit var binding: ActivityDocumentScannerBinding
  private lateinit var launcher: ActivityResultLauncher<Intent>
  private lateinit var viewModel: DocumentScannerViewModel
  private lateinit var scannedPagesAdapter: ScannedPagesAdapter
  private val documentScanner = DocumentScanner(
    this,
    {
      it.forEach { image -> viewModel.addScannedPage(Uri.parse(image)) }
    },
    {
      Log.e("Document scanner", it)
    },
    {
      Log.i("Document scanner", "Canceled")
    }
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDocumentScannerBinding.inflate(layoutInflater)
    setContentView(binding.root)

    viewModel = ViewModelProvider(this)[DocumentScannerViewModel::class.java]

    launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      documentScanner.handleDocumentScanIntentResult(result)
    }

    val documentScannerIntent = documentScanner.createDocumentScanIntent()
    binding.documentScannerScanPageButton.setOnClickListener {
      launcher.launch(documentScannerIntent)
    }

    binding.documentScannerCancelButton.setOnClickListener {
      onBackPressedDispatcher.onBackPressed()
    }

    binding.documentScannerConfirmButton.setOnClickListener {
      val pdfUri = convertToPdf()
      Log.i("Document scanner", "PDF URI: $pdfUri")
      val intent = Intent().apply {
        putExtra("pdf", pdfUri)
      }
      setResult(RESULT_OK, intent)
      finish()
    }

    scannedPagesAdapter = ScannedPagesAdapter(listOf(), viewModel)
    binding.documentScannerPagesList.apply {
      adapter = scannedPagesAdapter
      layoutManager = GridLayoutManager(this@DocumentScannerActivity, 3)
    }

    viewModel.scannedPages.observe(this) {
      binding.documentScannerConfirmButton.isEnabled = it.isNotEmpty()
      scannedPagesAdapter.updateData(it)
    }
  }

  private fun convertToPdf(): Uri {
    val tempFile = File.createTempFile("temp", ".pdf", cacheDir)
    val document = PdfDocument()
    viewModel.scannedPages.value?.forEachIndexed { index, scannedPage ->
      val pageWidth = 595
      val pageHeight = 842

      // Create a page for the scanned image
      val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
      val page = document.startPage(pageInfo)

      // Get image content as bitmap
      val inputS = contentResolver.openInputStream(scannedPage)
      val bitmap = BitmapFactory.decodeStream(inputS)
      inputS?.close()

      // Calculate the new height to match the aspect ratio
      val newBitmapHeight = bitmap.height * pageWidth / bitmap.width

      // Scale the bitmap to fit the page
      val scaledBitmap = Bitmap.createScaledBitmap(bitmap, pageWidth, newBitmapHeight, true)

      // Calculate the top offset to center the image on the page
      val topOffset = ((pageHeight - scaledBitmap.height) / 2).toFloat()

      // Draw the bitmap on the page
      page.canvas.drawBitmap(scaledBitmap, 0f, topOffset, Paint())
      document.finishPage(page)
    }
    document.writeTo(tempFile.outputStream())
    document.close()
    return Uri.fromFile(tempFile)
  }
}