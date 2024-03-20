package com.codingchallenge.locksettingsconfig

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object for managing API requests related to lock configurations using Retrofit.
 * This object provides methods to create and configure Retrofit instances for communicating with the lock configuration API endpoints.
 */
object LockConfigClient {

    private const val BASE_URL = "https://run.mocky.io/v3/"

    val apiService: ILockConfigApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ILockConfigApiService::class.java)
    }

}