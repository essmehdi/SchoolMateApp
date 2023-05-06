package com.github.essmehdi.schoolmate.documents.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.DialogNewTagBinding
import com.github.essmehdi.schoolmate.databinding.FragmentDocumentTagsBinding
import com.github.essmehdi.schoolmate.documents.adapters.DocumentTagsListAdapter
import com.github.essmehdi.schoolmate.documents.models.DocumentTag
import com.github.essmehdi.schoolmate.documents.viewmodels.DocumentTagsViewModel
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class DocumentTagsFragment : BottomSheetDialogFragment() {

  private lateinit var binding: FragmentDocumentTagsBinding
  private lateinit var tagsListAdapter: DocumentTagsListAdapter
  private lateinit var viewModel: DocumentTagsViewModel

  companion object {
    @JvmStatic
    fun newInstance() = DocumentTagsFragment()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentDocumentTagsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel = ViewModelProvider(this)[DocumentTagsViewModel::class.java]

    viewModel.loadDocumentTags()

    tagsListAdapter = DocumentTagsListAdapter(listOf(), viewModel)
    tagsListAdapter.setOnEditButtonTagClickListener(object : DocumentTagsListAdapter.OnEditButtonTagClickListener {
      override fun onEditButtonTagClick(tag: DocumentTag) {
        showTagEditorDialog(tag)
      }
    })

    binding.documentTagsList.apply {
      adapter = tagsListAdapter
      layoutManager = LinearLayoutManager(this@DocumentTagsFragment.context, LinearLayoutManager.VERTICAL, false)
    }

    binding.documentTagsAddButton.setOnClickListener { showTagEditorDialog() }

    viewModel.documentTags.observe(viewLifecycleOwner) {
      when (it) {
        is BaseResponse.Loading -> {
          tagsListAdapter.updateData(listOf())
          showLoading(true)
        }
        is BaseResponse.Success -> {
          tagsListAdapter.updateData(it.data!!)
          showLoading(false)
        }
        is BaseResponse.Error -> {
          handleDocumentError(it.code!!)
        }
      }
    }

    viewModel.deleteStatus.observe(this) {
      when (it) {
        is BaseResponse.Loading -> {
          showToast(R.string.loading_document_tag_deletion, Snackbar.LENGTH_INDEFINITE)
        }
        is BaseResponse.Success -> {
          showToast(R.string.success_document_tag_deletion, Snackbar.LENGTH_SHORT)
        }
        is BaseResponse.Error -> {
          handleTagError(it.code!!)
        }
      }
    }

    viewModel.addStatus.observe(this) {
      when (it) {
        is BaseResponse.Loading -> {
          showToast(R.string.loading_document_tag_addition, Snackbar.LENGTH_INDEFINITE)
        }
        is BaseResponse.Success -> {
          showToast(R.string.success_document_tag_addition, Snackbar.LENGTH_SHORT)
        }
        is BaseResponse.Error -> {
          showToast(R.string.error_document_tag_addition, Snackbar.LENGTH_SHORT)
        }
      }
    }
  }

  private fun showTagEditorDialog(tag: DocumentTag? = null) {
    val editorBinding = DialogNewTagBinding.inflate(layoutInflater)

    // Set the tag name if we are editing
    if (tag != null)
      editorBinding.dialogNewTagEdittext.setText(tag.name)

    // Show alert dialog
    AlertDialog.Builder(this.requireContext()).apply {
      setTitle(if (tag == null) R.string.label_document_tags_add_dialog_title else R.string.label_document_tags_edit_dialog_title)
      setView(editorBinding.root)
      setPositiveButton(if (tag == null) R.string.label_document_tags_add_dialog_confirm_action else R.string.label_document_tags_edit_dialog_confirm_action) { dialog, _ ->
        val name = editorBinding.dialogNewTagEdittext.text.toString()
        // Check if the name was provided
        if (name.isBlank()) {
          editorBinding.dialogNewTagEdittextLayout.error = getString(R.string.error_document_tag_editor_empty_name)
        } else {
          // Add tag or edit tag
          if (tag == null)
            viewModel.addTag(name)
          else
            viewModel.editTag(tag.id, name)
        }
        dialog.dismiss()
      }
      create().show()
    }
  }

  private fun showToast(@StringRes messageResId: Int, length: Int) {
    Snackbar.make(binding.root, messageResId, length).show()
  }

  private fun showLoading(show: Boolean) {
    if (show) {
      binding.documentTagsLoading.loadingProgressBar.isVisible = true
      binding.documentTagsLoading.loadingErrorMessage.isVisible = false
    }
    binding.documentTagsLoading.loadingOverlay.isVisible = show
  }

  @Suppress("UNUSED_PARAMETER")
  private fun handleDocumentError(code: Int) {
    showLoading(true)
    binding.documentTagsLoading.loadingProgressBar.isVisible = false
    binding.documentTagsLoading.loadingErrorMessage.apply {
      isVisible = true
      text = getString(R.string.unknown_error_occurred)
    }
  }

  private fun handleTagError(code: Int) {
    if (code == 409) {
      showToast(R.string.error_document_tag_deletion_conflict, Snackbar.LENGTH_LONG)
    } else {
      showToast(R.string.error_document_tag_deletion_unknown, Snackbar.LENGTH_LONG)
    }
  }
}