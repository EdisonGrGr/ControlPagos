package com.example.controlpagos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.controlpagos.dao.CuentaDao
import com.example.controlpagos.dao.IngresoDao
import com.example.controlpagos.ui.theme.FondoClaroSecundario
import com.example.controlpagos.ui.theme.FondoClaroTerciario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuPrincipal(cuentaDao: CuentaDao, ingresoDao: IngresoDao) {
    val cuentaRepository = remember(cuentaDao) { CuentaRepositoryImpl(cuentaDao) }
    val viewModel: PantallaPrincipalViewModel = viewModel(
        factory = remember(cuentaRepository) { PantallaPrincipalViewModelFactory(cuentaRepository) }
    )
    val uiState by viewModel.uiState.collectAsState()
    val listaCuentas = uiState.listaCuentas

    val ingresoRepository = remember(ingresoDao) { IngresoRepositoryImpl(ingresoDao) }
    val ingresoViewModel: IngresoViewModel = viewModel(
        factory = remember(ingresoRepository) { IngresoViewModelFactory(ingresoRepository) }
    )
    val ingresoUiState by ingresoViewModel.uiState.collectAsState()
    val listaIngresos = ingresoUiState.listaIngresos

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route ?: "inicio"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        FondoClaroSecundario,
                        FondoClaroTerciario
                    )
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    tonalElevation = 8.dp
                ) {
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
                        icon = { Icon(Icons.Default.Add, contentDescription = "Ingresos") },
                        label = { Text("Ingresos") },
                        selected = rutaActual == "ingresos",
                        onClick = { navController.navigate("ingresos") }
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                    PantallaInicio(listaCuentas, listaIngresos)
                }

                composable("registrar") {
                    PantallaPrincipal(viewModel)
                }

                composable("lista") {
                    PantallaListaPagosScreen(viewModel)
                }

                composable("ingresos") {
                    PantallaIngresos(ingresoViewModel)
                }

                composable("calendario") {
                    CalendarioPagos(listaCuentas)
                }
            }
        }
    }
}

