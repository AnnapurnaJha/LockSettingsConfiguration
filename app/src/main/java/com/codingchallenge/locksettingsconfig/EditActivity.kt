package com.codingchallenge.locksettingsconfig

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.codingchallenge.locksettingsconfig.databinding.ActivityEditBinding

class EditActivity : AppCompatActivity() {

    private var isCommon: Boolean = false
    private lateinit var binding: ActivityEditBinding
    private lateinit var viewModel: LockConfigViewModel
    private lateinit var property: String
    private var range: Range? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this).get(LockConfigViewModel::class.java)
        Log.v("MyData", "View Model attached")

        property = intent.getStringExtra(viewModel.parameterName) ?: "None"
        setListeners()

        setupDoor(DoorType.PRIMARY)
        isCommon = viewModel.isCommon(property)

        if (!isCommon) {
            binding.idCvSecondaryDoor.visibility = View.VISIBLE
            setupDoor(DoorType.SECONDARY)
        }
    }

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

        val values = viewModel.getValues(property, doorType)
        val defaultValue = viewModel.getDefaultValue(property, doorType)
        defaultValueTv.text = getString(R.string.default_value, defaultValue)

        values?.let {
            setValues(it, defaultValue, radioGroup)
        } ?: run {
            range = viewModel.getRange(property, doorType)
            setRange(editText)
        }
    }

    private fun setRange(editText: EditText) {
        editText.visibility = View.VISIBLE
        editText.hint = getString(R.string.hint_min_max, range?.min, range?.max)
    }

    private fun setValues(
        items: List<String>?,
        defaultValue: String,
        radioGroup: RadioGroup,
    ) {
        radioGroup.visibility = View.VISIBLE
        // Clear existing radio buttons
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

    private fun setListeners() {
        binding.idBtnSave.setOnClickListener {
            // Save the value from Range or values based on view visibility
            if (binding.idRgPrimaryValues.isVisible) {
                // Save edited data and call method in ViewModel to handle navigation
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

    private fun saveParameter(radioGroup: RadioGroup, doorType: DoorType) {
        val selectedRb = findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
        val selectedValue = selectedRb?.text.toString()
        viewModel.updateLockSettings(property, selectedValue, doorType)
    }

    private fun saveParameter(editText: EditText, doorType: DoorType) {
        val errorMsg = range?.let { viewModel.validationError(editText.text.toString(), it) }
        // Show the error if there is a error message, else proceed to save
        errorMsg?.let {
            editText.setError(it)
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

