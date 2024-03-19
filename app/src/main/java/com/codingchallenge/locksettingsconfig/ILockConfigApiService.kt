package com.codingchallenge.locksettingsconfig

import retrofit2.http.GET

interface ILockConfigApiService {
    @GET("d5f5d613-474b-49c4-a7b0-7730e8f8f486")
    suspend fun getConfigData(): LockConfigData
}