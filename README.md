# FocusGate

Aplicación Android nativa (Kotlin + Jetpack Compose) que intercepta la apertura de Instagram
y exige completar un desafío de 10 preguntas de conocimiento general antes de dejar pasar.

## Estado de este entregable: compilado y funcionando

El proyecto está terminado y **ya compila limpio**: `./gradlew assembleDebug` corre sin errores
ni advertencias, produciendo `app-project/app/build/outputs/apk/debug/app-debug.apk`.

Nota técnica sobre el camino para llegar hasta acá: en este equipo, cualquier build de Gradle
(o cualquier programa Java que use `java.nio.channels.Selector`, usado internamente por Gradle
para la comunicación con su daemon) fallaba con `Unable to establish loopback connection`. La
causa real **no era un antivirus ni una restricción de seguridad**: era que la carpeta temporal
por defecto de Windows (`%TEMP%`, bajo la carpeta de usuario) generaba una ruta con la que la
implementación de sockets Unix domain de Windows (`afunix.sys`) no podía trabajar correctamente.
La solución fue simplemente apuntar las variables de entorno `TMP`/`TEMP` a una carpeta corta
(por ejemplo `C:\FocusGateTools\tmp`) antes de ejecutar Gradle — nada de software de seguridad
estaba bloqueando nada.

## Estructura del repositorio

```
FocusGate/
├── app-project/        <- Proyecto de Android Studio (ábrelo directamente aquí)
│   ├── app/
│   │   ├── src/main/java/com/focusgate/app/   <- Código Kotlin
│   │   ├── src/main/assets/questions/         <- Banco de 500 preguntas (JSON, offline)
│   │   └── src/main/res/                      <- Recursos (iconos, temas, strings)
│   └── build.gradle.kts, settings.gradle.kts, gradlew / gradlew.bat
└── README.md            <- Este archivo
```

## Cómo abrir el proyecto en Android Studio

1. Abre Android Studio → **Open** → selecciona la carpeta `app-project`.
2. Deja que Gradle sincronice (usará el SDK configurado en `app-project/local.properties`,
   o el que tengas instalado en tu propio equipo si lo abres en otra máquina).
3. Conecta un teléfono Android (con "Depuración USB" activada) o usa un emulador, y pulsa **Run**.

## Cómo instalar el APK en tu teléfono

El APK ya compilado se entrega junto a este proyecto (`FocusGate-debug.apk`); también puedes
regenerarlo en cualquier momento con `./gradlew assembleDebug`, y quedará en
`app-project/app/build/outputs/apk/debug/app-debug.apk`.

1. Copia `FocusGate-debug.apk` a tu teléfono.
2. En tu teléfono, activa "Instalar apps de orígenes desconocidos" para el administrador de
   archivos o la app que uses para abrir el APK (Android lo pedirá automáticamente).
3. Abre el archivo APK en el teléfono y confirma la instalación.
4. Abre FocusGate y sigue la pantalla de bienvenida para activar el permiso necesario.

## Permisos que pide la app y para qué

| Permiso | Obligatorio | Para qué se usa |
|---|---|---|
| **Servicio de accesibilidad** (`BIND_ACCESSIBILITY_SERVICE`) | Sí | Es el único mecanismo que Android ofrece a una app normal (sin root) para detectar, en el momento en que ocurre, que otra app pasó a primer plano. FocusGate lo usa exclusivamente para saber cuándo se abre `com.instagram.android`; **no lee el contenido de pantalla de ninguna app** (`canRetrieveWindowContent="false"` en la configuración del servicio). |
| **Acceso al uso de apps** (`PACKAGE_USAGE_STATS`) | No (opcional) | Solo alimenta la cifra de "tiempo de uso de Instagram" en la pantalla de Estadísticas. Si no se concede, el resto de la app —incluido el bloqueo— funciona igual. |

FocusGate **no pide** permiso de superposición (`SYSTEM_ALERT_WINDOW`), ni acceso a contactos,
almacenamiento, cámara, ubicación ni ningún otro permiso: no los necesita para nada de lo que
hace, y añadirlos violaría el principio de "ningún permiso innecesario" del proyecto.

## Cómo funciona el bloqueo (y sus límites reales en Android)

Android **no permite** a una app normal impedir literalmente que otra app dibuje su ventana;
ese nivel de control solo existe para quien controla el dispositivo (un "device owner" en modo
kiosco/empresarial, o con acceso root). Por eso FocusGate usa la alternativa estándar que sí
está disponible y es la que usan apps de bloqueo conocidas en Google Play:

1. `InstagramAccessibilityService` recibe del sistema un evento `TYPE_WINDOW_STATE_CHANGED`
   en el instante en que cualquier ventana nueva pasa a primer plano (no es una consulta
   periódica: Android lo entrega push, sin retraso de sondeo).
2. En cuanto ese evento dice `com.instagram.android` y el usuario no tiene ya un "pase" de
   desafío completado, el servicio lanza inmediatamente `BlockOverlayActivity` como una tarea
   nueva, que cubre toda la pantalla con el desafío de FocusGate.
3. Completar las 10 preguntas otorga el pase y relanza Instagram. Salir de Instagram hacia
   cualquier otra app (o al launcher) consume el pase: la próxima vez que se abra Instagram,
   vuelve a pedirse el desafío completo.

**Limitación documentada:** entre el paso 1 y el paso 2 hay un intervalo — normalmente muy
por debajo de medio segundo, pero no estrictamente cero — en el que Instagram puede llegar a
dibujar su primer fotograma antes de que la pantalla de FocusGate la cubra. Esto es inherente
a cómo Android entrega estos eventos a cualquier app sin privilegios de sistema; ninguna app de
bloqueo de terceros sin root puede eliminarlo del todo. Tampoco se intenta bloquear el botón
"Atrás" o "Inicio" del sistema (eso exigiría permisos de modo kiosco, mucho más invasivos de lo
que pide este proyecto); en la práctica esto no es una fuga real, porque salir de la pantalla de
desafío sin completarla simplemente deja el pase sin otorgar, así que Instagram vuelve a quedar
cubierto por el desafío la próxima vez que esté en primer plano.

## Banco de preguntas

500 preguntas (50 por categoría × 10 categorías), repartidas en dificultad fácil/intermedia/
difícil, como archivos JSON en `app/src/main/assets/questions/`. Se cargan una sola vez en una
base de datos Room la primera vez que arranca la app — no se necesita conexión a internet nunca.
Para añadir más preguntas basta con editar esos JSON (o insertarlas directamente en Room) con el
mismo esquema: `id`, `category`, `difficulty`, `language`, `question`, `options` (4), `correctIndex`,
`explanation`.

## Arquitectura

Kotlin + Jetpack Compose + Material 3, MVVM, Room (preguntas y estadísticas), DataStore
(preferencias), Coroutines/Flow, Navigation Compose. Sin frameworks de inyección de
dependencias: un contenedor manual (`AppContainer`) construye el grafo de objetos una vez en
`FocusGateApplication`.
