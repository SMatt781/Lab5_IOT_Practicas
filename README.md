# Lab5: tres prácticas de almacenamiento local y notificaciones

Abre **cada carpeta de proyecto por separado** en Android Studio. No abras Lab5 como si fuera un solo proyecto Gradle.

## Rama para Android Studio Panda 2

Esta rama, `Version_panda2`, usa AGP 9.1.1, Gradle 9.3.1 y Android SDK 36.1 (targetSdk 36). No exige Java 25. La rama `main` conserva la configuración original. Lee [COMPATIBILIDAD_PANDA2.md](COMPATIBILIDAD_PANDA2.md) antes de abrir las prácticas en el laboratorio.

| Proyecto | Tema y pantallas | Almacenamiento | Avisos |
|---|---|---|---|
| [Practica1](Practica1/GUIA_PASO_A_PASO.md) | Agenda: lista, formulario y calendario mensual | Lista JSON en SharedPreferences | Fechas programadas, únicos DEFAULT/anuales HIGH |
| [Practica2](Practica2/GUIA_PASO_A_PASO.md) | Inventario IoT: lista, búsqueda, filtro y formulario | Room; preferencia de filtro en SharedPreferences | Stock bajo y recuperación |
| [Practica3](Practica3/GUIA_PASO_A_PASO.md) | Gastos: resumen mensual, formulario, importar/exportar | Archivo JSON interno; presupuestos en SharedPreferences | Gasto registrado y umbrales de presupuesto |

## Por dónde empezar

1. Abre Practica1.
2. Lee **Recorrido rápido** en su GUIA_PASO_A_PASO.md y ejecútalo antes de leer toda la explicación.
3. Después sigue las secciones de Design: identifica los componentes y sus ID.
4. Continúa con el flujo Java: listener, modelo, validación, Repository, almacenamiento, LiveData y lista.
5. Deja AlarmManager y receivers para después de entender el guardado.
6. Pasa a Practica2 para aprender el contraste con Room.
7. Termina con Practica3 para trabajar archivos y el selector de documentos.

Cada guía es independiente y extensa: incluye preparación de Android Studio, qué colocar en Design, atributos/ID, código explicado, responsabilidades de archivos, permisos, pruebas manuales, errores frecuentes y ubicación de los datos.

## Base de clase y referencias

Se revisaron los PDF de Storage y su extra Database de semana 7, y Notificaciones de semana 8.1. También se revisaron los ejemplos del profesor en [Clase7](https://github.com/2026-2-1TEL05-AppsIot/Clase7) y [Clase8_1](https://github.com/2026-2-1TEL05-AppsIot/Clase8_1), además del material de apoyo Clase7-WebService.

Se conservaron Java, XML, ViewBinding, Material Design y el patrón Entity/DAO/Repository/ViewModel donde corresponde. No se agregó Compose, Firebase, MongoDB ni un servidor.

El enunciado usado localmente, cuyo archivo se llama PB4-IOT-2026-1.pdf, dice Laboratorio 5/Evaluación 4 en su encabezado. **Practica1 desarrolla ese enunciado pasado**, no sus fechas antiguas de entrega. La rúbrica visible suma 15 puntos; no se inventan apartados faltantes. El PDF no se incluye en este repositorio.

Extensiones explicadas, no atribuidas al profesor: AlarmManager/reprogramación para cumplir fechas futuras, calendario con marcas, AtomicFile para escritura segura y validación de importaciones. Los ejemplos del profesor de avisos inmediatos no se tomaron como si ya programaran alarmas completas.

## Ejecutar y probar

- Cada proyecto tiene su propio wrapper Gradle y applicationId. Pueden instalarse los tres a la vez.
- Usa JDK integrado de Android Studio y un dispositivo API 34 o superior.
- Acepta notificaciones en la app si quieres verlas.
- Solo Practica1 ofrece acceso opcional a alarmas exactas. Sin él funciona de manera aproximada, sujeto a retrasos de Android.
- Los datos se conservan al cerrar y abrir. Desinstalar o borrar datos los elimina.
- **Probar aviso ahora** comprueba permiso, canal y destino, no la puntualidad de una alarma.
- Las pruebas no sustituyen comprobar en tu A55 las condiciones reales de batería y permisos.

En esta computadora las salidas generadas se envían a C:/AndroidBuilds/Lab5_Practica1, _2 y _3 mediante una propiedad opcional en local.properties, ignorado por Git. Las fuentes siguen aquí en OneDrive. En otra computadora sin esa propiedad se usan build normales. Los APK debug se generan bajo la salida del módulo app/outputs/apk/debug.

Los tests están en app/src/test y app/src/androidTest. La guía de cada proyecto explica cómo ejecutarlos. Ver también [VERIFICACION.md](VERIFICACION.md) para los resultados comprobados y sus límites.

## Alcance y GitHub

Repositorio público: [Lab5_IOT_Practicas](https://github.com/SMatt781/Lab5_IOT_Practicas).

Las tres carpetas conservan su independencia. Se incluyen el código, los wrappers Gradle, las pruebas y las guías. Se excluyen local.properties, .gradle, .idea, build y claves de firma. Los datos creados en la instalación no se sincronizan por GitHub; solo se comparte el código. IoTelito es un proyecto aparte y no se incluye aquí.

Para descargarlo, clona el repositorio y abre Practica1, Practica2 o Practica3 por separado desde File > Open en Android Studio. Android Studio configurará la ruta del SDK de tu equipo.
