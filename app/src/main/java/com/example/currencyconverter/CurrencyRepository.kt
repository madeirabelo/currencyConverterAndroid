package com.example.currencyconverter

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class CurrencyRepository {
    private val apiService: ApiService

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    suspend fun fetchAllRates(baseCurrency: String = "USD"): Map<String, Double> {
        return try {
            Log.d("CurrencyRepository", "Fetching rates for base currency: $baseCurrency")
            val response = apiService.getAllRates(baseCurrency)
            Log.d("CurrencyRepository", "API Response: $response")
            
            if (response.result == "success" && response.rates != null) {
                Log.d("CurrencyRepository", "Success! Rates: ${response.rates}")
                response.rates
            } else {
                Log.e("CurrencyRepository", "API returned error or null rates. Result: ${response.result}, Rates: ${response.rates}")
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e("CurrencyRepository", "API call failed", e)
            emptyMap()
        }
    }

    suspend fun fetchExchangeRate(fromCurrency: String, toCurrency: String): Double? {
        return try {
            Log.d("CurrencyRepository", "Fetching rate from $fromCurrency to $toCurrency")
            val response = apiService.getExchangeRate(fromCurrency, toCurrency)
            Log.d("CurrencyRepository", "API Response: $response")
            
            if (response.result == "success") {
                Log.d("CurrencyRepository", "Success! Rate: ${response.conversion_rate}")
                response.conversion_rate
            } else {
                Log.e("CurrencyRepository", "API returned error: ${response.result}")
                null
            }
        } catch (e: Exception) {
            Log.e("CurrencyRepository", "API call failed", e)
            null
        }
    }
} 