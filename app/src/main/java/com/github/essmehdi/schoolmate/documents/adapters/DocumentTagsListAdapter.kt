package com.github.essmehdi.schoolmate.documents.adapters

import android.annotation.SuppressLint
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.DocumentTagItemBinding
import com.github.essmehdi.schoolmate.documents.models.DocumentTag
import com.github.essmehdi.schoolmate.documents.viewmodels.DocumentTagsViewModel
import com.github.essmehdi.schoolmate.shared.utils.Utils
import org.joda.time.format.DateTimeFormat

class DocumentTagsListAdapter(var data: List<DocumentTag>?, val viewModel: DocumentTagsViewModel): RecyclerView.Adapter<DocumentTagsListAdapter.DocumentTagItemViewHolder>() {

  private lateinit var onEditButtonTagClickListener: OnEditButtonTagClickListener

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentTagItemViewHolder {
    val binding =
      DocumentTagItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return DocumentTagItemViewHolder(binding)
  }

  override fun getItemCount(): Int {
    return data?.size ?: 0
  }

  override fun onBindViewHolder(holder: DocumentTagItemViewHolder, position: Int) {
    data?.let { holder.bind(it[position]) }
  }

  @SuppressLint("NotifyDataSetChanged")
  fun updateData(newData: List<DocumentTag>) {
    this.data = newData
    notifyDataSetChanged()
  }

  fun setOnEditButtonTagClickListener(onEditButtonTagClickListener: OnEditButtonTagClickListener) {
    this.onEditButtonTagClickListener = onEditButtonTagClickListener
  }

  interface OnEditButtonTagClickListener {
    fun onEditButtonTagClick(tag: DocumentTag)
  }

  inner class DocumentTagItemViewHolder(private val binding: DocumentTagItemBinding): RecyclerView.ViewHolder(binding.root) {
    
    fun bind(tag: DocumentTag) {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
      val dateTime = formatter.parseDateTime(tag.createdAt)

      binding.documentTagItemTitleText.text = tag.name
      binding.documentTagItemDateText.text = Utils.calculatePastTime(dateTime.toDate(), binding.root.context) ?: ""

      binding.documentTagItemRemoveButton.setOnClickListener { viewModel.deleteTag(tag.id) }
      binding.documentTagItemEditButton.setOnClickListener { onEditButtonTagClickListener.onEditButtonTagClick(tag) }
    }
  }
}