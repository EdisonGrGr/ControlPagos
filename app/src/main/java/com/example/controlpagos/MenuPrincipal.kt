package com.example.controlpagos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.controlpagos.dao.CuentaDao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuPrincipal(cuentaDao: CuentaDao) {
    val cuentaRepository = remember(cuentaDao) { CuentaRepositoryImpl(cuentaDao) }
    val viewModel: PantallaPrincipalViewModel = viewModel(
        factory = remember(cuentaRepository) { PantallaPrincipalViewModelFactory(cuentaRepository) }
    )
    val uiState by viewModel.uiState.collectAsState()
    val listaCuentas = uiState.listaCuentas

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route ?: "inicio"

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = rutaActual == "inicio",
                    onClick = { navController.navigate("inicio") }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Agregar") },
                    label = { Text("Agregar") },
                    selected = rutaActual == "registrar",
                    onClick = { navController.navigate("registrar") }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Lista") },
                    label = { Text("Lista") },
                    selected = rutaActual == "lista",
                    onClick = { navController.navigate("lista") }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendario") },
                    label = { Text("Calendario") },
                    selected = rutaActual == "calendario",
                    onClick = { navController.navigate("calendario") }
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Control de Pagos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "inicio",
            modifier = Modifier.padding(padding)
        ) {
            composable("inicio") {
                PantallaInicio(listaCuentas)
            }

            composable("registrar") {
                PantallaPrincipal(viewModel)
            }

            composable("lista") {
                PantallaListaPagosScreen(viewModel)
            }

            composable("calendario") {
                CalendarioPagosScreen(listaCuentas)
            }
        }
    }
}

