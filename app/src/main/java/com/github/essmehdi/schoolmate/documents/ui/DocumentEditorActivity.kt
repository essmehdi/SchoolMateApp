package com.github.essmehdi.schoolmate.documents.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.ActivityDocumentEditorBinding
import com.github.essmehdi.schoolmate.documents.api.dto.DocumentDetailsDto
import com.github.essmehdi.schoolmate.documents.models.Document
import com.github.essmehdi.schoolmate.documents.viewmodels.DocumentEditorViewModel
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.google.android.material.chip.Chip
import okhttp3.MediaType

class DocumentEditorActivity : AppCompatActivity() {

  private lateinit var binding: ActivityDocumentEditorBinding
  private lateinit var viewModel: DocumentEditorViewModel
  private lateinit var filePickerLauncher: ActivityResultLauncher<String>
  private lateinit var scannerLauncher: ActivityResultLauncher<Intent>

  @Suppress("DEPRECATION")
  @SuppressLint("Range")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDocumentEditorBinding.inflate(layoutInflater)
    setContentView(binding.root)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    viewModel = ViewModelProvider(this)[DocumentEditorViewModel::class.java]

    if (intent.hasExtra("document")) {
      val doc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getSerializableExtra("document", Document::class.java)
      } else {
        intent.getSerializableExtra("document")
      } as Document
      enableEditMode(doc)
    }

    filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
      viewModel.selectedFile.value = uri
    }

    scannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      Log.d("Scanner activity OK", (result.resultCode == RESULT_OK).toString())
      if (result.resultCode == RESULT_OK) {
        val uri = result.data?.extras?.getParcelable("pdf") as Uri?
        Log.d("Document scanner", uri.toString())
        if (uri != null) {
          viewModel.selectedFile.value = uri
        } else {
          Toast.makeText(this, R.string.error_document_scanner_pdf_result, Toast.LENGTH_SHORT).show()
        }
      }
    }

    viewModel.fetchDocumentTags()

    binding.documentTagsChooserButton.setOnClickListener { showTagsPopup() }
    binding.documentFileChooserButton.homeButtonRoot.setOnClickListener { openFile() }
    binding.documentScannerButton.homeButtonRoot.setOnClickListener { openScanner() }
    binding.documentUploadFormSendButton.setOnClickListener { sendForm() }

    viewModel.documentTags.observe(this) {
      // Disable the tags chooser if documentTags are not fetched
      binding.documentTagsChooserButton.isEnabled = it is BaseResponse.Success

      // On edit mode, fill the chip group with tags when tags are fetched
      if (it is BaseResponse.Success && viewModel.editMode.value == true)
        fillChipsGroup(viewModel.selectedTags.value!!)
    }

    viewModel.selectedTags.observe(this) { selectedTags ->
      // Fill chip group when selected tags are changed
      if (viewModel.documentTags.value is BaseResponse.Success)
        fillChipsGroup(selectedTags)
    }

    viewModel.selectedFile.observe(this) { uri ->
      uri?.let {
        binding.documentChosenFileText.visibility = View.VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          binding.documentChosenFileText.text = Html.fromHtml(getString(R.string.label_chosen_file, viewModel.getFileName(contentResolver)), Html.FROM_HTML_MODE_COMPACT)
        } else {
          binding.documentChosenFileText.text = Html.fromHtml(getString(R.string.label_chosen_file, viewModel.getFileName(contentResolver)))
        }
      }
    }

    viewModel.uploadStatus.observe(this) {
      when (it) {
        null -> {
          restoreButton()
        }
        is BaseResponse.Loading -> {
          buttonLoading()
        }
        is BaseResponse.Success -> {
          setResult(RESULT_OK)
          finish()
        }
        is BaseResponse.Error -> {
          handleError(it.code!!)
        }
      }
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressedDispatcher.onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  private fun fillChipsGroup(selectedTags: Set<Long>) {
    val chips = selectedTags.map { selectedTagId -> Chip(this).apply {
      text = viewModel.documentTags.value?.data?.find { tag -> tag.id == selectedTagId }?.name ?: ""
    } }
    binding.documentTagsChipGroup.removeAllViews()
    chips.forEach {
      binding.documentTagsChipGroup.addView(it)
    }
  }

  private fun openScanner() {
    val intent = Intent(this, DocumentScannerActivity::class.java)
    scannerLauncher.launch(intent)
  }

  private fun enableEditMode(document: Document) {
    viewModel.editMode.value = true
    viewModel.editId.value = document.id

    binding.apply {
      documentNameEdittext.setText(document.name)
      documentSharedCheckbox.isChecked = document.shared
      viewModel.selectedTags.value = document.tags.map { it.id }.toSet()
      documentFileChooserButton.homeButtonRoot.isVisible = false
      documentScannerButton.homeButtonRoot.isVisible = false
      documentUploadFormSendButton.text = getString(R.string.action_send_edit_form)
    }
  }

  private fun showTagsPopup() {
    val tags = viewModel.documentTags.value?.data ?: listOf()
    val choices = tags.map { it.name }
    val checkedChoices = tags.map { viewModel.selectedTags.value!!.contains(it.id) }
    AlertDialog.Builder(this).apply {
      setTitle(R.string.action_select_tags)
      setCancelable(true)
      setMultiChoiceItems(
        choices.toTypedArray(),
        checkedChoices.toTypedArray().toBooleanArray()
      ) { _, index, selected ->
        if (selected) {
          viewModel.selectTag(tags[index].id)
        } else {
          viewModel.removeTag(tags[index].id)
        }
      }
      setNeutralButton(R.string.label_tags_popup_close) { dialogInterface, _ ->
        dialogInterface.dismiss()
      }
      create().show()
    }
  }

  private fun openFile() {
    filePickerLauncher.launch("application/pdf")
  }

  private fun sendForm() {
    val name = binding.documentNameEdittext.text?.toString()
    if (name.isNullOrBlank()) {
      binding.documentNameEdittextLayout.error = getString(R.string.error_document_name_empty_edittext)
      return
    } else {
      binding.documentNameEdittextLayout.error = null
    }
    val shared = binding.documentSharedCheckbox.isChecked
    val tags = viewModel.selectedTags.value?.toList() ?: listOf()

    val documentDetailsDto = DocumentDetailsDto(name, shared, tags)
    if (viewModel.editMode.value == true) {
      viewModel.editDocument(documentDetailsDto)
    } else {
      if (viewModel.selectedFile.value == null) {
        Toast.makeText(this, R.string.error_document_file_empty, Toast.LENGTH_LONG).show()
        return
      }
      val file = viewModel.getFileContent(contentResolver)
      val filename = viewModel.getFileName(contentResolver)
      val fileType = viewModel.getFileMediaType(contentResolver)
      viewModel.uploadDocument(documentDetailsDto, filename, MediaType.get(fileType), file)
    }
  }

  private fun buttonLoading() {
    binding.documentUploadFormSendButton.apply {
      text = getString(
        if (viewModel.editMode.value == true) R.string.label_editing_form else R.string.label_uploading_form)
      isActivated = false
    }
  }

  private fun restoreButton() {
    binding.documentUploadFormSendButton.apply {
      text = if (viewModel.editMode.value == true) getString(R.string.action_send_edit_form) else getString(R.string.action_send_upload_form)
      isActivated = true
    }
  }

  private fun handleError(code: Int) {
    if (code == 400) {
      Toast.makeText(this, R.string.error_wrong_field_value, Toast.LENGTH_LONG).show()
    } else {
      Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
    }
    restoreButton()
  }
}