package com.codingchallenge.locksettingsconfig

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LockConfigViewModel(application: Application) : AndroidViewModel(application){

    private val apiService = LockConfigClient.apiService
    private val repository = LockConfigRepository(apiService)
    private val context by lazy { getApplication<Application>().applicationContext }

    private val _primaryLockSettings = MutableLiveData<LockConfigData>()
    val primaryLockSettings: LiveData<LockConfigData> = _primaryLockSettings

    private val _secondaryLockSettings = MutableLiveData<LockConfigData>()
    val secondaryLockSettings: LiveData<LockConfigData> = _secondaryLockSettings

    private val _filteredLockSettings = MutableLiveData<LockConfigData?>()
    val filteredLockSettings: MutableLiveData<LockConfigData?> = _filteredLockSettings

    val parameterName = "parameterName"

    fun fetchConfig() {
        viewModelScope.launch {
            try {
                // Check if the configuration already saved
                val config = LockSettingsManager(context).getLockSettings(DoorType.PRIMARY)
                Log.v("MySavedData", config.toString())
                config?.let {
                    Log.v("MySavedData", "Updating UI")
                    _primaryLockSettings.value = it
                    _secondaryLockSettings.value = LockSettingsManager(context).getLockSettings(DoorType.SECONDARY)
                }
                    ?: let {
                        Log.v("Fetching data", "Fetching data")
                        // Fetch config from the api
                        val data = repository.fetchConfig()
                        // Save fetched data as default values for both doors initially.
                        Log.v("Fetched Data", data.toString())
                        saveLockSettings(data, data)
                    }

            } catch (e: Exception) {
                // Handle error
                Log.v("MyData", "Exception occurred")
                e.printStackTrace()
            }
        }
    }

    // Method to save lock settings
    private fun saveLockSettings(
        primaryLockSettings: LockConfigData,
        secondaryLockSettings: LockConfigData
    ) {
        LockSettingsManager(context).saveLockSettings(primaryLockSettings, DoorType.PRIMARY)
        LockSettingsManager(context).saveLockSettings(secondaryLockSettings, DoorType.SECONDARY)
        _primaryLockSettings.value = primaryLockSettings
        _secondaryLockSettings.value = secondaryLockSettings
    }

    fun updateLockSettings(
        property: String,
        value: String,
        doorType: DoorType
    ) {
        when (doorType) {
            DoorType.PRIMARY -> _primaryLockSettings.value =
                LockSettingsManager(context).updateAndSaveLockSettings(property, value, doorType)

            DoorType.SECONDARY -> _secondaryLockSettings.value =
                LockSettingsManager(context).updateAndSaveLockSettings(property, value, doorType)
        }
    }

    fun getValues(property: String, doorType: DoorType): List<String>? {
        val lockSettings = LockSettingsManager(context).getLockSettings(doorType)
        val values = when (property) {
            LockConfigData::lockVoltage.name -> lockSettings?.lockVoltage?.values
            LockConfigData::lockType.name -> lockSettings?.lockType?.values
            LockConfigData::lockKick.name -> lockSettings?.lockKick?.values
            LockConfigData::lockRelease.name -> lockSettings?.lockRelease?.values
            else -> null
        }
        return values
    }

    fun getRange(property: String, doorType: DoorType): Range? {
        val lockSettings = LockSettingsManager(context).getLockSettings(doorType)
        val range = when (property) {
            LockConfigData::lockReleaseTime.name -> lockSettings?.lockReleaseTime?.range
            LockConfigData::lockAngle.name -> lockSettings?.lockAngle?.range
            else -> null
        }
        return range
    }

    fun getDefaultValue(property: String, doorType: DoorType): String {
        val lockSettings = LockSettingsManager(context).getLockSettings(doorType)
        val defaultValue = when (property) {
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

    fun isCommon(property: String): Boolean {
        val lockSettings = LockSettingsManager(context).getLockSettings(DoorType.PRIMARY)
        val isCommon = when (property) {
            LockConfigData::lockRelease.name -> lockSettings?.lockRelease?.common
            LockConfigData::lockAngle.name -> lockSettings?.lockAngle?.common
            else -> false
        }
        return isCommon ?: false
    }

    // Validation function
    fun validationError(input: String, range: Range) : String? {
        try {
            if(input.isEmpty()){
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
            return "Error"
        }
    }

    fun searchLockSettings(query: String) {
        val filteredPrimary = _primaryLockSettings.value?.let { lockSettings ->
            filterLockSettings(lockSettings, query)
        }

//        val filteredSecondary = _secondaryLockSettings.value?.let { lockSettings ->
//            filterLockSettings(lockSettings, query)
//        }

        //val combinedList = filteredPrimary + filteredSecondary
        _filteredLockSettings.value = filteredPrimary
    }

    private fun filterLockSettings(lockSettings: LockConfigData, query: String): LockConfigData {
        return lockSettings.copy(
            lockVoltage = filterLockVoltage(lockSettings.lockVoltage, query),
            lockType = filterLockType(lockSettings.lockType, query),
            lockKick = filterLockKick(lockSettings.lockKick, query),
            lockRelease = filterLockRelease(lockSettings.lockRelease, query),
            lockReleaseTime = filterLockReleaseTime(lockSettings.lockReleaseTime, query),
            lockAngle = filterLockAngle(lockSettings.lockAngle, query)
        )
    }

    private fun filterLockVoltage(lockVoltage: LockVoltage, query: String): LockVoltage {
        return lockVoltage.copy(
            values = lockVoltage.values.filter { it.contains(query, ignoreCase = true) }
        )
    }

    private fun filterLockType(lockType: LockType, query: String): LockType {
        return lockType.copy(
            values = lockType.values.filter { it.contains(query, ignoreCase = true) }
        )
    }

    private fun filterLockKick(lockKick: LockKick, query: String): LockKick {
        return lockKick.copy(
            values = lockKick.values.filter { it.contains(query, ignoreCase = true) }
        )
    }

    private fun filterLockRelease(lockRelease: LockRelease, query: String): LockRelease {
        return lockRelease.copy(
            values = lockRelease.values.filter { it.contains(query, ignoreCase = true) }
        )
    }

    private fun filterLockReleaseTime(lockReleaseTime: LockReleaseTime, query: String): LockReleaseTime {
        // Range doesn't need to be filtered
        return lockReleaseTime
    }

    private fun filterLockAngle(lockAngle: LockAngle, query: String): LockAngle {
        // Range doesn't need to be filtered
        return lockAngle
    }


}


