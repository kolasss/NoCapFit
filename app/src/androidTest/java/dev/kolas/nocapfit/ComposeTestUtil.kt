package dev.kolas.nocapfit

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import dev.kolas.nocapfit.ui.theme.NoCapFitTheme

fun ComposeContentTestRule.setThemedContent(content: @Composable () -> Unit) {
    setContent {
        NoCapFitTheme(dynamicColor = false) {
            content()
        }
    }
}
