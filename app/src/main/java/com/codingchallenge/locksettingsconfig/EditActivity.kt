package com.codingchallenge.locksettingsconfig

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.codingchallenge.locksettingsconfig.databinding.ActivityEditBinding

/**
 * Activity for editing lock configurations.
 * This activity allows users to edit the default value of the parameters for all types of doors and save them.
 */
class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private lateinit var viewModel: LockConfigViewModel
    private lateinit var property: String
    private var isCommon: Boolean = false
    private var range: Range? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[LockConfigViewModel::class.java]
        property = intent.getStringExtra(LockConfigViewModel.PARAMETER_NAME) ?: "None"
        setListeners()

        setupUI()
    }

    /**
     * Sets up the UI based on the parameter and its values for both the doors
     */
    private fun setupUI() {
        setupDoor(DoorType.PRIMARY)
        isCommon = viewModel.isCommon(property)
        // If the property is common for both the doors, only update the title
        if (isCommon) {
            binding.idTvPrimary.text = getString(R.string.primary_secondary)
        } else { // Update the UI to configure parameter for both the doors separately
            binding.idCvSecondaryDoor.visibility = View.VISIBLE
            setupDoor(DoorType.SECONDARY)
        }
    }

    /**
     * Sets up the UI parameter for a specific door
     * @param doorType type of the door : Primary or Secondary
     */
    private fun setupDoor(doorType: DoorType) {
        binding.idTvTitle.text = property
        val defaultValueTv: TextView
        val radioGroup: RadioGroup
        val editText: EditText

        when (doorType) {
            DoorType.PRIMARY -> {
                defaultValueTv = binding.idTvDefaultPrValue
                radioGroup = binding.idRgPrimaryValues
                editText = binding.idEtPrimaryValue
            }

            DoorType.SECONDARY -> {
                defaultValueTv = binding.idTvDefaultSeValue
                radioGroup = binding.idRgSecondaryValues
                editText = binding.idEtSecondaryValue
            }
        }

        // Set the default value of the parameter
        val defaultValue = viewModel.getDefaultValue(property, doorType)
        defaultValueTv.text = getString(R.string.default_value, defaultValue)

        // Check if the parameter has values or range, and set them accordingly
        val values = viewModel.getValues(property, doorType)
        values?.let {
            setValues(it, defaultValue, radioGroup)
        } ?: run {
            range = viewModel.getRange(property, doorType)
            setRange(editText)
        }
    }

    /**
     * Sets the permitted range for the parameter
     */
    private fun setRange(editText: EditText) {
        editText.visibility = View.VISIBLE
        editText.hint = getString(R.string.hint_min_max, range?.min, range?.max)
    }

    /**
     * Sets the possible values for the parameter
     */
    private fun setValues(
        items: List<String>?,
        defaultValue: String,
        radioGroup: RadioGroup,
    ) {
        radioGroup.visibility = View.VISIBLE
        // Clear existing radio buttons, if any
        radioGroup.removeAllViews()

        // Populate RadioGroup with new items
        items?.forEach { item ->
            val radioButton = RadioButton(this)
            radioButton.text = item
            radioButton.tag = item
            radioGroup.addView(radioButton)
        }
        // Set default selected item
        val defaultRadioButton = radioGroup.findViewWithTag<RadioButton>(defaultValue)
        defaultRadioButton?.isChecked = true
    }

    /**
     * Sets the listener to all the views for user action
     */
    private fun setListeners() {
        binding.idBtnSave.setOnClickListener {
            // Save the value from Range or values based on view visibility
            if (binding.idRgPrimaryValues.isVisible) {
                saveParameter(binding.idRgPrimaryValues, DoorType.PRIMARY)
                // Save the same value for Secondary door if the common is true
                val radioGroup =
                    if (isCommon) binding.idRgPrimaryValues else binding.idRgSecondaryValues
                saveParameter(radioGroup, DoorType.SECONDARY)
                finish()
            } else {
                saveParameter(binding.idEtPrimaryValue, DoorType.PRIMARY)
                // Save the same value for Secondary door if the common is true, else save the secondary value
                val editText =
                    if (isCommon) binding.idEtPrimaryValue else binding.idEtSecondaryValue
                saveParameter(editText, DoorType.SECONDARY)
            }
        }
        binding.idBtnCancel.setOnClickListener {
            finish()
        }
    }

    /**
     * Saves the value of parameter with values
     */
    private fun saveParameter(radioGroup: RadioGroup, doorType: DoorType) {
        val selectedRb = findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
        val selectedValue = selectedRb?.text.toString()
        viewModel.updateLockSettings(property, selectedValue, doorType)
    }

    /**
     * Saves the value of parameter with range
     */
    private fun saveParameter(editText: EditText, doorType: DoorType) {
        val errorMsg = range?.let { viewModel.validationError(editText.text.toString(), it) }
        // Show the error message, if there is any, else proceed to save
        errorMsg?.let {
            editText.error = it
        } ?: run {
            viewModel.updateLockSettings(property, editText.text.toString(), doorType)
            finish()
        }
    }

    // Handle the Up button click
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

