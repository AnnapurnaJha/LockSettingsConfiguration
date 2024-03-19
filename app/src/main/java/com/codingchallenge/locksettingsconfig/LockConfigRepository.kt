package com.codingchallenge.locksettingsconfig

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LockConfigRepository (private val apiService: ILockConfigApiService) {
    suspend fun fetchConfig(): LockConfigData {
        return withContext(Dispatchers.IO) {
            try {
                apiService.getConfigData()
            } catch (e: Exception) {
                // Handle error
                throw e
            }
        }
    }
}