package com.codingchallenge.locksettingsconfig

import retrofit2.http.GET

/**
 * Interface representing the API service for managing lock configurations.
 * This interface defines methods for interacting with the lock configuration API endpoints.
 */
interface ILockConfigApiService {
    @GET("d5f5d613-474b-49c4-a7b0-7730e8f8f486")
    suspend fun getConfigData(): LockConfigData
}