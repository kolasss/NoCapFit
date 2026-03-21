package com.example.nocapfit.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Converts raw digit string to mm:ss display.
 * "130" → "1:30", "634" → "6:34", "60" → "0:60" (= 60 seconds)
 */
class MmSsVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val display = when {
            digits.length <= 2 -> "0:${digits.padStart(2, '0')}"
            else -> {
                val seconds = digits.takeLast(2)
                val minutes = digits.dropLast(2)
                "$minutes:$seconds"
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return display.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return digits.length
            }
        }

        return TransformedText(AnnotatedString(display), offsetMapping)
    }
}
