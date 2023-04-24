package com.github.essmehdi.schoolmate.documents.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.databinding.ScannedPageBinding
import com.github.essmehdi.schoolmate.documents.viewmodels.DocumentScannerViewModel

class ScannedPagesAdapter(var data: List<Uri>, val viewModel: DocumentScannerViewModel): RecyclerView.Adapter<ScannedPagesAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val binding = ScannedPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return ViewHolder(binding)
  }

  override fun getItemCount(): Int {
    return data.size
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(data[position])
  }

  fun updateData(newData: List<Uri>) {
    this.data = newData
  }

  inner class ViewHolder(val binding: ScannedPageBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(uri: Uri) {
      binding.scannedPageImage.setImageURI(uri)
      binding.scannedPageDeleteButton.setOnClickListener {
        viewModel.removeScannedPage(adapterPosition)
      }
    }
  }
}