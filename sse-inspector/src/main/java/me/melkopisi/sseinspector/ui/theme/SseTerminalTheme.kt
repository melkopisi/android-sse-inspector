package me.melkopisi.sseinspector.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = TerminalColors.CancelledCyan,
    onPrimary = Color.Black,
    secondary = TerminalColors.LiveGreen,
    onSecondary = Color.Black,
    background = TerminalColors.Background,
    onBackground = TerminalColors.TextPrimary,
    surface = TerminalColors.Surface,
    onSurface = TerminalColors.TextPrimary,
    surfaceVariant = TerminalColors.SurfaceVariant,
    onSurfaceVariant = TerminalColors.TextSecondary,
    outline = TerminalColors.Border
)

@Composable
fun SseTerminalTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            content = content
        )
    }
}
