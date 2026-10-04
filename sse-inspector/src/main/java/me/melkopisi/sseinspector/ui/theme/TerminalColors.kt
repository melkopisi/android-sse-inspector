package me.melkopisi.sseinspector.ui.theme

import androidx.compose.ui.graphics.Color

@Suppress("MagicNumber")
object TerminalColors {
    val Background = Color(0xFF0D1117)
    val Surface = Color(0xFF161B22)
    val SurfaceVariant = Color(0xFF21262D)
    val Border = Color(0xFF30363D)
    val TextPrimary = Color(0xFFE6EDF3)
    val TextSecondary = Color(0xFF8B949E)

    // Status colors
    val LiveGreen = Color(0xFF3FB950)
    val ConnectingAmber = Color(0xFFD29922)
    val CancelledCyan = Color(0xFF58A6FF)
    val ClosedGray = Color(0xFF8B949E)
    val FailedRed = Color(0xFFF85149)
    val EventPurple = Color(0xFFA371F7)

    // JSON Syntax Highlighting colors
    val JsonKey = Color(0xFF79C0FF)
    val JsonString = Color(0xFF7EE787)
    val JsonNumber = Color(0xFFFFA657)
    val JsonBoolean = Color(0xFFD2A8FF)
    val JsonNull = Color(0xFF8B949E)
    val JsonPunctuation = Color(0xFFC9D1D9)
}
