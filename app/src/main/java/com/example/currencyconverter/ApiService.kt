package com.example.currencyconverter

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class ExchangeRateResponse(
    val result: String,
    val base_code: String,
    val target_code: String,
    val conversion_rate: Double,
    val time_last_update_utc: String
)

data class      AllRatesResponse(
    val result: String,
    val base_code: String,
    @SerializedName("rates")
    val rates: Map<String, Double>? = null
)

interface ApiService {
    @GET("v6/latest")
    suspend fun getAllRates(
        @Query("base") baseCurrency: String
    ): AllRatesResponse

    @GET("v6/pair")
    suspend fun getExchangeRate(
        @Query("from") fromCurrency: String,
        @Query("to") toCurrency: String
    ): ExchangeRateResponse
} 