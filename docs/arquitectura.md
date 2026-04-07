# Arquitectura

## Resumen
La app sigue una arquitectura MVVM simplificada con capas:
- UI Compose
- ViewModel con `StateFlow`
- Repositorio
- DAO/Room

## Flujo de datos
1. UI envia eventos al `PantallaPrincipalViewModel`.
2. ViewModel actualiza estado (`uiState`) y ejecuta operaciones.
3. ViewModel usa `CuentaRepository`.
4. Repositorio usa `CuentaDao` (Room).
5. UI observa `uiState` via `collectAsState()`.

## Componentes clave
- `MainActivity.kt`: crea DB y arranca `MenuPrincipal`.
- `MenuPrincipal.kt`: navegacion y ViewModel compartido.
- `PantallaPrincipalViewModel.kt`: estado global de cuentas y formularios.
- `CuentaRepository.kt`: abstraccion de datos.
- `dao/CuentaDao.kt`: consultas Room.
- `database/AppDatabase.kt`: definicion de DB.

## Notificaciones
- `RecordatorioHelper.kt`: programa/cancela recordatorios con WorkManager.
- `ReminderWorker.kt`: construye notificacion al ejecutarse.

## Formato y utilidades
- `FormatUtils.kt`: parseo/formato de fecha y monto.
- `CsvUtils.kt`: exportacion/importacion CSV.

