package com.codingchallenge.locksettingsconfig

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlin.reflect.KProperty1

enum class DoorType {
    PRIMARY,
    SECONDARY
}

private const val PRIMARY_LOCK_SETTINGS = "primarylockSettings"
private const val SECONDARY_LOCK_SETTINGS = "secondarylockSettings"

class LockSettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("LockSettings", Context.MODE_PRIVATE)


    private val gson = Gson()

    // Save LockSettings to SharedPreferences
    fun saveLockSettings(lockConfigData: LockConfigData, doorType: DoorType) {
        val json = gson.toJson(lockConfigData)
        when (doorType) {
            DoorType.PRIMARY -> sharedPreferences.edit().putString(PRIMARY_LOCK_SETTINGS, json).apply()
            DoorType.SECONDARY -> sharedPreferences.edit().putString(SECONDARY_LOCK_SETTINGS, json).apply()
        }
    }

    // Retrieve LockConfigData from SharedPreferences
    fun getLockSettings(doorType: DoorType): LockConfigData? {
        val json = when (doorType) {
            DoorType.PRIMARY -> sharedPreferences.getString(PRIMARY_LOCK_SETTINGS, null)
            DoorType.SECONDARY -> sharedPreferences.getString(SECONDARY_LOCK_SETTINGS, null)
        }
        return gson.fromJson(json, LockConfigData::class.java)
    }

    // Update and save lock settings locally
    /*fun updateAndSaveLockSettings() {
        // Retrieve existing lock settings
        val lockConfigData = getLockSettings() ?: return

        // Modify lock settings as needed
        lockConfigData.lockVoltage.default = "12V"
        lockConfigData.lockType.default = "Lock with power"

        // Save updated lock settings
        saveLockSettings(lockConfigData)
        Log.v("MyUpdatedData", lockConfigData.toString())
    }*/

    // Update and save lock settings locally
    fun <T> updateAndSaveLockSettings(
        property: KProperty1<LockConfigData, T>,
        value: String,
        doorType: DoorType
    ): LockConfigData? {
        // Retrieve existing lock settings
        val lockSettings = getLockSettings(doorType) ?: return null
        val result = property.get(lockSettings)
        when (result) {
            is LockVoltage -> lockSettings.lockVoltage.default = value
            is LockType -> lockSettings.lockType.default = value
            is LockKick -> lockSettings.lockKick.default = value
            is LockRelease -> lockSettings.lockRelease.default = value
            is LockReleaseTime -> lockSettings.lockReleaseTime.default = value.toDouble()
            is LockAngle -> lockSettings.lockAngle.default = value.toInt()
            else -> println("x is something else")
        }

        // Save updated lock settings
        saveLockSettings(lockSettings, doorType)
        return lockSettings
    }

    fun updateAndSaveLockSettings(
        property: String,
        value: String,
        doorType: DoorType
    ): LockConfigData? {
        // Retrieve existing lock settings
        val lockSettings = getLockSettings(doorType) ?: return null
        when (property) {
            LockConfigData::lockVoltage.name -> lockSettings.lockVoltage.default = value
            LockConfigData::lockType.name -> lockSettings.lockType.default = value
            LockConfigData::lockKick.name -> lockSettings.lockKick.default = value
            LockConfigData::lockRelease.name -> lockSettings.lockRelease.default = value
            LockConfigData::lockReleaseTime.name -> lockSettings.lockReleaseTime.default = value.toDouble()
            LockConfigData::lockAngle.name -> lockSettings.lockAngle.default = value.toDouble().toInt() // this is to handle exception if user enters a double value
            else -> println("x is something else")
        }

        // Save updated lock settings
        saveLockSettings(lockSettings, doorType)
        return lockSettings
    }

}