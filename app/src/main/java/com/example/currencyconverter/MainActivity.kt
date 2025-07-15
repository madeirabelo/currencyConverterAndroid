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

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

import android.content.res.Configuration
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colors.background) {
                    CurrencyConverterScreen(onLanguageSelected = { language ->
                        setLocale(language)
                    })
                }
            }
        }
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }
}

@Composable
fun CurrencyConverterScreen(viewModel: CurrencyViewModel = viewModel(), onLanguageSelected: (String) -> Unit) {
    val scrollState = rememberScrollState()
    val currentLocale = remember { mutableStateOf(Locale.getDefault().language) }

    LaunchedEffect(Unit) {
        currentLocale.value = Locale.getDefault().language
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Main content of the currency converter

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.enter_value_in_any_currency),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Currency Input Fields
                    CurrencyInputField("USD", stringResource(id = R.string.us_dollar), viewModel)
                    Spacer(modifier = Modifier.height(12.dp))
                    CurrencyInputField("EUR", stringResource(id = R.string.euro), viewModel)
                    Spacer(modifier = Modifier.height(12.dp))
                    CurrencyInputField("ARS", stringResource(id = R.string.argentine_peso), viewModel)
                    Spacer(modifier = Modifier.height(12.dp))
                    CurrencyInputField("PYG", stringResource(id = R.string.paraguayan_guarani), viewModel)
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.exchange_rates_usd_base),
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.fetchLatestRates() },
                            enabled = !viewModel.isLoading.value,
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            if (viewModel.isLoading.value) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(stringResource(id = R.string.update_rates))
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageSwitcher(onLanguageSelected = { language ->
                    onLanguageSelected(language)
                    currentLocale.value = language
                }, currentLocale = currentLocale.value)
            }
        }
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
            text = stringResource(id = R.string.you_entered_this_value),
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
        Text(stringResource(id = R.string.one_usd_equals), modifier = Modifier.padding(end = 8.dp))
        
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
            Text(stringResource(id = R.string.set))
        }
    }
    
    if (fetchedRates[currency]?.isNotEmpty() == true) {
        Text(
            text = stringResource(id = R.string.rate_from_api),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
    
    if (showSuccess) {
        Text(
            text = stringResource(id = R.string.rate_updated_successfully),
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun LanguageSwitcher(onLanguageSelected: (String) -> Unit, currentLocale: String) {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf("en" to "EN", "pt" to "PT", "es" to "ES", "fr" to "FR", "de" to "DE")

    var selectedDisplayCode by remember { mutableStateOf(items.firstOrNull { it.first == currentLocale }?.second ?: "") }

    Box(modifier = Modifier
        .wrapContentSize(Alignment.TopEnd)
        .padding(16.dp)
    ) {
        Button(onClick = { expanded = true }, modifier = Modifier.width(72.dp)) {
            Text(selectedDisplayCode)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { (code, name) ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    selectedDisplayCode = name
                    onLanguageSelected(code)
                }) {
                    Text(text = name, style = MaterialTheme.typography.body2)
                }
            }
        }
    }
}