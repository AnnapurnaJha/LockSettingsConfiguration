package com.codingchallenge.locksettingsconfig

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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