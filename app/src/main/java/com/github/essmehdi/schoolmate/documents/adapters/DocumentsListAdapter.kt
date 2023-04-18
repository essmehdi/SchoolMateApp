package com.github.essmehdi.schoolmate.documents.adapters

import android.annotation.SuppressLint
import android.view.*
import android.view.MenuItem.OnMenuItemClickListener
import android.view.View.OnCreateContextMenuListener
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.DocumentItemBinding
import com.github.essmehdi.schoolmate.documents.models.Document
import com.github.essmehdi.schoolmate.documents.viewmodels.DocumentsViewModel
import com.github.essmehdi.schoolmate.shared.utils.Utils
import org.joda.time.format.DateTimeFormat
import java.util.*

class DocumentsListAdapter(var data: List<Document>?, val viewModel: DocumentsViewModel): RecyclerView.Adapter<DocumentsListAdapter.DocumentItemViewHolder>() {

  lateinit var onEditMenuItemClickedListener: OnEditMenuItemClickedListener

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentItemViewHolder {
    val binding =
      DocumentItemBinding
        .bind(LayoutInflater.from(parent.context).inflate(R.layout.document_item, parent, false))
    return DocumentItemViewHolder(binding)
  }

  override fun getItemCount(): Int {
    return data?.size ?: 0
  }

  override fun onBindViewHolder(holder: DocumentItemViewHolder, position: Int) {
    data?.let { holder.bind(it[position]) }
  }

  @SuppressLint("NotifyDataSetChanged")
  fun updateData(newData: List<Document>) {
    this.data = newData
    notifyDataSetChanged()
  }

  inner class DocumentItemViewHolder(private val binding: DocumentItemBinding): RecyclerView.ViewHolder(binding.root), OnCreateContextMenuListener, OnMenuItemClickListener{

    init {
      binding.root.setOnCreateContextMenuListener(this)
    }

    fun bind(document: Document) {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
      val dateTime = formatter.parseDateTime(document.uploadedAt)

      binding.documentItemNameText.text = document.name
      binding.documentItemDateText.text = Utils.calculatePastTime(dateTime.toDate(), binding.root.context) ?: ""
      if (document.tags.isEmpty()) {
        binding.documentItemTagsText.isVisible = false
      } else {
        binding.documentItemTagsText.text = document.tags.joinToString(" • ") { it.name }
      }
    }

    override fun onCreateContextMenu(
      menu: ContextMenu?,
      v: View?,
      menuInfo: ContextMenu.ContextMenuInfo?
    ) {
      menu?.add(Menu.NONE, 1, 1, binding.root.context.getString(R.string.label_document_item_context_menu_download))?.setOnMenuItemClickListener(this)
      menu?.add(Menu.NONE, 2, 2, binding.root.context.getString(R.string.label_document_item_context_menu_edit))?.setOnMenuItemClickListener(this)
      menu?.add(Menu.NONE, 3, 3, binding.root.context.getString(R.string.label_document_item_context_menu_delete))?.setOnMenuItemClickListener(this)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
      val currentDocument = data!![adapterPosition]
      return when (item.order) {
        1 -> {
          viewModel.downloadDocument(currentDocument, binding.root.context)
          true
        }
        2 -> {
          onEditMenuItemClickedListener.onEditMenuItemClickedListener(currentDocument)
          true
        }
        3 -> {
          viewModel.deleteDocument(currentDocument)
          true
        }
        else -> false
      }
    }
  }
}

interface OnEditMenuItemClickedListener {
  fun onEditMenuItemClickedListener(document: Document)
}