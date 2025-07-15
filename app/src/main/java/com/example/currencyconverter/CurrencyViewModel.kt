package com.example.currencyconverter

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log

data class CurrencyValue(
    val currency: String,
    val value: String,
    val isUserInput: Boolean = false
)

class CurrencyViewModel : ViewModel() {
    private val repository = CurrencyRepository()
    
    private val _currencyValues = mutableStateOf(
        mapOf(
            "USD" to CurrencyValue("USD", ""),
            "EUR" to CurrencyValue("EUR", ""),
            "ARS" to CurrencyValue("ARS", ""),
            "PYG" to CurrencyValue("PYG", ""),
            "BRL" to CurrencyValue("BRL", ""),
            "UYU" to CurrencyValue("UYU", "")
        )
    )
    val currencyValues: State<Map<String, CurrencyValue>> = _currencyValues

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _lastUpdateTime = mutableStateOf("")
    val lastUpdateTime: State<String> = _lastUpdateTime

    // Store fetched rates for display in UI
    private val _fetchedRates = mutableStateOf(
        mapOf(
            "EUR" to "",
            "ARS" to "",
            "PYG" to "",
            "BRL" to "",
            "UYU" to ""
        )
    )
    val fetchedRates: State<Map<String, String>> = _fetchedRates

    private val exchangeRates = mutableMapOf<String, Double>(
        "USD" to 1.0,
        "EUR" to 0.8566,  // Default values
        "ARS" to 1266.0, // Default values
        "PYG" to 7828.0, // Default values
        "BRL" to 5.0, // Default values
        "UYU" to 40.0 // Default values
    )

    fun setExchangeRate(currency: String, rate: Double) {
        if (currency != "USD") { // USD is always 1.0
            exchangeRates[currency] = rate
            val newFetchedRates = _fetchedRates.value.toMutableMap()
            newFetchedRates[currency] = String.format("%.4f", rate)
            _fetchedRates.value = newFetchedRates
        }
    }

    fun updateCurrencyValue(currency: String, value: String) {
        val currentValues = _currencyValues.value.toMutableMap()
        
        // Clear all values first
        currentValues.forEach { (key, _) ->
            currentValues[key] = CurrencyValue(key, "")
        }
        
        // Set the user input
        currentValues[currency] = CurrencyValue(currency, value, true)
        
        // Convert to all other currencies
        val amount = value.toDoubleOrNull()
        if (amount != null && amount > 0) {
            val fromRate = exchangeRates[currency]
            if (fromRate != null) {
                for ((targetCurrency, _) in currentValues) {
                    if (targetCurrency != currency) {
                        val toRate = exchangeRates[targetCurrency] ?: continue
                        
                        // Convert to USD first, then to target currency
                        val inUSD = amount / fromRate
                        val result = inUSD * toRate

                        // Format the result with appropriate decimal places
                        val formattedResult = formatNumber(result, targetCurrency)

                        currentValues[targetCurrency] = CurrencyValue(targetCurrency, formattedResult, false)
                    }
                }
            }
        }
        
        _currencyValues.value = currentValues
    }

    fun fetchLatestRates() {
        viewModelScope.launch {
            Log.d("CurrencyViewModel", "Starting to fetch latest rates")
            _isLoading.value = true
            try {
                val rates = repository.fetchAllRates("USD")
                Log.d("CurrencyViewModel", "Received rates from API: $rates")
                
                if (rates.isNotEmpty()) {
                    // Update exchange rates with API data
                    exchangeRates["USD"] = 1.0
                    exchangeRates["EUR"] = rates["EUR"] ?: 0.8566
                    exchangeRates["ARS"] = rates["ARS"] ?: 1266.0
                    exchangeRates["PYG"] = rates["PYG"] ?: 7828.0
                    exchangeRates["BRL"] = rates["BRL"] ?: 5.0
                    exchangeRates["UYU"] = rates["UYU"] ?: 40.0
                    
                    Log.d("CurrencyViewModel", "Updated exchange rates: $exchangeRates")
                    
                    // Update fetched rates for display
                    val newFetchedRates = mutableMapOf<String, String>()
                    newFetchedRates["EUR"] = String.format("%.4f", rates["EUR"] ?: 0.8566)
                    newFetchedRates["ARS"] = String.format("%.2f", rates["ARS"] ?: 1266.0)
                    newFetchedRates["PYG"] = String.format("%.0f", rates["PYG"] ?: 7828.0)
                    newFetchedRates["BRL"] = String.format("%.2f", rates["BRL"] ?: 5.0)
                    newFetchedRates["UYU"] = String.format("%.2f", rates["UYU"] ?: 40.0)
                    _fetchedRates.value = newFetchedRates
                    
                    Log.d("CurrencyViewModel", "Updated fetched rates for display: $newFetchedRates")
                    
                    _lastUpdateTime.value = "Last updated: ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}"
                    Log.d("CurrencyViewModel", "Successfully updated rates at ${_lastUpdateTime.value}")
                } else {
                    Log.e("CurrencyViewModel", "No rates received from API - rates map is empty")
                    _lastUpdateTime.value = "Failed to fetch rates - API returned empty data"
                }
            } catch (e: Exception) {
                Log.e("CurrencyViewModel", "Error fetching rates", e)
                _lastUpdateTime.value = "Error: ${e.message}"
                // Keep existing rates if API fails
            } finally {
                _isLoading.value = false
                Log.d("CurrencyViewModel", "Finished fetching rates")
            }
        }
    }

    fun getCurrentRates(): Map<String, Double> {
        return exchangeRates.toMap()
    }

    private fun formatNumber(number: Double, currency: String): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.groupingSeparator = ' '

        return when {
            number >= 1_000_000 -> {
                val formatter = DecimalFormat("#,##0.00", symbols)
                "${formatter.format(number / 1_000_000)}M"
            }
            number >= 1_000 -> {
                val formatter = DecimalFormat("#,##0.00", symbols)
                "${formatter.format(number / 1_000)}k"
            }
            currency == "PYG" -> {
                val formatter = DecimalFormat("#,##0", symbols)
                formatter.format(number)
            }
            currency == "ARS" -> {
                val formatter = DecimalFormat("#,##0", symbols)
                formatter.format(number)
            }
            currency == "BRL" -> {
                val formatter = DecimalFormat("#,##0.00", symbols)
                formatter.format(number)
            }
            currency == "UYU" -> {
                val formatter = DecimalFormat("#,##0.00", symbols)
                formatter.format(number)
            }
            else -> {
                val formatter = DecimalFormat("#,##0.00", symbols)
                formatter.format(number)
            }
        }
    }
}