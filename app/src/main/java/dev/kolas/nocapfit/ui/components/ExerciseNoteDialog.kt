package dev.kolas.nocapfit.ui.components

import androidx.compose.runtime.Composable

@Composable
fun ExerciseNoteDialog(
    initialValue: String?,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    InputDialog(
        title = "Note",
        initialValue = initialValue.orEmpty(),
        label = "Note",
        singleLine = false,
        allowEmpty = true,
        onConfirm = { value -> onConfirm(value.ifBlank { null }) },
        onDismiss = onDismiss
    )
}
