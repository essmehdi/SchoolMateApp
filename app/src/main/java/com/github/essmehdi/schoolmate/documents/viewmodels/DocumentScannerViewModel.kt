package com.github.essmehdi.schoolmate.documents.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DocumentScannerViewModel: ViewModel() {
  val scannedPages: MutableLiveData<List<Uri>> = MutableLiveData()

  fun addScannedPage(uri: Uri) {
    scannedPages.value = scannedPages.value?.plus(uri) ?: listOf(uri)
  }

  fun removeScannedPage(index: Int) {
    val pages = scannedPages.value?.toMutableList() ?: return
    pages.removeAt(index)
    scannedPages.value = pages
  }
}