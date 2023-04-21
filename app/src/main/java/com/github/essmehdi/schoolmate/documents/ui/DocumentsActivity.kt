package com.github.essmehdi.schoolmate.documents.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.ActivityDocumentsBinding
import com.github.essmehdi.schoolmate.documents.adapters.DocumentsListAdapter
import com.github.essmehdi.schoolmate.documents.adapters.OnEditMenuItemClickedListener
import com.github.essmehdi.schoolmate.documents.models.Document
import com.github.essmehdi.schoolmate.documents.viewmodels.DocumentsViewModel
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class DocumentsActivity : AppCompatActivity() {

  private lateinit var binding: ActivityDocumentsBinding
  private lateinit var viewModel: DocumentsViewModel
  private lateinit var launcher: ActivityResultLauncher<Intent>
  private lateinit var documentsAdapter: DocumentsListAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDocumentsBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.documentsToolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_navigation_left)

    launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == RESULT_OK) {
        viewModel.refresh()
      }
    }

    viewModel = ViewModelProvider(this)[DocumentsViewModel::class.java]
    viewModel.loadDocumentTags()
    viewModel.loadDocuments()
    viewModel.trackEmpty()

    // Initialize recycler view adapter
    documentsAdapter = DocumentsListAdapter(listOf(), viewModel)
    documentsAdapter.onEditMenuItemClickedListener = object : OnEditMenuItemClickedListener {
      override fun onEditMenuItemClickedListener(document: Document) {
        goToDocumentEditor(document)
      }
    }
    binding.documentsMain.documentsList.apply {
      adapter = documentsAdapter
      layoutManager = LinearLayoutManager(this@DocumentsActivity, LinearLayoutManager.VERTICAL, false)
      addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          super.onScrolled(recyclerView, dx, dy)
          if (dy > 0) {
            if (viewModel.currentPageStatus.value is BaseResponse.Loading) {
              return
            }
            val visibleItemCount = layoutManager?.childCount ?: 0
            val totalItemCount = layoutManager?.itemCount ?: 0
            val pastVisibleItems = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (visibleItemCount + pastVisibleItems >= totalItemCount) {
              viewModel.loadDocuments()
            }
          }
        }
      })
    }

    viewModel.showEmpty.observe(this) {
      showEmpty(it)
    }

    // Update adapter on data change
    viewModel.documents.observe(this) { documents ->
      documents?.let { documentsAdapter.updateData(it) }
    }

    viewModel.currentPageStatus.observe(this) { currentPage ->
      when (currentPage) {
        is BaseResponse.Loading -> {
          if (viewModel.documents.value == null || viewModel.documents.value!!.isEmpty()) {
            showLoading()
          } else {
            showLoading(false)
          }
        }
        is BaseResponse.Success -> {
          showLoading(false)
        }
        is BaseResponse.Error -> {
          handleError(currentPage.code!!)
        }
        null -> {}
      }
    }

    viewModel.deleteStatus.observe(this) {
      when (it) {
        is BaseResponse.Success -> {
          Snackbar.make(binding.root, R.string.success_document_deletion, Snackbar.LENGTH_SHORT).show()
          viewModel.refresh()
        }
        is BaseResponse.Loading -> {
          Snackbar.make(binding.root, R.string.loading_document_deletion, Snackbar.LENGTH_INDEFINITE).show()
        }
        is BaseResponse.Error -> {
          Snackbar.make(binding.root, R.string.error_document_deletion, Snackbar.LENGTH_SHORT).show()
        }
        else -> {}
      }
    }

    viewModel.filterTags.observe(this) {
      binding.documentsMain.documentsTagsFilterGroup.isVisible = it.isNotEmpty()
      binding.documentsMain.documentsTagsFilterGroup.apply {
        removeAllViews()
        it.forEach {
          addView(Chip(this@DocumentsActivity).apply {
            text = it.name
          })
        }
      }
    }

    binding.documentAddButton.setOnClickListener { goToDocumentEditor() }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val inflater = menuInflater
    inflater.inflate(R.menu.documents_list_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.documents_menu_tags -> {
        val fragment = DocumentTagsFragment.newInstance()
        fragment.show(supportFragmentManager, fragment.tag)
        true
      }
      R.id.documents_menu_filter -> {
        showFilterTags()
        true
      }
      R.id.documents_menu_sort_name -> {
        item.isChecked = !item.isChecked
        viewModel.changeSortField("name")
        true
      }
      R.id.documents_menu_sort_upload -> {
        item.isChecked = !item.isChecked
        viewModel.changeSortField("uploadedAt")
        true
      }
      R.id.documents_menu_sort_shared -> {
        item.isChecked = !item.isChecked
        viewModel.changeSortField("shared")
        true
      }
      R.id.documents_menu_sort_asc -> {
        item.isChecked = !item.isChecked
        viewModel.changeOrder("asc")
        true
      }
      R.id.documents_menu_sort_desc -> {
        item.isChecked = !item.isChecked
        viewModel.changeOrder("desc")
        true
      }
      else -> super.onContextItemSelected(item)
    }
  }

  private fun showFilterTags() {
    val tags = viewModel.documentTags.value?.data ?: listOf()
    val choices = tags.map { it.name }
    val checkedChoices = tags.map { viewModel.filterTags.value!!.contains(it) }
    AlertDialog.Builder(this).apply {
      setTitle(R.string.action_select_tags)
      setCancelable(true)
      setMultiChoiceItems(
        choices.toTypedArray(),
        checkedChoices.toTypedArray().toBooleanArray()
      ) { _, index, selected ->
        if (selected) {
          viewModel.addFilterTag(tags[index])
        } else {
          viewModel.removeFilterTag(tags[index])
        }
      }
      setNeutralButton(R.string.label_tags_popup_close) { dialogInterface, _ ->
        dialogInterface.dismiss()
      }
      setOnDismissListener {
        if (viewModel.oldFilterTags.value != viewModel.filterTags.value)
          viewModel.refresh()
        viewModel.oldFilterTags.value = viewModel.filterTags.value
      }
      create().show()
    }
  }

  private fun showEmpty(show: Boolean = true) {
    binding.documentsMain.documentsEmpty.root.isVisible = show
  }

  private fun showLoading(show: Boolean = true) {
    binding.documentsMain.documentsLoading.loadingOverlay.isVisible = show
  }

  private fun handleError(code: Int) {
    binding.documentsMain.documentsLoading.loadingErrorMessage.text = getString(R.string.unknown_error_occurred)
    binding.documentsMain.documentsLoading.loadingErrorMessage.visibility = View.VISIBLE
    binding.documentsMain.documentsLoading.loadingProgressBar.visibility = View.GONE
    showLoading()
  }

  private fun goToDocumentEditor(document: Document? = null) {
    val intent = Intent(this, DocumentEditorActivity::class.java)
    if (document != null) intent.putExtra("document", document)
    launcher.launch(intent)
  }
}