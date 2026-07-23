package com.example.hesapmakinesi

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET

@Serializable
data class CurrencyResponse(
    val result: String,
    val base_code: String,
    val rates: Map<String, Double>
)

interface CurrencyApi {
    @GET("latest/USD")
    suspend fun getLatestRates(): CurrencyResponse
}

object RetrofitClient {
    private val json = Json { ignoreUnknownKeys = true }
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://open.er-api.com/v6/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: CurrencyApi = retrofit.create(CurrencyApi::class.java)
}
