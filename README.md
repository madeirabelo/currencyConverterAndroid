# Currency Converter Android App

A simple and intuitive currency converter Android application built with Jetpack Compose. The app supports conversion between USD, EUR, ARS, and PGY currencies.

## Features

- Convert between USD, EUR, ARS, and PGY currencies
- Set custom exchange rates against USD
- Modern Material Design UI with Jetpack Compose
- Real-time currency conversion

## Technical Stack

- Kotlin
- Jetpack Compose for UI
- ViewModel for state management
- Material Design components

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync the project with Gradle files
4. Run the app on an emulator or physical device

## Usage

1. Enter the amount you want to convert
2. Select the source currency from the dropdown
3. Select the target currency from the dropdown
4. Click 'Convert' to see the result
5. Use the exchange rate section to set custom rates against USD

## Requirements

- Android Studio Arctic Fox or newer
- Android SDK 24 or higher
- Kotlin 1.8.10 or higher

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/currencyconverter/
│   │   ├── ApiService.kt          # API service for currency data
│   │   ├── CurrencyRepository.kt  # Repository pattern implementation
│   │   ├── CurrencyViewModel.kt   # ViewModel for UI state management
│   │   └── MainActivity.kt        # Main activity with Compose UI
│   └── res/                       # Resources (layouts, strings, etc.)
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.