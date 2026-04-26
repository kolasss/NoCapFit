package dev.kolas.nocapfit.ui.components

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

        val offsetMapping = if (digits.length <= 2) {
            val prefixLen = display.length - digits.length
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = when {
                    digits.isEmpty() -> display.length
                    offset == 0 -> 0
                    else -> offset + prefixLen
                }

                override fun transformedToOriginal(offset: Int): Int =
                    if (offset <= prefixLen) {
                        0
                    } else {
                        (offset - prefixLen).coerceAtMost(digits.length)
                    }
            }
        } else {
            val minutesLen = digits.length - 2
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int =
                    if (offset < minutesLen) offset else offset + 1

                override fun transformedToOriginal(offset: Int): Int =
                    if (offset <= minutesLen) {
                        offset
                    } else {
                        (offset - 1).coerceAtMost(digits.length)
                    }
            }
        }

        return TransformedText(AnnotatedString(display), offsetMapping)
    }
}
