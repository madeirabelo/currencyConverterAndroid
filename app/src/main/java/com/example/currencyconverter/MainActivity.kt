package com.example.currencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colors.background) {
                    CurrencyConverterScreen()
                }
            }
        }
    }
}

@Composable
fun CurrencyConverterScreen(viewModel: CurrencyViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Currency Converter",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Enter value in any currency",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Currency Input Fields
                CurrencyInputField("USD", "US Dollar", viewModel)
                Spacer(modifier = Modifier.height(12.dp))
                CurrencyInputField("EUR", "Euro", viewModel)
                Spacer(modifier = Modifier.height(12.dp))
                CurrencyInputField("ARS", "Argentine Peso", viewModel)
                Spacer(modifier = Modifier.height(12.dp))
                CurrencyInputField("PYG", "Paraguayan Guaraní", viewModel)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exchange Rates (USD Base)",
                        style = MaterialTheme.typography.h6
                    )
                    
                    Button(
                        onClick = { viewModel.fetchLatestRates() },
                        enabled = !viewModel.isLoading.value
                    ) {
                        if (viewModel.isLoading.value) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Update Rates")
                        }
                    }
                }
                
                if (viewModel.lastUpdateTime.value.isNotEmpty()) {
                    Text(
                        text = viewModel.lastUpdateTime.value,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ExchangeRateInput("EUR", viewModel)
                ExchangeRateInput("ARS", viewModel)
                ExchangeRateInput("PYG", viewModel)
            }
        }
        
        // Add some bottom padding to ensure all content is accessible
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CurrencyInputField(
    currency: String,
    currencyName: String,
    viewModel: CurrencyViewModel
) {
    val currencyValue = viewModel.currencyValues.value[currency] ?: CurrencyValue(currency, "")
    
    OutlinedTextField(
        value = currencyValue.value,
        onValueChange = { newValue ->
            viewModel.updateCurrencyValue(currency, newValue)
        },
        label = { Text("$currencyName ($currency)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = if (currencyValue.isUserInput) 
                MaterialTheme.colors.primary 
            else 
                MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
            unfocusedBorderColor = if (currencyValue.isUserInput) 
                MaterialTheme.colors.primary.copy(alpha = 0.5f) 
            else 
                MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
        )
    )
    
    if (currencyValue.isUserInput && currencyValue.value.isNotEmpty()) {
        Text(
            text = "You entered this value",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ExchangeRateInput(currency: String, viewModel: CurrencyViewModel) {
    val fetchedRates = viewModel.fetchedRates.value
    var rate by remember { mutableStateOf(fetchedRates[currency] ?: "") }
    var showSuccess by remember { mutableStateOf(false) }

    // Update rate when fetched rates change
    LaunchedEffect(fetchedRates[currency]) {
        rate = fetchedRates[currency] ?: ""
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("1 USD = ", modifier = Modifier.padding(end = 8.dp))
        
        OutlinedTextField(
            value = rate,
            onValueChange = { 
                rate = it
                showSuccess = false
            },
            label = { Text(currency) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = if (fetchedRates[currency]?.isNotEmpty() == true) 
                    MaterialTheme.colors.primary 
                else 
                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                unfocusedBorderColor = if (fetchedRates[currency]?.isNotEmpty() == true) 
                    MaterialTheme.colors.primary.copy(alpha = 0.5f) 
                else 
                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            )
        )

        Button(
            onClick = {
                val rateValue = rate.toDoubleOrNull()
                if (rateValue != null && rateValue > 0) {
                    viewModel.setExchangeRate(currency, rateValue)
                    showSuccess = true
                }
            },
            modifier = Modifier.padding(start = 8.dp),
            enabled = rate.isNotEmpty()
        ) {
            Text("Set")
        }
    }
    
    if (fetchedRates[currency]?.isNotEmpty() == true) {
        Text(
            text = "Rate from API",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
    
    if (showSuccess) {
        Text(
            text = "Rate updated successfully!",
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}