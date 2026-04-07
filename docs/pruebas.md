# Pruebas

## Unit tests actuales
Ubicacion: `app/src/test/java/com/example/controlpagos/ExampleUnitTest.kt`

Cobertura principal:
- Calculo de totales por periodo (3/6/12 meses).
- Tendencia mensual.
- Parseo/export/import CSV.

## Ejecutar pruebas
```powershell
cd C:\Users\User\AndroidStudioProjects\ControlPagos
.\gradlew.bat :app:testDebugUnitTest
```

## Compilacion de verificacion
```powershell
cd C:\Users\User\AndroidStudioProjects\ControlPagos
.\gradlew.bat :app:compileDebugKotlin
```

## Recomendaciones siguientes
- Agregar tests de ViewModel (`StateFlow` + eventos).
- Agregar tests de RecordatorioHelper (fechas limite).
- Agregar tests instrumentados de UI critica (flujo alta/edicion/eliminacion).

