package com.codingchallenge.locksettingsconfig

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson

enum class DoorType {
    PRIMARY,
    SECONDARY
}

/**
 * Manages the configuration settings for locks in the application, persisting them using SharedPreferences.
 * This class provides methods to retrieve, save and update lock configurations,
 * as well as save them to and retrieve them from SharedPreferences.
 */
class LockConfigManager(context: Context) {

    companion object {
        private val TAG = LockConfigManager::class.java.simpleName
        private const val PRIMARY_LOCK_SETTINGS = "primaryLockSettings"
        private const val SECONDARY_LOCK_SETTINGS = "secondaryLockSettings"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("LockSettings", Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Save LockSettings to SharedPreferences for a specific door
     */
    fun saveLockSettings(lockConfigData: LockConfigData, doorType: DoorType) {
        val json = gson.toJson(lockConfigData)
        when (doorType) {
            DoorType.PRIMARY -> sharedPreferences.edit().putString(PRIMARY_LOCK_SETTINGS, json).apply()
            DoorType.SECONDARY -> sharedPreferences.edit().putString(SECONDARY_LOCK_SETTINGS, json).apply()
        }
        Log.v(TAG, "LockConfig saved to Shared Preferences for $doorType : $lockConfigData")
    }

    /**
     * Retrieve LockConfigData from SharedPreferences for a specific door
     */
    fun getLockSettings(doorType: DoorType): LockConfigData? {
        val json = when (doorType) {
            DoorType.PRIMARY -> sharedPreferences.getString(PRIMARY_LOCK_SETTINGS, null)
            DoorType.SECONDARY -> sharedPreferences.getString(SECONDARY_LOCK_SETTINGS, null)
        }
        Log.v(TAG, "LockConfig retrieved from Shared Preferences for $doorType : $json " )
        return gson.fromJson(json, LockConfigData::class.java)
    }

    /**
     * Updates and saves the lock settings in shared preferences with the new value of a parameter for the specific door
     */
    fun updateAndSaveLockSettings(
        parameter: String,
        value: String,
        doorType: DoorType
    ): LockConfigData? {
        // Retrieve existing lock settings
        val lockSettings = getLockSettings(doorType) ?: return null
        when (parameter) {
            LockConfigData::lockVoltage.name -> lockSettings.lockVoltage.default = value
            LockConfigData::lockType.name -> lockSettings.lockType.default = value
            LockConfigData::lockKick.name -> lockSettings.lockKick.default = value
            LockConfigData::lockRelease.name -> lockSettings.lockRelease.default = value
            LockConfigData::lockReleaseTime.name -> lockSettings.lockReleaseTime.default = value.toDouble()
            LockConfigData::lockAngle.name -> lockSettings.lockAngle.default = value.toDouble().toInt() // this is to handle exception if user enters a double value
            else -> Log.v(TAG, "Unknown parameter found while updating preferences")
        }

        // Save updated lock settings
        saveLockSettings(lockSettings, doorType)
        return lockSettings
    }

}