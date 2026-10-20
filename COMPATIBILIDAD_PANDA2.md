# Abrir las prácticas con Android Studio Panda 2

## Qué cambia en esta rama

La rama `Version_panda2` adapta las tres prácticas; `main` conserva la configuración original.

| Configuración | main | Version_panda2 |
|---|---|---|
| Android Gradle Plugin (AGP) | 9.3.3 | 9.1.1 |
| Gradle Wrapper | 9.5.0 | 9.3.1, con SHA-256 oficial |
| compileSdk | 37 | 36.1 |
| targetSdk | 37 | 36 |
| minSdk | 34 | 34, sin cambios |
| JDK del daemon | Descarga obligatoria de Java 25 | JDK configurado en Android Studio o JAVA_HOME |
| Nivel del código Java | 11 | 11, sin cambios |

AGP 9.1 requiere JDK 17 o superior compatible con Gradle. Usa preferentemente el JDK 21 integrado de Panda 2; no cambies a Java 8 u 11 para ejecutar Gradle. El nivel Java 11 del código fuente es distinto del JDK que ejecuta la compilación.

Se conserva el código Java/XML y las versiones de las dependencias. No se reemplazan Room, SharedPreferences, los archivos JSON, las notificaciones, los formularios ni las listas. El ajuste de targetSdk cambia la versión de comportamiento de Android declarada por la aplicación, por lo que conviene volver a probarla en el celular.

## Paso a paso en el laboratorio

1. Descarga o clona el repositorio. Si usas Git, selecciona la rama `Version_panda2` desde el selector de ramas de Android Studio, o ejecuta `git switch Version_panda2` después de `git fetch origin`. Si descargas un ZIP desde GitHub, selecciona primero esa rama.
2. Abre Android Studio Panda 2. En **Help > About**, comprueba que diga Panda 2 / 2025.3.2.
3. Desde **File > Open**, abre solo `Practica1`. Después podrás abrir `Practica2` y `Practica3` en ventanas independientes. No abras Lab5 como un único proyecto Gradle.
4. En **Tools > SDK Manager > SDK Platforms**, activa **Show Package Details**, despliega Android 16 / Baklava y selecciona **Android SDK Platform 36.1**. No basta con tener solo Platform 36 o 37.
5. En **SDK Tools**, con **Show Package Details**, instala **Android SDK Build-Tools 36.0.0**. Confirma con **Apply**. Conserva Platform-Tools y Emulator si ya están instalados.
6. En **File > Settings > Build, Execution, Deployment > Build Tools > Gradle**, elige el **Gradle JDK integrado de Panda 2** (`jbr` / JDK 21). Si `GRADLE_LOCAL_JAVA_HOME` apunta a ese JDK integrado, también sirve. Para Gradle, usa la distribución del **Wrapper** del proyecto, no una versión elegida manualmente.
7. Pulsa **Sync Project with Gradle Files**. La primera sincronización requiere internet para descargar Gradle 9.3.1, AGP 9.1.1 y las dependencias. No aceptes una actualización a AGP 9.3, porque perderías la compatibilidad con Panda 2.
8. Si pide la ruta del SDK, selecciona el SDK local del laboratorio. Android Studio crea `local.properties`, que no se comparte por Git.
9. Selecciona un emulador o celular con Android 14 / API 34 o superior. Pulsa **Run**. Para instalar la app no hace falta que el teléfono tenga Android 16: minSdk sigue siendo 34.
10. Acepta el permiso de notificaciones. En Agenda, prueba también el permiso opcional de alarmas exactas y las opciones de aviso de la guía.
11. Repite la sincronización y ejecución para `Practica2` y `Practica3`.

Si trabajas sin internet en el laboratorio, realiza antes la descarga/sincronización en ese mismo equipo: descargar únicamente el ZIP del código no incluye Gradle, SDK ni dependencias.

## Archivos modificados y motivo

- `gradle/libs.versions.toml`: fija AGP 9.1.1, dentro de la serie admitida por Panda 2.
- `gradle/wrapper/gradle-wrapper.properties`: selecciona Gradle 9.3.1, requerido por AGP 9.1, y su checksum oficial.
- `app/build.gradle`: compila contra SDK 36.1 y declara targetSdk 36, evitando SDK 37, cuyo soporte de IDE requiere una versión posterior a Panda 2.
- Se elimina `gradle/gradle-daemon-jvm.properties`: contenía criterios de descarga de Java 25. Sin ellos, se respeta el JDK elegido en Android Studio o JAVA_HOME.
- Las guías y README documentan esta configuración para no indicar las versiones antiguas de main.

## Pruebas y límites

Los tres proyectos generaron sus APK y pasaron las 31 pruebas unitarias con Java 17; también se generaron los tres APK de pruebas instrumentadas. Los resultados de esta rama están separados de los originales de main en `VERIFICACION.md`. No se volvieron a ejecutar las pruebas instrumentadas ni se probó esta rama en el A55. Compilar con las herramientas compatibles no equivale a ejecutar el IDE Panda 2: la comprobación final de sincronización debe realizarse en el equipo del laboratorio.

## Referencias oficiales

- [Compatibilidad entre Android Studio y AGP](https://developer.android.com/studio/releases/index.html#android_gradle_plugin_and_android_studio_compatibility): Panda 2 admite hasta la serie 9.1.
- [Requisitos de AGP 9.1](https://developer.android.com/build/releases/agp-9-1-0-release-notes): Gradle 9.3.1 y JDK mínimo 17.
- [Configurar Android SDK 36.1](https://developer.android.com/about/versions/16/qpr2/setup-sdk): sintaxis compileSdk y selección del paquete en SDK Manager.
