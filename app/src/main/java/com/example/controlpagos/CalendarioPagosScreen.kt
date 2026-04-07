package com.example.controlpagos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.controlpagos.model.Cuenta

@Composable
fun CalendarioPagosScreen(listaCuentas: List<Cuenta>) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Calendario de Pagos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalendarioPagos(listaCuentas)
    }
}

