# Troubleshooting

## 1) Error de sobrecarga duplicada en IDE
Si Android Studio muestra funciones duplicadas pero Gradle compila:
- Hacer `File > Invalidate Caches / Restart`.
- Sincronizar Gradle de nuevo.

## 2) Kapt warning version 2.0+
Mensaje conocido: fallback de kapt a 1.9.
- Mientras compile y tests pasen, no bloquea release.

## 3) CSV no importa
Verificar:
- Header opcional correcto: `nombre,numeroCuenta,monto,fecha`.
- Monto > 0.
- Fecha valida (`dd/MM/yyyy`).

## 4) Notificaciones no aparecen
Verificar:
- Permiso `POST_NOTIFICATIONS` concedido (Android 13+).
- Fecha de pago valida y futura.
- WorkManager activo en el dispositivo.

## 5) Build local
Comando base:
```powershell
cd C:\Users\User\AndroidStudioProjects\ControlPagos
.\gradlew.bat :app:testDebugUnitTest :app:compileDebugKotlin
```

