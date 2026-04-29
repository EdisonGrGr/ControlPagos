# ControlPagos

Aplicación Android para gestionar pagos, recordatorios, calendario y estadísticas.

## Contenido rápido
- Objetivo: registrar cuentas/pagos y dar seguimiento con notificaciones.
- Stack: Kotlin, Jetpack Compose, Room, Navigation Compose, WorkManager.
- Estado actual: Fase 1-6 implementadas (UI, arquitectura MVVM, estadísticas y CSV).

## Cambios Recientes (Sprint Visual + Estabilidad)

### 🎨 Mejoras Visuales
- **Paleta de colores mejorada**: Colores más vibrantes y saturados para mejor contraste visual.
  - Azul principal: `0xFF246BFF` (más brillante)
  - Teal secundario: `0xFF00A8A8` (más saturado)
  - Coral terciario: `0xFFFF6B5A` (más llamativo)
- **Fondos degradados**: Implementación de gradientes verticales suaves en:
  - `MenuPrincipal.kt`: Degradado principal del app
  - `PantallaInicio.kt`: Dashboard de inicio
  - `PantallaPrincipal.kt`: Formulario de pagos
  - `PantallaIngresos.kt`: Formulario de ingresos
- **Colores de contenedor**: Nuevos colores para contenedores y superficies (primaryContainer, secondaryContainer, tertiaryContainer, errorContainer).

### 🔧 Correcciones de Funcionalidad
- **Date Picker Fix**: Reparación del selector de fecha que no respondía a clicks
  - Migración a `LaunchedEffect` para manejo correcto de efectos secundarios
  - Reemplazo de `.clickable` modifier con `IconButton` en trailingIcon para mayor confiabilidad
  - Aplicado a `PantallaPrincipal.kt` y `PantallaIngresos.kt`

### 🗑️ Limpieza de Código
- **Pruebas y previews eliminadas**:
  - Eliminadas funciones @Preview de `PantallaPrincipal.kt`
  - Eliminados datos de prueba (muestraCuentaEjemplo)
  - Deletados archivos de prueba: `ExampleUnitTest.kt`, `ExampleInstrumentedTest.kt`
  - Deletado wrapper innecesario: `CalendarioPagosScreen.kt`
- **Código muerto removido**:
  - Función `formatearFechaDesdeMillisUtc()` de `FormatUtils.kt`
  - Estado `mostrarCalendario` de `PantallaPrincipalViewModel.kt`
  - Métodos huérfanos `abrirCalendario()` y `cerrarCalendario()`

## Requisitos
- Android Studio reciente.
- JDK 11.
- SDK `compileSdk = 35`.

## Ejecutar en local
```powershell
cd C:\Users\User\AndroidStudioProjects\ControlPagos
.\gradlew.bat :app:assembleDebug
```

Para abrir en emulador/dispositivo, usar **Run** en Android Studio.

## Pruebas
```powershell
cd C:\Users\User\AndroidStudioProjects\ControlPagos
.\gradlew.bat :app:testDebugUnitTest
```

## Estructura principal
- `app/src/main/java/com/example/controlpagos/`
  - `MainActivity.kt`
  - `MenuPrincipal.kt`
  - `PantallaPrincipal.kt`
  - `PantallaInicio.kt`
  - `PantallaListaPagos.kt`
  - `CalendarioPagos.kt`
  - `CalendarioPagosScreen.kt`
  - `PantallaPrincipalViewModel.kt`
  - `CuentaRepository.kt`
  - `CsvUtils.kt`
  - `EstadisticasPagos.kt`
  - `FormatUtils.kt`
- `app/src/main/java/dao/`
- `app/src/main/java/database/`
- `app/src/main/java/model/`

## Detalles Técnicos de Cambios Recientes

### Archivos Modificados - UI/Visual
- `ui/theme/Color.kt`: Actualización de paleta de colores (AzulPrincipal40, TealSecundario40, CoralTercero40, colores de contenedor)
- `ui/theme/Theme.kt`: Integración de colores actualizados en lightColorScheme y darkColorScheme

### Archivos Modificados - Pantallas
- `MenuPrincipal.kt`: Agregado Box con Brush.verticalGradient, TopAppBar con primaryContainer
- `PantallaInicio.kt`: Agregado degradado vertical a LazyColumn
- `PantallaPrincipal.kt`: 
  - Agregado degradado vertical
  - Refactorizado date picker con LaunchedEffect
  - Reemplazado clickable con IconButton en campo fecha
  - Eliminadas funciones @Preview y datos de prueba
- `PantallaIngresos.kt`:
  - Agregado degradado vertical
  - Refactorizado date picker con LaunchedEffect
  - Reemplazado clickable con IconButton en campo fecha

### Archivos Modificados - ViewModel/Utils
- `PantallaPrincipalViewModel.kt`: Removido estado mostrarCalendario y métodos abrirCalendario/cerrarCalendario
- `FormatUtils.kt`: Removida función formatearFechaDesdeMillisUtc

### Archivos Eliminados
- `CalendarioPagosScreen.kt` (wrapper de prueba)
- `app/src/test/java/com/example/controlpagos/ExampleUnitTest.kt`
- `app/src/androidTest/java/com/example/controlpagos/ExampleInstrumentedTest.kt`

## Documentación completa
- [Índice de docs](docs/README.md)
- [Arquitectura](docs/arquitectura.md)
- [Funcionalidades](docs/funcionalidades.md)
- [Fases](docs/fases.md)
- [Publicación](docs/publicacion.md)
- [Pruebas](docs/pruebas.md)
- [Troubleshooting](docs/troubleshooting.md)

