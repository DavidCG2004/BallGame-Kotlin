# Gravity Ball Kotlin

Juego móvil en **Kotlin** que utiliza el **acelerómetro** del dispositivo para controlar una pelota a través de un campo con obstáculos y objetivos. Desarrollado como proyecto taller para la clase de sensores en Android.

## 🎮 Gameplay

- Inclina el celular para mover la bolita por la pantalla.
- Atrapa los **objetivos verdes** para sumar **+10 puntos**.
- Evita los **obstáculos rojos** — cada choque te cuesta una vida.
- Tienes **3 vidas**. Al perderlas todas, el juego termina.
- Los obstáculos cambian de posición y tamaño **aleatoriamente** en cada partida.

## 🖥️ Pantallas

| Pantalla | Descripción |
|---|---|
| **Inicio** | Menú con vista previa de la bolita, selector de color (4 colores), selector de tamaño (Chica/Media/Grande) y botón **Comenzar** |
| **Cuenta regresiva** | 3… 2… 1… ¡YA! — overlay semitransparente antes de iniciar |
| **Juego** | Pelota controlada por sensores, objetivos, obstáculos, puntaje y vidas en tiempo real |
| **Game Over** | Muestra puntaje final con botón **Reiniciar** |

## 🛠️ Tecnologías

- **Lenguaje:** Kotlin
- **Plugin Android:** 8.7.3
- **Plugin Kotlin:** 2.0.21
- **compileSdk / targetSdk:** 35
- **minSdk:** 24
- **Java:** 17
- **AndroidX:** Habilitado
- **Dependencias externas:** `androidx.core:core-splashscreen:1.0.1`

## Capturas del Juego
<img width="600" alt="WhatsApp Image 2026-07-03 at 4 33 38 PM" src="https://github.com/user-attachments/assets/1de1d87c-fde7-4934-a71a-76838ff69864" />
<img width="600" alt="WhatsApp Image 2026-07-03 at 4 33 39 PM" src="https://github.com/user-attachments/assets/78398c2e-7bdc-419a-940d-308762d3b16f" />


## 📁 Estructura del proyecto

```
app/src/main/java/com/epn/gravitygame/
├── MainActivity.kt       — Activity principal, configura sensores y SplashScreen
├── GameView.kt           — Vista personalizada: renderizado, UI, lógica del juego
├── Ball.kt               — Clase de la pelota: posición, física, dibujo
├── Target.kt             — Objetivo verde que se recolecta
├── Obstacle.kt           — Obstáculo rojo rectangular
├── Collision.kt          — Utilidades de detección de colisiones
└── Vector2.kt            — Vector 2D para posiciones

app/src/main/res/
├── drawable/
│   ├── ic_launcher_foreground.xml   — Vector de la pelota 3D (icono)
│   └── ic_launcher_background.xml   — Fondo del icono
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml              — Adaptive icon (API 26+)
│   └── ic_launcher_round.xml
├── mipmap-anydpi/
│   ├── ic_launcher.xml              — Fallback vector (API 24-25)
│   └── ic_launcher_round.xml
└── values/
    ├── colors.xml
    ├── strings.xml
    └── styles.xml                   — Tema AppTheme + Theme.Splash
```

## ✨ Características

- **Splash Screen:** Pantalla de bienvenida con icono de la app usando `core-splashscreen`
- **Personalización:** 4 colores y 3 tamaños de bolita seleccionables antes de jugar
- **Obstáculos aleatorios:** Posición, ancho y alto variables; evitan la zona central de aparición
- **Cuenta regresiva:** 3… 2… 1… ¡YA! antes de iniciar la partida
- **Feedback háptico:** Vibración al chocar con bordes, obstáculos y al atrapar objetivos
- **Flash visual:** Borde rojo al chocar con los límites de la pantalla
- **Sin XML de layout:** Todo el renderizado es mediante Canvas

## 🚀 Cómo compilar

### Con Android Studio

1. Abre Android Studio.
2. **File > Open** y selecciona la carpeta del proyecto.
3. Espera a que Gradle sincronice.
4. Conecta un dispositivo Android con depuración USB.
5. Presiona **Run**.

### Línea de comandos (APK debug)

```bash
cd "C:\Users\APP MOVILES\Desktop\KotlinGravityGame_StudioOnly"
./gradlew assembleDebug
```

El APK se genera en:

```bash
app/build/outputs/apk/debug/app-debug.apk
```

## 📋 Permisos

- **`android.hardware.sensor.accelerometer`** (requerido) — El juego necesita acelerómetro.
- **`android.permission.VIBRATE`** — Para feedback háptico.

## 📐 API de Splash Screen

La app usa `androidx.core:core-splashscreen:1.0.1` para mostrar una pantalla de presentación:

1. `installSplashScreen()` se invoca antes de `super.onCreate()` en `MainActivity`.
2. El tema `Theme.Splash` en `styles.xml` configura fondo, icono animado y tema posterior.
3. El `AndroidManifest.xml` usa `Theme.Splash` tanto en `<application>` como en `<activity>`.

## 🎨 Personalización del icono

El icono es un **adaptive icon** (API 26+) con fallback vectorial (API 24-25):

- **Foreground:** Vector drawable de una pelota 3D con sombra, gradiente radial y brillos.
- **Background:** Fondo sólido `#F8FAFC`.
- Los archivos PNG en `mipmap-*` se reemplazaron por vectores XML.

## 📝 Notas

- La orientación es **vertical** (`portrait`) forzada en el manifiesto.
- No se usa motor de físicas externo — la detección de colisiones es propia (`Collision.kt`).
- El bucle del juego está basado en eventos del sensor, no en un hilo dedicado.
