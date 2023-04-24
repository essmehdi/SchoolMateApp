package com.github.essmehdi.schoolmate.documents.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.github.essmehdi.schoolmate.databinding.ActivityDocumentScannerBinding
import com.github.essmehdi.schoolmate.documents.viewmodels.DocumentScannerViewModel
import com.websitebeaver.documentscanner.DocumentScanner
import java.io.File

class DocumentScannerActivity : AppCompatActivity() {

  private lateinit var binding: ActivityDocumentScannerBinding
  private lateinit var launcher: ActivityResultLauncher<Intent>
  private val viewModel: DocumentScannerViewModel by viewModels()
  private val documentScanner = DocumentScanner(
    this,
    { viewModel.addScannedPages(*it.map { uriString -> Uri.parse(uriString) }.toTypedArray()) }, // Success handler
    { Log.e("Document scanner", it) }, // Error handler
    { Log.i("Document scanner", "Canceled") } // Canceled handler
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDocumentScannerBinding.inflate(layoutInflater)
    setContentView(binding.root)

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
      val intent = Intent().apply {
        putExtra("pdf", pdfUri)
      }
      setResult(RESULT_OK, intent)
      finish()
    }

    // Initialize the recycler view
    binding.documentScannerPagesList.apply {
      adapter = viewModel.adapter.value
      layoutManager = GridLayoutManager(this@DocumentScannerActivity, 2)
    }

    // Create an item touch helper to drag pages
    val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(
      object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
        androidx.recyclerview.widget.ItemTouchHelper.UP or
                androidx.recyclerview.widget.ItemTouchHelper.DOWN or
                androidx.recyclerview.widget.ItemTouchHelper.START or
                androidx.recyclerview.widget.ItemTouchHelper.END,
        0
      ) {
        override fun onMove(
          recyclerView: androidx.recyclerview.widget.RecyclerView,
          viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
          target: androidx.recyclerview.widget.RecyclerView.ViewHolder
        ): Boolean {
          val from = viewHolder.adapterPosition
          val to = target.adapterPosition
          viewModel.moveScannedPage(from, to)
          return true
        }

        override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
          // Do nothing
        }
      }
    )
    itemTouchHelper.attachToRecyclerView(binding.documentScannerPagesList)

    viewModel.scannedPages.observe(this) {
      binding.documentScannerConfirmButton.isEnabled = it.isNotEmpty()
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