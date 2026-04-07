# ControlPagos

Aplicacion Android para gestionar pagos, recordatorios, calendario y estadisticas.

## Contenido rapido
- Objetivo: registrar cuentas/pagos y dar seguimiento con notificaciones.
- Stack: Kotlin, Jetpack Compose, Room, Navigation Compose, WorkManager.
- Estado actual: Fase 1-6 implementadas (UI, arquitectura MVVM, estadisticas y CSV).

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

## Documentacion completa
- [Indice de docs](docs/README.md)
- [Arquitectura](docs/arquitectura.md)
- [Funcionalidades](docs/funcionalidades.md)
- [Fases](docs/fases.md)
- [Publicacion](docs/publicacion.md)
- [Pruebas](docs/pruebas.md)
- [Troubleshooting](docs/troubleshooting.md)

