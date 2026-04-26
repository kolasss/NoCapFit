package com.example.nocapfit.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun CompactInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    var editing by remember { mutableStateOf(false) }

    if (editing) {
        CompactInputEditor(
            value = value,
            onValueChange = onValueChange,
            onDismiss = { editing = false },
            modifier = modifier,
            keyboardType = keyboardType,
            textStyle = textStyle,
            visualTransformation = visualTransformation
        )
    } else {
        val displayText = remember(value, visualTransformation) {
            if (value.isEmpty()) {
                ""
            } else {
                visualTransformation.filter(AnnotatedString(value)).text.text
            }
        }
        Box(
            modifier = modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.extraSmall
                )
                .clickable { editing = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = displayText,
                style = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CompactInputEditor(
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val focusRequester = remember { FocusRequester() }
    var hasFocusedOnce by remember { mutableStateOf(false) }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(value, selection = TextRange(0, value.length)))
    }
    if (textFieldValue.text != value) {
        textFieldValue = textFieldValue.copy(text = value)
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    BasicTextField(
        value = textFieldValue,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        onValueChange = { newValue ->
            textFieldValue = newValue
            onValueChange(newValue.text)
        },
        singleLine = true,
        textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused) {
                    hasFocusedOnce = true
                } else if (hasFocusedOnce) {
                    onDismiss()
                }
            },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                innerTextField()
            }
        }
    )
}
