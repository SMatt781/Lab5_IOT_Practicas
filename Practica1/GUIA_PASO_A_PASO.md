# Práctica 1: Agenda de eventos

Esta práctica desarrolla el laboratorio pasado adjunto. El PDF se llama PB4-IOT-2026-1.pdf pero su encabezado dice Laboratorio 5/Evaluación 4. Se utiliza como ejercicio de estudio: sus fechas e instrucciones de entrega antiguas no son una entrega actual.

La aplicación permite crear eventos únicos o anuales, editarlos, eliminarlos, conservarlos en SharedPreferences, programar sus avisos y consultar un calendario mensual con marcas.

**Empieza probándola, luego reconstruye una parte a la vez.** No debes escribir todo de memoria.

## Recorrido rápido para comprobar que funciona

1. Abre Practica1 en Android Studio y ejecuta app.
2. Pulsa **Permitir notificaciones** y acepta.
3. Pulsa **Añadir evento**.
4. Escribe “Examen de IoT”.
5. Pulsa **Seleccionar fecha** y elige mañana.
6. Activa **Añadir hora** y selecciona una hora futura.
7. En Periodicidad deja **Una vez**.
8. En Recordar antes elige **Mismo día** para que el aviso corresponda a esa hora; si dejas 1 día, se intenta un día antes a la misma hora.
9. Pulsa Guardar. Aparece una tarjeta con el nombre, fecha y tiempo restante.
10. En esa tarjeta pulsa **Probar aviso ahora**. Despliega la barra de notificaciones; toca el aviso: abre el evento correcto para editar.
11. Vuelve y pulsa **Ver calendario mensual**. El día de tu evento tiene un punto naranja.
12. Selecciona ese día: debajo aparecen los eventos desde esa fecha, no en otra Activity.
13. Crea otro evento “Cumpleaños” con periodicidad **Cada año**. Su punto es morado y usa el canal de importancia alta.
14. Cierra y abre la app: ambos siguen guardados.


## Antes de empezar: abrir y ejecutar sin perderte

1. Abre Android Studio. Si tienes otro proyecto, ve a **File > Open**.
2. Selecciona **la carpeta de esta práctica**, no la carpeta Lab5 ni solo app. Esa carpeta contiene settings.gradle, gradlew.bat y app.
3. Si Android Studio te pregunta si confías en el proyecto, confirma solo porque es tu carpeta de trabajo.
4. Espera a **Gradle Sync**. Si no empieza: **File > Sync Project with Gradle Files**.
5. En **Settings > Build, Execution, Deployment > Build Tools > Gradle**, usa el **JDK integrado de Android Studio (jbr)**. No cambies a un Java antiguo instalado por otro programa.
6. En la rama Version_panda2 conserva AGP 9.1.1, Gradle 9.3.1 y compileSdk 36.1 (targetSdk 36). Instala Android SDK Platform 36.1 y Build-Tools 36.0.0 desde Tools > SDK Manager. Usa el JDK integrado de Panda 2 (17 o superior compatible con Gradle); ya no se exige Java 25. Consulta COMPATIBILIDAD_PANDA2.md en la carpeta Lab5 para el paso a paso. No aceptes actualizar AGP a 9.3 si trabajarás con Panda 2.
7. Si falta el SDK, abre **Tools > SDK Manager** y descarga el que indique compileSdk. El mínimo de estos proyectos es Android 14/API 34, como tus plantillas: un emulador inferior no sirve.
8. Arriba elige la configuración **app** y un dispositivo compatible. Para tu Samsung A55, activa las opciones de desarrollador y depuración USB, conecta el cable y acepta su autorización.
9. Pulsa el triángulo **Run**. Las tres prácticas tienen identificadores diferentes y pueden estar instaladas juntas.
10. Al principio no hay datos inventados automáticamente. Sigue las pruebas manuales de esta guía para crearlos.
11. Autoriza las notificaciones desde el botón de la app. Si las rechazas, **el almacenamiento sigue funcionando**. No confundas guardar datos con tener permiso para mostrar avisos.
12. No necesitas cuenta, servidor ni Internet para el funcionamiento central de estas prácticas.

### Archivos locales y OneDrive en tu computadora

Tus proyectos permanecen en su ubicación original. En el **local.properties de esta computadora**, ignorado por Git, se configuró una salida de compilación opcional:

```properties
lab5.buildRoot=C:/AndroidBuilds/Lab5_PracticaNUMERO
```

En cada proyecto NUMERO es 1, 2 o 3, respectivamente. Esta ruta solo guarda archivos **generados**; no mueve tus fuentes. El build.gradle raíz lee esa propiedad si existe. Si otra computadora no la tiene, compila en las carpetas build normales. **No subas local.properties**, porque también contiene tu ruta privada al SDK.

Los archivos iniciales de OneDrive se fijaron como disponibles localmente para evitar el error “not a regular file”. Si reaparece, en el Explorador haz clic derecho sobre la carpeta y selecciona **Mantener siempre en este dispositivo**; espera el círculo verde antes de compilar. No borres código ni cambies de dispositivo emulado para intentar arreglar un error de archivos.

## Preparación común, después de crear el proyecto vacío

### 1. Dependencias y ViewBinding

Abre **Gradle Scripts > build.gradle (Module: app)**. Dentro de android se habilitó:

```groovy
buildFeatures {
    viewBinding true
}
```

ViewBinding hace que Android genere una clase por XML. activity_main.xml genera **ActivityMainBinding**. Un botón con ID saveButton se utiliza como **binding.saveButton**. No escribes manualmente esa clase generada.

En dependencies se agregaron RecyclerView, ViewModel/LiveData y Gson. Gson convierte objetos Java a JSON; RecyclerView muestra listas; ViewModel conserva la operación durante una rotación; LiveData avisa a la pantalla cuando hay datos nuevos. Room se agrega solo en la práctica que lo necesita.

Después de cambiar dependencias: **Sync**, luego **Build > Assemble Project** o el botón Run. Si ActivityMainBinding está rojo, primero revisa los errores XML y la sincronización; no crees una clase con ese nombre para “solucionarlo”.

### 2. Cómo reconocer los componentes en Design

Cambia el panel izquierdo a **Android**. Abre **app > res > layout**, doble clic en el XML y usa **Design** o **Split**.

- **LinearLayout vertical:** organiza hijos uno debajo de otro. Es el contenedor, no un texto visible. Su ancho/alto principal son match_parent.
- **MaterialToolbar:** encabezado con el título. En formularios se añade una flecha para terminar la Activity actual.
- **TextView:** muestra información que el usuario no edita. Usa text.
- **TextInputLayout:** marco Material, etiqueta flotante y espacio de error.
- **TextInputEditText:** campo editable *dentro* de TextInputLayout. Pon el hint en el contenedor, no duplicado en ambos.
- **MaterialAutoCompleteTextView:** selector de opciones dentro de un TextInputLayout con estilo de menú desplegable. Java suministra las opciones.
- **MaterialButton:** botón Material con acción. El texto del botón no ejecuta nada por sí solo.
- **MaterialSwitch:** permite activar una condición verdadera/falsa.
- **RecyclerView:** zona de lista. No creas diez tarjetas manualmente: dibujas una fila XML y el Adapter la repite por cada registro.
- **NestedScrollView:** permite desplazar formularios que no caben, especialmente al abrir el teclado. Debe tener **un solo hijo directo**, aquí un LinearLayout vertical.

**match_parent** ocupa lo disponible del padre. **wrap_content** mide lo necesario para mostrar el contenido. En una lista dentro de LinearLayout vertical se usa **height=0dp y weight=1**: recibe el espacio restante después del encabezado. Eso no es lo mismo que 0dp en ConstraintLayout.

Los estilos comunes están en **res > values > lab_styles.xml**. Un atributo que no ves directamente en el XML puede venir de su style. Los colores están allí; los temas diurno/nocturno se configuraron claros para mantener una presentación consistente.

### 3. Conectar XML con Java

Este patrón aparece al iniciar las pantallas:

```java
binding = ActivityMainBinding.inflate(getLayoutInflater());
showContent(binding.getRoot());
binding.addButton.setOnClickListener(v -> {
    // Acción del botón
});
```

En el código real la variable se llama **b** en varias Activities: b y binding son solo nombres de variable. inflate construye los componentes del XML; getRoot devuelve su contenedor. showContent pertenece a BaseActivity y llama a setContentView añadiendo la separación de las barras del sistema.

**No se agrega un margen fijo para un Pixel o un A55.** BaseActivity usa WindowInsets para la barra de estado, botones/gestos y teclado. El espacio se calcula desde el sistema; por eso no se superpone el formulario a la hora ni al botón Atrás.

### 4. Cambiar de pantalla

Para abrir un formulario nuevo:

```java
startActivity(new Intent(this,FormularioActivity.class));
```

FormularioActivity es un nombre explicativo: usa el nombre real indicado en la sección de esta práctica. Para editar se agrega un extra con el identificador del registro. La otra Activity lee ese extra y carga ese registro.

**finish()** cierra la pantalla actual y vuelve a la anterior; no abre otra copia de MainActivity. Cada Activity está declarada en AndroidManifest.xml. parentActivityName permite construir una pila de regreso cuando llegas desde una notificación.

### 5. Guardar sin congelar la pantalla

El flujo general es:

**Clic > leer campos > validar > ViewModel > Repository > almacenamiento > LiveData > RecyclerView.**

El Repository utiliza un ExecutorService de un hilo para serializar las escrituras. El Handler devuelve el resultado al hilo principal. **No hagas Thread.sleep en el hilo principal ni llames allowMainThreadQueries para esconder un error de Room.**

El ViewModel contiene busy y un OperationResult. busy evita dos guardados simultáneos desde el botón. OperationResult.consume evita repetir un mensaje o cerrar una pantalla otra vez después de rotarla. Los formularios guardan sus selecciones de fecha/hora en onSaveInstanceState; Android conserva también el texto de controles con ID.

### 6. Notificaciones: las piezas que debes distinguir

1. **Permiso:** POST_NOTIFICATIONS en el manifest y solicitud en tiempo de ejecución desde Android 13.
2. **Canal:** categoría registrada en NotificationManager. En estos proyectos hay un canal importante (HIGH) y uno normal (DEFAULT).
3. **Contenido:** NotificationCompat.Builder pone título, texto, icono pequeño y el canal.
4. **Destino:** un Intent indica qué Activity abrir y qué identificador enviar.
5. **PendingIntent:** Android conserva la acción para ejecutarla al tocar la notificación aunque la Activity original no esté abierta.
6. **Identidad:** una etiqueta estable por registro evita que todos los avisos abran el último elemento o se reemplacen sin intención.
7. **Notificación mostrada:** NotificationManagerCompat.notify publica el aviso.
8. **Programación:** esto es otra cosa. Solo la agenda necesita AlarmManager para intentar publicarlo más adelante.

NotificationHelper centraliza estas piezas. Usa FLAG_IMMUTABLE y FLAG_UPDATE_CURRENT, un URI único como identidad y TaskStackBuilder para regresar correctamente. HIGH no garantiza una ventana emergente: depende del canal, modo No molestar y configuración del usuario. Cambiar la importancia en Java no cambia mágicamente un canal ya creado: revisa Ajustes de Android para ese canal.

## Qué se tomó de clase y qué se adaptó

- Semana 7: almacenamiento privado de la app, SharedPreferences, archivos y Gson.
- Extra de semana 7: Entity, DAO, RoomDatabase, Repository, ViewModel y LiveData.
- Semana 8.1: canales, permiso, NotificationCompat y PendingIntent con extras.
- El código del profesor usa Java, XML y ViewBinding: estos ejemplos también.
- Los ejemplos del profesor muestran avisos inmediatos. La programación por fecha de la agenda se añadió para cubrir el laboratorio pasado; no se presenta como una función que ya estuviera terminada en su repositorio.
- Se usó ExecutorService en lugar de copiar AsyncTask de material antiguo. Room usa una versión compatible con este proyecto, no la versión antigua 2.2.5 del PDF.
- No se agregó MongoDB, Firebase ni backend: no son necesarios para este objetivo de almacenamiento **local**. Room es una base SQLite local, no una base NoSQL en la nube.

Referencias consultadas del profesor:

- [Clase7: almacenamiento y ejemplos de avisos](https://github.com/2026-2-1TEL05-AppsIot/Clase7)
- [Clase7-WebService: apoyo de los ejemplos](https://github.com/2026-2-1TEL05-AppsIot/Clase7-WebService)
- [Clase8_1: canales y PendingIntent](https://github.com/2026-2-1TEL05-AppsIot/Clase8_1)

No se copiaron métodos con efectos distintos a su nombre: por ejemplo, listarArchivosGuardados del ejemplo de Clase7 también elimina archivos. Aquí no se eliminan tus registros al listar.

## Qué subirá a GitHub después y qué no

Esta entrega es **solo local para que la pruebes**. No se hizo commit ni push.

Al compartir luego: código Java, XML, Gradle, wrapper y esta guía. No compartas .gradle, .idea de tu máquina, build, local.properties, claves ni archivos privados exportados. Los datos creados en tu celular **no están en GitHub**: viven dentro de su instalación. Desinstalar o borrar los datos de la app elimina ese almacenamiento; cerrar y volver a abrir no.

El objetivo no es memorizar todas las líneas. Repite primero: diseñar un campo, darle ID, leerlo desde Java, validarlo, guardarlo, mostrarlo otra vez y abrir su detalle.


## Mapa de archivos específico

Todos los Java siguientes están en app/src/main/java/com/example/lab5_practica1/.

| Archivo | Responsabilidad |
|---|---|
| Event.java | Datos de un evento; no dibuja pantalla ni guarda por sí solo. |
| EventDates.java | Validación, próxima fecha, formato y tiempo restante. |
| EventRepository.java | Leer/escribir lista JSON en SharedPreferences y coordinar cambios. |
| AgendaViewModel.java | Ocupación y resultado de las operaciones de cada pantalla. |
| MainActivity.java | Lista y botones principales. |
| EventAdapter.java | Convertir cada evento en row_event.xml. |
| EventFormActivity.java | Crear/editar leyendo los controles. |
| CalendarActivity.java | Mes de 42 casillas y lista desde el día elegido. |
| AlarmScheduler.java | Crear/cancelar la alarma asociada al ID. |
| EventAlarmReceiver.java | Recibir la alarma aunque no esté abierta la Activity. |
| RestoreReceiver.java | Reprogramar tras reinicio/cambio de hora/zona/actualización. |
| AgendaApp.java | Inicializar canales y recuperar la agenda al iniciar el proceso. |
| common/ | BaseActivity, NotificationHelper y resultado de operación. |

Layouts: activity_main.xml, activity_event_form.xml, activity_calendar.xml, row_event.xml.

## Paso a paso para reproducir el diseño

### Paso 1. Pantalla principal: activity_main.xml

En **res > layout > activity_main.xml > Design**, mira Component Tree.

1. Contenedor LinearLayout vertical, match_parent en ambos ejes.
2. MaterialToolbar con ID **toolbar** y título “Agenda de eventos”.
3. LinearLayout vertical interno con paddingHorizontal 16dp.
4. TextView de título y TextView **statusText**. Este último muestra permiso y precisión del horario.
5. MaterialButton **permissionButton**: “Permitir notificaciones”.
6. MaterialButton **precisionButton**: “Configurar avisos exactos (opcional)”.
7. MaterialButton **calendarButton**: “Ver calendario mensual”.
8. MaterialButton **addButton**: “Añadir evento”.
9. TextView **emptyText**: “No hay eventos programados”.
10. Después del bloque de controles, RecyclerView **recycler**: ancho match_parent, alto 0dp y layout_weight 1. Padding 12dp y clipToPadding false permiten ver el final al desplazarte.

No pongas todas las tarjetas a mano. Abre row_event.xml y diseña **una** tarjeta MaterialCardView con LinearLayout vertical dentro. Tiene:

| ID | Componente | Qué muestra/hace |
|---|---|---|
| nameText | TextView | Nombre |
| periodText | TextView | Anual/una vez y anticipación |
| dateText | TextView | Próxima fecha |
| remainingText | TextView | Días y horas restantes |
| editButton | MaterialButton | Abrir formulario con ID |
| deleteButton | MaterialButton | Confirmar y eliminar |
| testButton | MaterialButton | Publicar un aviso inmediato de prueba |

El Adapter recibe una lista, infla esa fila y asigna los textos en onBindViewHolder. El tamaño de la lista viene de getItemCount.

### Paso 2. Formulario: activity_event_form.xml

1. Crea un layout con LinearLayout vertical raíz.
2. Añade MaterialToolbar **toolbar**.
3. Añade NestedScrollView con alto 0dp, weight 1 y un único LinearLayout vertical dentro.
4. TextInputLayout **nameEditLayout**, hint “Nombre del evento”. Dentro TextInputEditText **nameEdit**, inputType textCapSentences y maxLength 100.
5. MaterialButton **dateButton**: abrirá MaterialDatePicker. Un selector de fecha evita formatos ambiguos escritos a mano.
6. MaterialSwitch **timeSwitch**: activa hora opcional.
7. MaterialButton **timeButton**: abre MaterialTimePicker; se deshabilita cuando no se usa hora.
8. TextInputLayout de menú desplegable con **periodInput** dentro: MaterialAutoCompleteTextView, inputType none.
9. Otro desplegable **leadInput** para anticipación.
10. MaterialButton **saveButton**.
11. Añade los textos de ayuda para explicar las 09:00 y el aviso cuando la anticipación ya pasó.

Los desplegables se cargan desde Java con setSimpleItems; no son EditText normales donde cualquier texto es válido. En este ejercicio no se permite una periodicidad arbitraria.

### Paso 3. Calendario: activity_calendar.xml

1. LinearLayout raíz y toolbar.
2. Fila horizontal con **previousButton**, TextView **monthText** con width 0dp/weight 1, y **nextButton**.
3. Etiquetas de días de semana.
4. GridLayout **calendarGrid**, columnCount 7, height wrap_content.
5. Leyenda de colores.
6. TextView **selectedText** y **emptyText**.
7. RecyclerView **recycler** debajo, alto 0dp/weight 1.

Las 42 casillas del mes se construyen en CalendarActivity.render porque cambian según el mes. No son 42 XML distintos. El primer hueco depende del día de la semana; los días fuera del mes quedan vacíos. Cada casilla contiene número y hasta dos puntos de colores. Si hay evento anual y único el mismo día, muestra ambos puntos.

El calendario no usa un CalendarView nativo porque no permite dibujar de esta forma varias marcas de colores de eventos. Esto es una adaptación del requisito, no una librería externa escondida.

## Paso a paso de la lógica

### Paso 4. Crear el modelo Event

Un objeto representa un registro:

```java
Event event = new Event();
event.name = "Examen";
event.date = "2027-01-20";
event.annual = false;
event.withTime = true;
event.timeMinutes = 14 * 60 + 30;
event.leadDays = 1;
```

La fecha se guarda como ISO yyyy-MM-dd, no como un texto con formato cambiante. timeMinutes guarda minutos desde medianoche: 14:30 equivale a 870. El ID lo asigna el Repository, no lo escribes tú en el formulario. revision cambia al editar y sirve para ignorar una alarma antigua que estuviera entregándose al mismo tiempo.

### Paso 5. Validar antes de guardar

EventFormActivity.save:

1. Lee nameEdit.getText().toString().
2. Usa la fecha seleccionada en el picker.
3. Lee isChecked del switch.
4. Copia hora, periodicidad y leadDays.
5. Llama a EventDates.validate.
6. Si falta nombre o fecha, muestra error; no guarda.
7. Si el evento es único y ya pasó, lo rechaza.
8. Si es anual, admite un año de origen anterior: lo importante es su próxima ocurrencia.
9. Solo entonces llama model.save(event).

Guardar no significa navegar inmediatamente: el formulario espera confirmación del Repository y recién hace finish(). Si el guardado falla, conserva la pantalla para que no creas que los datos se guardaron.

### Paso 6. SharedPreferences con una lista

SharedPreferences no tiene un tipo List<Event>. La solución de clase es **convertir la lista a JSON con Gson** y guardar ese String.

EventRepository usa:

```java
context.getSharedPreferences("agenda_eventos", Context.MODE_PRIVATE);
```

Dentro hay:

- **eventos**: String JSON de la lista.
- **next_id**: siguiente identificador disponible.

El esquema de guardado es:

```java
prefs.edit()
    .putString("eventos", gson.toJson(list))
    .putInt("next_id", nextId)
    .commit();
```

Se usa commit **dentro del Executor**, no en la pantalla, para verificar si realmente se escribió. apply sirve muy bien para preferencias pequeñas, pero aquí interesa confirmar el resultado antes de cerrar.

Para recuperar:

```java
List<Event> list = gson.fromJson(
    prefs.getString("eventos", "[]"),
    new TypeToken<ArrayList<Event>>() {}.getType()
);
```

TypeToken informa a Gson qué objetos van dentro de la lista. “[]” significa lista vacía. Si un JSON está dañado no se reemplaza silenciosamente por vacío: se informa el problema.

Editar: se quita el objeto con ese ID y se agrega el objeto actualizado, manteniendo el ID. Eliminar: removeIf por ID y persistir la lista restante. No se borran todos los eventos para borrar uno.

Una segunda preferencia, **avisos_emitidos**, recuerda la ocurrencia cuyo aviso ya se intentó entregar; evita repetir la misma alarma cada vez que vuelves a abrir.

### Paso 7. Navegación real

MainActivity.edit llama:

```java
startActivity(new Intent(this, EventFormActivity.class)
    .putExtra("event_id", e.id));
```

El formulario lee getIntExtra("event_id", 0). Si vale 0 crea; si no, espera la lista del Repository y carga ese evento una sola vez. La bandera loaded evita que una actualización de LiveData borre lo que estabas escribiendo.

CalendarActivity usa el mismo formulario, Adapter y Repository. Por eso editar desde el calendario actualiza también la lista principal: no hay dos copias separadas de los datos.

### Paso 8. Programar un aviso, no solo mostrarlo

Regla: **instante de aviso = fecha/hora de la próxima ocurrencia menos leadDays**.

Ejemplo: evento el 22 a las 14:00 con anticipación de 1 día → aviso el 21 a las 14:00.

1. AlarmScheduler cancela el PendingIntent anterior del mismo ID.
2. Calcula la próxima ocurrencia. Para anual ya pasada este año usa el próximo.
3. Si no se seleccionó hora, usa 09:00.
4. Convierte la hora local a epoch con la zona actual.
5. Si el aviso ya debía haber salido, pero el evento sigue futuro, se intenta dentro de aproximadamente 1.5 segundos, no se inventa una fecha antigua.
6. Crea un PendingIntent de broadcast con URI agenda://event/ID.
7. Si Android permite alarmas exactas, usa setExactAndAllowWhileIdle.
8. Si no, usa setAndAllowWhileIdle. Es **aproximado**, Android puede retrasarlo por ahorro de batería; no promete puntualidad al segundo.
9. EventAlarmReceiver recibe ID, revisión y ocurrencia.
10. El Repository verifica que ese evento todavía exista y que la revisión coincida.
11. NotificationHelper publica nombre y fecha.
12. Se marca esa ocurrencia como procesada; si era anual, programa la siguiente anualidad.

**Permiso de notificaciones y permiso de alarmas exactas son diferentes.** Aceptar uno no concede el otro. El botón de exactitud abre los ajustes especiales de Android; concederlo es opcional. Se declara SCHEDULE_EXACT_ALARM, no se utiliza USE_EXACT_ALARM para saltarse restricciones.

Si no había permiso de notificaciones cuando llegó la alarma, los datos siguen guardados, pero no se publica el aviso. La ocurrencia se marca como intentada para evitar un bucle inmediato. Puedes comprobar el canal con el botón de prueba; editar y guardar permite reprogramar de nuevo.

**Probar aviso ahora no prueba el tiempo de AlarmManager:** solo comprueba canal, permiso y destino. Para probar la programación real sigue el caso de uno o dos minutos que aparece más abajo.

### Paso 9. Editar, eliminar y reiniciar

Al editar se conserva ID, cambia revision, se cancela la programación anterior y se registra la nueva. Una entrega vieja que llegue tarde es ignorada por la revisión.

Al eliminar se cancela la alarma y los avisos visibles relacionados, además de quitar el registro de SharedPreferences.

RestoreReceiver escucha reinicio, cambio de hora, zona, actualización del paquete y concesión de alarmas exactas. Utiliza goAsync mientras el Repository reprograma en su hilo. No es un servicio permanente.

**Límites reales:** Forzar detención desde Ajustes detiene el paquete hasta que lo abras. Las alarmas se pierden al desinstalar o borrar datos. Las alarmas inexactas y las políticas de batería del fabricante pueden retrasarse. Un evento único que ya terminó cuando recuperas la app se conserva como pasado, pero no se intenta notificar retroactivamente. Los eventos anuales del 29 de febrero se celebran el 28 en años no bisiestos; en bisiestos vuelven al 29.

### Paso 10. Calendario y tiempo restante

Al tocar un día, selected cambia y se reconstruye la lista debajo. Los únicos se incluyen si su fecha está desde selected en adelante. Los anuales se calculan desde esa selección, incluso si su año original fue anterior.

El texto restante usa la fecha actual, no una cuenta regresiva guardada que se volvería incorrecta al cerrar la app. MainActivity actualiza ese texto cada minuto mientras está visible y detiene el Handler al salir.

## Correspondencia con el laboratorio pasado

| Parte visible del enunciado | Implementación |
|---|---|
| UI: nombre, fecha, tiempo restante, agregar/eliminar | MainActivity + EventAdapter |
| Formulario editable completo | EventFormActivity |
| Lista persistida en SharedPreferences | EventRepository |
| Estado vacío exacto | emptyText |
| Programar/actualizar/cancelar avisos | AlarmScheduler + Repository |
| Canales anuales HIGH y únicos DEFAULT | NotificationHelper |
| Nombre y fecha en aviso | EventAlarmReceiver/Repository |
| Calendario mensual y colores | CalendarActivity |
| Lista desde día seleccionado, misma Activity | RecyclerView debajo de calendarGrid |

Los puntajes visibles del documento suman **15 (3 + 4 + 4 + 4)**. No se inventó un criterio adicional de cinco puntos.

## Pruebas manuales, una por una

### A. Guardado y edición

1. Crea un evento único futuro.
2. Sal con Atrás y abre otra vez: sigue ahí.
3. Edita nombre, fecha, hora, periodicidad y anticipación.
4. Guarda y comprueba todos los campos al volver a entrar.
5. Rota el teléfono con un nombre a medio escribir: no debe volver al nombre antiguo.
6. Elimina desde la lista; confirma que desaparece también del calendario.
7. Elimina el último: “No hay eventos programados”.

### B. Aviso programado real

1. Permite notificaciones. Opcionalmente permite alarmas exactas.
2. Crea un evento **para hoy**, con hora **dos o tres minutos después** de la hora actual.
3. Activa la hora. Periodicidad Una vez. Anticipación **Mismo día**.
4. Guarda, pulsa Inicio del teléfono y espera. **No pulses Forzar detención**.
5. Debe aparecer el aviso alrededor de esa hora; en modo aproximado puede retrasarse.
6. Tócalo y verifica nombre y fecha del registro.
7. Repite con otra fecha/hora. Antes de que llegue, edítalo para diez minutos después: el aviso anterior no debe salir.
8. Repite eliminándolo antes de la hora: no debe salir.
9. Si ya pasó la anticipación pero no el evento, el aviso se intenta ahora, una sola vez.

### C. Anuales y calendario

1. Crea un anual en una fecha de cumpleaños anterior a este mes.
2. Comprueba que su próxima fecha es del año siguiente.
3. Crea un único y un anual el mismo día de un mes futuro.
4. En calendario se ven dos colores.
5. Toca un día anterior, el mismo y uno posterior: verifica cómo cambia la lista inferior.
6. Cambia de mes con las flechas.
7. Crea un anual 29/02 usando un año bisiesto y comprueba el comportamiento documentado al consultar otros años.

### D. Permisos y validación

1. Intenta guardar sin nombre y sin fecha.
2. Intenta crear un único pasado.
3. Rechaza el permiso de avisos y crea un evento: se guarda, pero no publica notificaciones.
4. Permite después desde Ajustes; usa Probar aviso ahora.
5. Comprueba que un aviso anual y uno único están en canales diferentes.

## Pruebas automáticas incluidas

- EventDatesTest: fechas únicas/anuales, 29 de febrero, hora, campos inválidos y anticipación.
- AgendaStorageTest: crear, editar conservando ID, persistencia y cancelación del PendingIntent al eliminar.
- AlarmDeliveryTest: ignora revisiones antiguas, entrega una ocurrencia una sola vez y usa el canal anual correcto.
- NotificationFlowTest: dos avisos conservan destinos distintos, el PendingIntent abre el formulario con su ID y la cancelación retira el aviso.
- UiSmokeTest: apertura, controles, canales y formulario vacío.
- Las ExampleUnitTest/ExampleInstrumentedTest son las pruebas originales de la plantilla; no representan cobertura funcional adicional.

Para ejecutarlas: clic derecho en app/src/test > Run Tests, y app/src/androidTest con un emulador. En Terminal, desde Practica1:

```powershell
$env:JAVA_HOME = "C:/Program Files/Android/Android Studio/jbr"
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-configuration-cache --no-watch-fs
.\gradlew.bat :app:connectedDebugAndroidTest --no-configuration-cache --no-watch-fs
```

Las pruebas instrumentadas de esta práctica crean un evento con nombre de prueba y lo eliminan al terminar; no necesitan limpiar toda tu agenda.

## Dónde ver los datos para aprender

Con la app debug ejecutándose: **View > Tool Windows > Device Explorer**. Busca /data/data/com.example.lab5_practica1/shared_prefs/. Encontrarás agenda_eventos.xml y avisos_emitidos.xml. El XML es el contenedor de preferencias; dentro hay un String JSON. No lo edites mientras corre la app: úsalo para observar el resultado.

## Orden recomendado para estudiar

Primero UI y navegación; luego Event y validación; después SharedPreferences/Gson; luego Adapter/LiveData; finalmente canales/PendingIntent y AlarmManager. Si intentas aprender los receivers antes de saber leer un EditText, te complicas innecesariamente.

Referencias adicionales de la adaptación: [Alarmas de Android](https://developer.android.com/develop/background-work/services/alarms), [restricciones de alarmas exactas](https://developer.android.com/about/versions/14/changes/schedule-exact-alarms).
