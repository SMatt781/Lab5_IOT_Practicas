# Usar las tres prácticas con Android Studio Panda 3

## Configuración de Version_panda3

- Android Studio: Panda 3 / 2025.3.3.
- Android Gradle Plugin: 9.1.1.
- Gradle Wrapper: 9.3.1, con SHA-256 oficial.
- compileSdk: 36.1; targetSdk: 36; minSdk: 34.
- JDK: el integrado de Panda 3, o JDK 17 o superior compatible con Gradle. No se exige descargar Java 25.
- Código fuente Java: nivel 11, sin cambios.

Esta rama nace de Version_panda2 porque su configuración también es compatible con Panda 3. No se cambiaron las versiones de compilación, las dependencias ni el código Java/XML. Las ramas main y Version_panda2 se mantienen independientes y sin modificaciones.

Panda 3 admite AGP de la serie 9.1. Se conserva SDK 36.1 para funcionar también sin Patch 1. Usar SDK 37 requeriría como mínimo Panda 3 Patch 1 y no aporta nada necesario a estas prácticas.

## Pasos en Android Studio

1. En GitHub selecciona la rama Version_panda3 antes de descargar el ZIP. Si ya clonaste el repositorio, ejecuta `git fetch origin` y `git switch Version_panda3`, o selecciona la rama desde Android Studio.
2. Comprueba **Help > About**: debe indicar Panda 3 / 2025.3.3.
3. Abre **File > Open** y elige una sola carpeta: Practica1, Practica2 o Practica3. Lab5 no es un proyecto Gradle único.
4. En **Tools > SDK Manager > SDK Platforms**, activa **Show Package Details** e instala **Android SDK Platform 36.1**, dentro de Android 16 / Baklava. Tener solamente SDK 36 o SDK 37 no sustituye el paquete 36.1 solicitado.
5. En **SDK Tools**, con **Show Package Details**, instala **Android SDK Build-Tools 36.0.0**. Confirma con **Apply**.
6. En **Settings > Build, Execution, Deployment > Build Tools > Gradle**, selecciona el **Gradle JDK integrado de Panda 3** y la distribución del **Gradle Wrapper** del proyecto. Java 8 y Java 11 no sirven para ejecutar este Gradle; el nivel Java 11 del código fuente es una configuración distinta.
7. Pulsa **Sync Project with Gradle Files**. La primera sincronización necesita internet para descargar las herramientas y dependencias. No aceptes actualizar a AGP 9.3 porque Panda 3 no lo admite.
8. Si se solicita la ruta del SDK, usa la del equipo del laboratorio. Android Studio genera local.properties; no copies el de otra computadora.
9. Selecciona un celular o emulador con Android 14 / API 34 o superior y pulsa **Run**. Acepta las notificaciones y sigue el recorrido rápido de la guía de esa práctica.
10. Repite con las otras dos carpetas, abriéndolas por separado.

## Verificación y límites

La configuración y el código son idénticos a los ya verificados en Version_panda2: las tres prácticas compilaron con Java 17 y pasaron 31 pruebas unitarias (9 de Agenda, 9 de Inventario y 13 de Gastos). También se generaron los APK de pruebas instrumentadas. Estos resultados se heredan de esa revisión; no se presentan como una nueva ejecución de las pruebas en Panda 3.

El IDE Panda 3 no está instalado en esta PC. Falta comprobar la sincronización dentro de ese IDE y ejecutar en el equipo/celular del laboratorio. La compatibilidad se basa en las versiones admitidas oficialmente y en la compilación previa de la misma configuración. No se volvieron a ejecutar las pruebas instrumentadas en esta rama.

## Referencias oficiales

- [Compatibilidad de Android Studio, AGP y SDK](https://developer.android.com/studio/releases#android_gradle_plugin_and_android_studio_compatibility).
- [Requisitos de AGP 9.1](https://developer.android.com/build/releases/agp-9-1-0-release-notes).
- [Instalar y configurar SDK 36.1](https://developer.android.com/about/versions/16/qpr2/setup-sdk).
