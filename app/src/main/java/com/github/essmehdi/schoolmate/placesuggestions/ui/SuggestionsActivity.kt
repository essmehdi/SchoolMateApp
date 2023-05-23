package com.github.essmehdi.schoolmate.placesuggestions.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.ActivitySuggestionsBinding
import com.github.essmehdi.schoolmate.placesuggestions.adapters.SuggestionsViewPagerAdapter
import com.github.essmehdi.schoolmate.placesuggestions.viewmodels.SuggestionsViewModel
import com.google.android.material.tabs.TabLayoutMediator


class SuggestionsActivity : AppCompatActivity(){

    private lateinit var binding: ActivitySuggestionsBinding
    lateinit var launcher: ActivityResultLauncher<Intent>
    val viewModel: SuggestionsViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuggestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // TODO verify
                viewModel.refresh()
            }
        }

        viewModel.fetchCurrentUser()
        viewModel.loadSuggestions()
        viewModel.loadCurrentUserSuggestions()


        setSupportActionBar(binding.suggestionsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_navigation_left)

        binding.suggestionAddButton.setOnClickListener{
            launcher?.launch(Intent(this, SuggestionEditorActivity::class.java))
        }

        setupViewPager()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.suggestions_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.suggestions_menu_sort_desc ->{
                item.isChecked = !item.isChecked
                viewModel.changeSortOder("desc")
                return true
            }
            R.id.suggestions_menu_sort_asc -> {
                item.isChecked = !item.isChecked
                viewModel.changeSortOder("asc")
                return true
            }
            else -> false

        }
    }

    private fun setupViewPager() {
        binding.suggestionViewPager.adapter = SuggestionsViewPagerAdapter(this)
        TabLayoutMediator(binding.suggestionTabLayout, binding.suggestionViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.all_suggestions_title)
                else -> getString(R.string.my_suggestions_title)
            }
        }.attach()
    }
}