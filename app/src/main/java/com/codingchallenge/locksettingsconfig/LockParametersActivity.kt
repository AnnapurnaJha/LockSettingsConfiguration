package com.codingchallenge.locksettingsconfig

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import com.codingchallenge.locksettingsconfig.databinding.ActivityLockParametersBinding

/**
 * Activity for displaying lock parameters.
 * This activity allows users to view various parameters associated with locks of different doors and enables to edit them
 */
class LockParametersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockParametersBinding
    private lateinit var viewModel: LockConfigViewModel
    private lateinit var parameterTextViews: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLockParametersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[LockConfigViewModel::class.java]

        setListeners()
        setObservers()
    }

    /**
     * Sets the observer for the live data in the view model and updates the UI accordingly
     */
    private fun setObservers() {
        // Observe primary lockSettings LiveData
        viewModel.primaryLockSettings.observe(this) { lockSettings ->
            // Update UI with new lock settings
            binding.idTvPrimaryVoltage.text = lockSettings.lockVoltage.default
            binding.idTvPrimaryType.text = lockSettings.lockType.default
            binding.idTvPrimaryKick.text = lockSettings.lockKick.default
            binding.idTvRelease.text = lockSettings.lockRelease.default
            binding.idTvPrimaryReleaseTime.text = lockSettings.lockReleaseTime.default.toString()
            binding.idTvAngle.text = lockSettings.lockAngle.default.toString()
        }

        // Observe secondary lockSettings LiveData
        viewModel.secondaryLockSettings.observe(this) { lockSettings ->
            // Update UI with new lock settings
            binding.idTvSecondaryVoltage.text = lockSettings.lockVoltage.default
            binding.idTvSecondaryType.text = lockSettings.lockType.default
            binding.idTvSecondaryKick.text = lockSettings.lockKick.default
            binding.idTvRelease.text = lockSettings.lockRelease.default
            binding.idTvSecondaryReleaseTime.text = lockSettings.lockReleaseTime.default.toString()
            binding.idTvAngle.text = lockSettings.lockAngle.default.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchConfig()
    }

    /**
     * Sets the listener to all the views for user action
     */
    private fun setListeners() {
        binding.idIbEditVoltage.setOnClickListener {
            editParameter(LockConfigData::lockVoltage.name)
        }

        binding.idIbEditType.setOnClickListener {
            editParameter(LockConfigData::lockType.name)
        }

        binding.idIbEditKick.setOnClickListener {
            editParameter(LockConfigData::lockKick.name)
        }

        binding.idIbEditRelease.setOnClickListener {
            editParameter(LockConfigData::lockRelease.name)
        }

        binding.idIbEditReleaseTime.setOnClickListener {
            editParameter(LockConfigData::lockReleaseTime.name)
        }

        binding.idIbEditAngle.setOnClickListener {
            editParameter(LockConfigData::lockAngle.name)
        }

        prepareParameterTextViewsList()
        binding.idSvParameter.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Highlight TextViews based on the search term
                newText?.let { searchTerm ->
                    highlightTextViewsWithSearchTerm(searchTerm)
                }
                return true
            }
        })
    }

    /**
     * Highlights TextViews containing the specified search term.
     * This function searches through a list of TextViews containing parameters and its values
     * and highlights those that contain the specified search term by changing their background color.
     * If the search term is empty, all highlights are removed.
     */
    private fun highlightTextViewsWithSearchTerm(searchTerm: String) {
        // Loop through the list of TextViews
        for (textView in parameterTextViews) {
            val text = textView.text.toString()
            // Check if the TextView's text contains the search term and the search term is not empty
            if (searchTerm.isNotEmpty() && text.contains(searchTerm, ignoreCase = true)) {
                // Highlight the TextView
                textView.setBackgroundColor(getColor(R.color.teal_200)) // Change background color
            } else {
                // Remove highlight if search term is not found
                textView.setBackgroundColor(getColor(R.color.transparent))
            }
        }
    }

    /**
     * Prepares a list of all the TextViews having lock parameter names and values
     */
    private fun prepareParameterTextViewsList() {
        parameterTextViews = listOf(
            binding.idTvTitleVoltage,
            binding.idTvPrimaryVoltage,
            binding.idTvSecondaryVoltage,
            binding.idTvTitleType,
            binding.idTvPrimaryType,
            binding.idTvSecondaryType,
            binding.idTvTitleKick,
            binding.idTvPrimaryKick,
            binding.idTvSecondaryKick,
            binding.idTvTitleRelease,
            binding.idTvRelease,
            binding.idTvTitleReleaseTime,
            binding.idTvPrimaryReleaseTime,
            binding.idTvSecondaryReleaseTime,
            binding.idTvTitleAngle,
            binding.idTvAngle
        )
    }

    /**
     * Navigates the user to the edit screen
     * @param parameter lock parameter name which needs to be edited
     */
    private fun editParameter(parameter: String) {
        val intent = Intent(this@LockParametersActivity, EditActivity::class.java)
        intent.putExtra(LockConfigViewModel.PARAMETER_NAME, parameter)
        startActivity(intent)
    }
}

