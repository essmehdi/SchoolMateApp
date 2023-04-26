package com.github.essmehdi.schoolmate.documents.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.documents.adapters.ScannedPagesAdapter

class DocumentScannerViewModel: ViewModel() {
  val scannedPages: MutableLiveData<List<Uri>> = MutableLiveData()
  val adapter: MutableLiveData<ScannedPagesAdapter> = MutableLiveData(ScannedPagesAdapter(emptyList(), this))

  fun addScannedPages(vararg uri: Uri) {
    scannedPages.value = scannedPages.value?.plus(uri) ?: uri.toList()
    adapter.value?.apply {
      updateData(scannedPages.value!!)
      notifyItemRangeInserted(itemCount - uri.size, uri.size)
    }
  }

  fun removeScannedPage(index: Int) {
    val pages = scannedPages.value?.toMutableList() ?: return
    pages.removeAt(index)
    scannedPages.value = pages
    adapter.value?.apply {
      updateData(scannedPages.value!!)
      notifyItemRemoved(index)
    }
  }

  fun moveScannedPage(from: Int, to: Int) {
    val pages = scannedPages.value?.toMutableList() ?: return
    val page = pages[from]
    pages.removeAt(from)
    pages.add(to, page)
    scannedPages.value = pages
    adapter.value?.apply {
      updateData(scannedPages.value!!)
      notifyItemMoved(from, to)
    }
  }
}