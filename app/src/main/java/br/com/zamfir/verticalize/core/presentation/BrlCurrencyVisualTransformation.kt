package br.com.zamfir.verticalize.core.presentation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Expects [text] to be a digit-only string representing a value in centavos
 * (e.g. "15000" -> "R$ 150,00") and only affects how it is displayed.
 */
class BrlCurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.ifEmpty { "0" }.padStart(3, '0')
        val cents = digits.takeLast(2)
        val reais = digits.dropLast(2).trimStart('0').ifEmpty { "0" }
        val grouped = reais.reversed().chunked(3).joinToString(".").reversed()
        val formatted = "R$ $grouped,$cents"

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = formatted.length
            override fun transformedToOriginal(offset: Int) = text.text.length
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
