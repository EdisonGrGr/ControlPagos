# Publicacion

## Android (Google Play)
Este proyecto es Android nativo. El canal de publicacion es Google Play Console.

## Checklist release
1. Incrementar `versionCode` y `versionName` en `app/build.gradle.kts`.
2. Configurar firma release (keystore).
3. Generar `.aab` firmado.
4. Completar ficha en Play Console:
   - descripcion
   - capturas
   - icono
   - clasificacion de contenido
   - data safety
5. Subir release y enviar a revision.

## Comando util
```powershell
cd C:\Users\User\AndroidStudioProjects\ControlPagos
.\gradlew.bat :app:bundleRelease
```

## Nota App Store (Apple)
No aplica directo a este codigo Android. Para App Store se requiere una app iOS.

