package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElectricAqua,
    secondary = SeafoamGlow,
    tertiary = BiolumAmber,
    background = DeepOceanBlack,
    surface = AbyssalNavy,
    error = DangerCrimson
)

@Composable
fun OceanDeepEscapeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
