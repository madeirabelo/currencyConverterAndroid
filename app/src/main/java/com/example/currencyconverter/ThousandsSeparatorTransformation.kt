package com.example.currencyconverter

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class ThousandsSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (text.text.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.groupingSeparator = ' '
        val formatter = DecimalFormat("#,##0.##", symbols)

        val originalText = text.text
        val parsed = originalText.replace(" ", "").toDoubleOrNull()

        if (parsed == null) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formattedString = formatter.format(parsed)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val nonSeparators = originalText.substring(0, offset).count { it != ' ' }
                val transformed = formatter.format(originalText.substring(0, offset).replace(" ", "").toDoubleOrNull() ?: 0.0)
                val separators = transformed.count { it == ' ' }
                return nonSeparators + separators
            }

            override fun transformedToOriginal(offset: Int): Int {
                val nonSeparators = formattedString.substring(0, offset).count { it != ' ' }
                val separators = formattedString.substring(0, offset).count { it == ' ' }
                return nonSeparators - separators
            }
        }

        return TransformedText(AnnotatedString(formattedString), offsetMapping)
    }
}