# Verificación local de Lab5

Fecha de las pruebas: 20/10/2026. Las comprobaciones se realizaron localmente antes de publicar este repositorio; no se modificó IoTelito.

## Compilaciones y pruebas

| Proyecto | assembleDebug | Unitarias | Instrumentadas (Android 14/API 34, Pixel 4) | Fallos |
|---|---|---:|---:|---:|
| Practica1 - Agenda | Correcto | 9 | 7 | 0 |
| Practica2 - Inventario | Correcto | 9 | 7 | 0 |
| Practica3 - Gastos | Correcto | 13 | 7 | 0 |
| Total | 3 APK generados | 31 | 21 | 0 |

Son **52 pruebas contando las 6 pruebas originales de las plantillas** (3 de suma y 3 de contexto). Las otras **46** cubren funciones de estas prácticas. La última compilación también verificó los ajustes de contraste de barras y encabezados del calendario.

### Qué se comprobó automáticamente

- Fechas únicas y anuales, año bisiesto, hora y validación de campos.
- Persistencia en SharedPreferences, edición conservando ID y cancelación de alarma al eliminar.
- Entrega lógica de avisos: descartar revisión antigua, canal correcto y no repetir la misma ocurrencia.
- Room: insertar, consultar, modificar, eliminar y revertir una transacción fallida, en una base de pruebas en memoria.
- Validación de cantidades enteras.
- Dinero en céntimos, umbrales, formato JSON, rechazo de registros inválidos y fusión sin duplicados.
- Archivo interno persistente al recrear ExpenseStore; archivo inválido rechazado antes de escribir.
- Apertura de pantallas, controles principales y bloqueo de formularios vacíos.
- Canales HIGH/DEFAULT y permiso concedido en el emulador.
- Dos notificaciones con identidades diferentes; el PendingIntent conserva el ID de destino y abre el formulario; cancelación del aviso.

### Qué no debes interpretar como una garantía

- No se ejecutaron pruebas físicas en tu Samsung A55. Revisa allí tamaño de letra, teclado, permiso, batería y canales.
- El test de entrega llama la lógica que procesa el aviso; **no demuestra puntualidad de AlarmManager después de horas en reposo**.
- Las alarmas aproximadas pueden retrasarse; Forzar detención impide el comportamiento normal hasta volver a abrir.
- Las pruebas de JSON comprueban lectura/escritura/fusión, no todos los proveedores de documentos disponibles en cada teléfono. Prueba manualmente exportar e importar con el selector.
- No se simularon fallos físicos de almacenamiento ni pérdida de energía.
- Los ejemplos no implementan sincronización entre teléfonos ni una base remota.
- Las advertencias de APIs de Android o funciones Gradle obsoletas no fueron errores de compilación. No se cambió de versión del proyecto para ocultarlas.

## Revisión visual

Se inspeccionaron en el emulador la pantalla principal, calendario y formulario de la agenda; principal y formulario de inventario; y resumen/formulario de gastos.

Se comprobó que las etiquetas Material no se duplican sobre el texto, que los formularios tienen desplazamiento y que las barras del sistema tienen separación y contraste. El emulador presentó inicialmente un aviso de System UI durante su arranque; se descartó, y la revisión se repitió con las pantallas estables. No se observó un cierre de las Activities de las prácticas.

## Archivos generados en esta computadora

Las fuentes están en sus carpetas originales dentro de Practicas/Lab5. Una propiedad local opcional redirige únicamente las salidas de compilación:

- C:/AndroidBuilds/Lab5_Practica1/app/outputs/apk/debug/app-debug.apk
- C:/AndroidBuilds/Lab5_Practica2/app/outputs/apk/debug/app-debug.apk
- C:/AndroidBuilds/Lab5_Practica3/app/outputs/apk/debug/app-debug.apk

Reportes unitarios: cada salida app/reports/tests/testDebugUnitTest/. Reportes Android: app/reports/androidTests/connected/debug/. Los XML de resultados Android están en app/outputs/androidTest-results/connected/debug/.

No subas estas salidas a GitHub como si fueran el código fuente.

## Ajuste local de lectura de OneDrive

Durante la compilación Gradle rechazó archivos marcados como puntos de reanálisis con “not a regular file”. Se fijaron como locales y se renovaron sus copias conservando exactamente sus bytes, comprobados con SHA-256. No se cambiaron sus nombres ni la ubicación de los proyectos.

El script de mantenimiento usado quedó en ../../tmp/lab5_qa/normalizar_lab5.ps1, fuera de los proyectos Android. **No es parte del temario, ni un componente de la app, ni necesita subirse al repositorio.** No borra carpetas; se limita a archivos de app/src de estas tres prácticas y verifica rutas y contenido antes de renovar una copia.

Si tu Android Studio compila normalmente, no necesitas ejecutar ese mantenimiento. Para problemas de archivos de OneDrive, prueba primero Mantener siempre en este dispositivo y espera a que termine la sincronización. local.properties no se comparte.

## Tu siguiente comprobación

Abre Practica1 desde File > Open, selecciona app y tu A55, pulsa Run y sigue el Recorrido rápido de su GUIA_PASO_A_PASO.md. Después repite los casos de guardar, editar, cerrar/abrir, cancelar aviso y tocar una notificación. No cambies los tres proyectos a la vez: comprueba uno y recién pasa al siguiente.
