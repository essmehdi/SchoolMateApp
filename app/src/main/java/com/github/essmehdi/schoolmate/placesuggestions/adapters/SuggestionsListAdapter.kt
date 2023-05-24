package com.github.essmehdi.schoolmate.placesuggestions.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.databinding.SuggestionsItemBinding
import android.view.MenuItem.OnMenuItemClickListener
import android.view.View
import android.view.View.OnCreateContextMenuListener
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.placesuggestions.enumerations.SuggestionType
import com.github.essmehdi.schoolmate.placesuggestions.models.PlaceSuggestions
import com.github.essmehdi.schoolmate.placesuggestions.ui.SuggestionDetailsActivity
import com.github.essmehdi.schoolmate.placesuggestions.ui.SuggestionEditorActivity
import com.github.essmehdi.schoolmate.placesuggestions.ui.SuggestionsActivity
import com.github.essmehdi.schoolmate.placesuggestions.viewmodels.SuggestionsViewModel
import com.github.essmehdi.schoolmate.shared.utils.Utils
import com.github.essmehdi.schoolmate.users.models.User
import com.github.essmehdi.schoolmate.users.models.UserRole
import com.github.essmehdi.schoolmate.users.viewmodels.UserPlacesViewModel
import org.joda.time.format.DateTimeFormat

class SuggestionsListAdapter(var data: List<PlaceSuggestions>, val viewModel: ViewModel?= null, val launcher: ActivityResultLauncher<Intent>?=null): RecyclerView.Adapter<SuggestionsListAdapter.SuggestionItemViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionItemViewHolder {
        val binding = SuggestionsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SuggestionItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    override fun onBindViewHolder(holder: SuggestionsListAdapter.SuggestionItemViewHolder, position: Int) {
        data?.let { holder.bind(it[position]) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<PlaceSuggestions>) {
        this.data = newData
        notifyDataSetChanged()
    }

    inner class SuggestionItemViewHolder (private val binding: SuggestionsItemBinding): RecyclerView.ViewHolder(binding.root),OnCreateContextMenuListener, OnMenuItemClickListener {
        init {
            binding.root.setOnCreateContextMenuListener(this)
        }

        fun bind(suggestion: PlaceSuggestions) {
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
            val dateTime = formatter.parseDateTime(suggestion.date)

            binding.suggestionDateText.text =
                Utils.calculatePastTime(dateTime.toDate(), binding.root.context) ?: ""
            binding.suggesterText.text = suggestion.user.fullName
            binding.suggestionTypeText.text = suggestion.suggestiontype.name
            binding.suggestionDescriptionText.text = suggestion.description
            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, SuggestionDetailsActivity::class.java)
                intent.putExtra("id", suggestion.id)
                launcher?.launch(intent)
            }
            binding.suggestionItemImage.setImageResource(
                when(suggestion.suggestiontype) {
                    SuggestionType.StudyPlace -> R.drawable.ic_books
                    SuggestionType.FoodPlace -> R.drawable.ic_food
                    SuggestionType.Entertainment -> R.drawable.ic_popcorn
                    SuggestionType.Other -> R.drawable.ic_question
                }
            )
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            if (viewModel != null) {
                menu?.add(this.bindingAdapterPosition,1,1,binding.root.context.getString(R.string.label_suggestion_item_context_menu_edit)
                )?.setOnMenuItemClickListener(this)
                menu?.add( this.bindingAdapterPosition,2,2, binding.root.context.getString(R.string.label_suggestion_item_context_menu_delete)
                )?.setOnMenuItemClickListener(this)
                val currentUser: User? =
                    if (viewModel is SuggestionsViewModel)
                        viewModel.currentUser.value!!
                    else if (viewModel is UserPlacesViewModel)
                        viewModel.currentUser.value!!
                    else null

                if (data[bindingAdapterPosition].user.id != currentUser!!.id) {
                    //make the delete button available to the suggester and moderator, and edit to the suggester
                    if(currentUser.role!=UserRole.MODERATOR){menu?.getItem(1)?.isEnabled = false }// delete (order 2)
                    menu?.getItem(0)?.isEnabled = false // edit (order 2)
                }
            }

        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            val currentSuggestion = data[bindingAdapterPosition]
            return when (item.order){
                1 -> {
                    val intent = Intent(binding.root.context, SuggestionEditorActivity::class.java)
                    intent.putExtra("id", currentSuggestion.id)
                    launcher?.launch(intent)
                    true
                }
                2 ->{
                    val builder = AlertDialog.Builder(binding.root.context)
                    builder.setTitle(binding.root.context.getString(R.string.label_suggestion_item_context_menu_delete))
                    builder.setMessage(binding.root.context.getString(R.string.confirm_delete_suggestion))
                    builder.setPositiveButton(binding.root.context.getString(R.string.label_suggestion_yes)) { _, _ ->
                        // Delete the complaint
                        if (viewModel is SuggestionsViewModel) {
                            viewModel.deleteSuggestion(currentSuggestion.id)
                        } else if (viewModel is UserPlacesViewModel) {
                            viewModel.deleteSuggestion(currentSuggestion.id)
                        }
                    }
                    builder.setNegativeButton(binding.root.context.getString(R.string.label_suggestion_no)) { dialog, _ ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                    builder.create().show()
                    true

                }
                else -> false
            }
        }
    }
}