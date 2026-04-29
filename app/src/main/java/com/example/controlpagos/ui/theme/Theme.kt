package com.example.controlpagos.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AzulClaro80,
    secondary = TealClaro80,
    tertiary = CoralClaro80,
    background = FondoOscuro,
    surface = SuperficieOscuro,
    surfaceVariant = Color(0xFF24304A),
    primaryContainer = Color(0xFF1C3F8F),
    secondaryContainer = Color(0xFF0D5E66),
    tertiaryContainer = Color(0xFF7F322C),
    errorContainer = Color(0xFF7B1D25)
)

private val LightColorScheme = lightColorScheme(
    primary = AzulPrincipal40,
    secondary = TealSecundario40,
    tertiary = CoralTercero40,
    background = FondoClaro,
    surface = SuperficieClaro,
    surfaceVariant = FondoClaroSecundario,
    primaryContainer = ContenedorPrimarioClaro,
    secondaryContainer = ContenedorSecundarioClaro,
    tertiaryContainer = ContenedorTerciarioClaro,
    errorContainer = Color(0xFFFFDAD6)
)

@Composable
fun ControlPagosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}