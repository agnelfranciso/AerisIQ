package com.example.aerisiq.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val ExpressiveDarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = PrimaryBlueVariant,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceTranslucent,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant
)

val ExpressiveShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(24.dp), // Deeply rounded cards
    large = RoundedCornerShape(32.dp)
)

@Composable
fun AerisIQTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = ExpressiveDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}
