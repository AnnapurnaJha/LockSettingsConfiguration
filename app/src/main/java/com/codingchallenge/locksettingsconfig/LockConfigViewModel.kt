package com.codingchallenge.locksettingsconfig

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * ViewModel class for managing the data and business logic related to the lock configuration screen.
 * This ViewModel communicates with the UI components of the lock configuration screen,
 * providing data to display and handling user interactions.
 * It abstracts the data from the UI, allowing for separation of concerns and easier testing.
 */
class LockConfigViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        val TAG: String = LockConfigViewModel::class.java.simpleName
        const val PARAMETER_NAME = "parameterName"
    }

    private val apiService = LockConfigClient.apiService
    private val repository = LockConfigRepository(apiService)
    private val context by lazy { getApplication<Application>().applicationContext }

    private val _primaryLockSettings = MutableLiveData<LockConfigData>()
    val primaryLockSettings: LiveData<LockConfigData> = _primaryLockSettings

    private val _secondaryLockSettings = MutableLiveData<LockConfigData>()
    val secondaryLockSettings: LiveData<LockConfigData> = _secondaryLockSettings

    /**
     * Fetches the lock configuration from the api or locally and updates the live data
     */
    fun fetchConfig() {
        viewModelScope.launch {
            try {
                // Check if the configuration already saved
                val config = LockConfigManager(context).getLockSettings(DoorType.PRIMARY)
                Log.v(TAG, "Saved Config : " + config.toString())
                config?.let {
                    _primaryLockSettings.value = it
                    _secondaryLockSettings.value =
                        LockConfigManager(context).getLockSettings(DoorType.SECONDARY)
                    Log.v(TAG, "Updated config to Live Data")
                }
                    ?: let {// Fetch the config from API if not saved locally
                        Log.v(TAG, "Fetching config from API")
                        // Fetch config from the api
                        val data = repository.fetchConfig()
                        // Save fetched data as default values for both doors initially.
                        Log.v(TAG, "Fetched Config : $data")
                        saveLockSettings(data, data)
                    }
            } catch (e: Exception) {
                // Handle error
                Log.v(TAG, "Exception occurred while fetching config : $e")
            }
        }
    }

    /**
     *  Method to save lock settings for primary and secondary doors
     */
    private fun saveLockSettings(
        primaryLockSettings: LockConfigData,
        secondaryLockSettings: LockConfigData
    ) {
        LockConfigManager(context).saveLockSettings(primaryLockSettings, DoorType.PRIMARY)
        LockConfigManager(context).saveLockSettings(secondaryLockSettings, DoorType.SECONDARY)
        _primaryLockSettings.value = primaryLockSettings
        _secondaryLockSettings.value = secondaryLockSettings
    }

    /**
     * Updates the lock settings with the new value of a parameter for the specific door
     */
    fun updateLockSettings(
        property: String,
        value: String,
        doorType: DoorType
    ) {
        when (doorType) {
            DoorType.PRIMARY -> _primaryLockSettings.value =
                LockConfigManager(context).updateAndSaveLockSettings(property, value, doorType)

            DoorType.SECONDARY -> _secondaryLockSettings.value =
                LockConfigManager(context).updateAndSaveLockSettings(property, value, doorType)
        }
    }

    /**
     * Returns the list of values for a specific parameter based on type of door
     */
    fun getValues(parameter: String, doorType: DoorType): List<String>? {
        val lockSettings = LockConfigManager(context).getLockSettings(doorType)
        val values = when (parameter) {
            LockConfigData::lockVoltage.name -> lockSettings?.lockVoltage?.values
            LockConfigData::lockType.name -> lockSettings?.lockType?.values
            LockConfigData::lockKick.name -> lockSettings?.lockKick?.values
            LockConfigData::lockRelease.name -> lockSettings?.lockRelease?.values
            else -> null
        }
        return values
    }

    /**
     * Returns tha range value for a specific parameter based on the type of door.
     */
    fun getRange(parameter: String, doorType: DoorType): Range? {
        val lockSettings = LockConfigManager(context).getLockSettings(doorType)
        val range = when (parameter) {
            LockConfigData::lockReleaseTime.name -> lockSettings?.lockReleaseTime?.range
            LockConfigData::lockAngle.name -> lockSettings?.lockAngle?.range
            else -> null
        }
        return range
    }

    /**
     * Retrieves the default value for a specific property based on the type of door.
     * This method is used to obtain default values for various properties associated with different types of doors.
     */
    fun getDefaultValue(parameter: String, doorType: DoorType): String {
        val lockSettings = LockConfigManager(context).getLockSettings(doorType)
        val defaultValue = when (parameter) {
            LockConfigData::lockVoltage.name -> lockSettings?.lockVoltage?.default
            LockConfigData::lockType.name -> lockSettings?.lockType?.default
            LockConfigData::lockKick.name -> lockSettings?.lockKick?.default
            LockConfigData::lockRelease.name -> lockSettings?.lockRelease?.default
            LockConfigData::lockReleaseTime.name -> lockSettings?.lockReleaseTime?.default
            LockConfigData::lockAngle.name -> lockSettings?.lockAngle?.default
            else -> ""
        }
        return defaultValue.toString()
    }

    /**
     * Returns true if the same parameter and the value is used for both the doors
     */
    fun isCommon(parameter: String): Boolean {
        val lockSettings = LockConfigManager(context).getLockSettings(DoorType.PRIMARY)
        val isCommon = when (parameter) {
            LockConfigData::lockRelease.name -> lockSettings?.lockRelease?.common
            LockConfigData::lockAngle.name -> lockSettings?.lockAngle?.common
            else -> false
        }
        return isCommon ?: false
    }

    /**
     * Validates the given input against the specified range and returns an error message if the input is not within the range.
     * Returns null if the input is within the range and valid.
     */
    fun validationError(input: String, range: Range): String? {
        try {
            if (input.isEmpty()) {
                return "Please enter the value"
            }
            val value = input.toDouble()
            return if (value in range.min..range.max) {
                // Input is valid and there is no validation error
                null
            } else {
                "Please enter the value within the range"
            }
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Exception occurred while validating input : $e")
            return "Error"
        }
    }
}


