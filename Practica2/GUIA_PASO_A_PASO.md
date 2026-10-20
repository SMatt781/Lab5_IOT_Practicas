# Práctica 2: Inventario IoT con Room y avisos de stock

Tema distinto al laboratorio de agenda: aquí administras **insumos de un laboratorio**, no fechas de eventos. Puedes registrar sensores, placas y cables, cambiar cantidades, buscar, filtrar, editar y eliminar. La base es Room, local y persistente. SharedPreferences solo guarda la preferencia de filtro.

**Qué aprendes:** Entity/DAO/Database/Repository/ViewModel, RecyclerView, operaciones de fondo, preferencias y notificaciones por cambios de estado.

## Primera prueba, antes de estudiar el código

1. Abre Practica2 en Android Studio y ejecuta app.
2. Pulsa **Permitir notificaciones**.
3. Si no has creado nada, pulsa **Cargar 4 ejemplos si está vacío**.
4. Verás Sensor DHT22, Cables Dupont, ESP32 y Protoboard.
5. Activa **Mostrar solo stock bajo**. Deben quedar DHT22 y Protoboard.
6. Cierra y abre la app: el filtro sigue activado porque se guardó en preferencias.
7. Desactiva el filtro. En DHT22 pulsa +1 hasta llegar a 6: supera el mínimo 5 y se avisa que se recuperó el stock.
8. Pulsa −1: llega a 5 y entra de nuevo en stock bajo; sale un aviso importante.
9. Toca ese aviso: abre el DHT22, no cualquier otro insumo.
10. Edita su nombre o mínimo. Guarda y comprueba la tarjeta actualizada.
11. Cierra completamente la pantalla y vuelve: los cambios siguen guardados en la base.

Los ejemplos se cargan **solo si la base está vacía**; el botón no sobreescribe lo que ya creaste. La carga inicial no envía cuatro avisos automáticamente; usa los cambios de cantidad o el botón de prueba para entenderlos.


## Antes de empezar: abrir y ejecutar sin perderte

1. Abre Android Studio. Si tienes otro proyecto, ve a **File > Open**.
2. Selecciona **la carpeta de esta práctica**, no la carpeta Lab5 ni solo app. Esa carpeta contiene settings.gradle, gradlew.bat y app.
3. Si Android Studio te pregunta si confías en el proyecto, confirma solo porque es tu carpeta de trabajo.
4. Espera a **Gradle Sync**. Si no empieza: **File > Sync Project with Gradle Files**.
5. En **Settings > Build, Execution, Deployment > Build Tools > Gradle**, usa el **JDK integrado de Android Studio (jbr)**. No cambies a un Java antiguo instalado por otro programa.
6. Conserva las versiones de Gradle/AGP del proyecto que creaste. En esta computadora ya estaban Gradle 9.5.0, AGP 9.3.3 y SDK 37. Se mantuvieron; no es necesario actualizar todo para entender los ejemplos.
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


## Mapa específico del proyecto

Todos los Java principales están en app/src/main/java/com/example/lab5_practica2/.

| Archivo | Para qué existe |
|---|---|
| InventoryItem.java | Entity: representa una fila de insumos. |
| InventoryDao.java | Operaciones SQL para consultar e insertar/actualizar/eliminar. |
| InventoryDatabase.java | Construye una sola base Room. |
| InventoryRepository.java | Ejecuta las operaciones de fondo y decide avisos. |
| InventoryRules.java | Validaciones que se pueden probar sin Android. |
| InventoryViewModel.java | Entrega datos/ocupación/resultados a la pantalla. |
| InventoryAdapter.java | Repite row_inventory.xml para mostrar la lista. |
| MainActivity.java | Buscar, filtrar, botones +1/−1, crear y eliminar. |
| ItemFormActivity.java | Registrar o editar un insumo. |
| InventoryApp.java | Crear los canales al iniciar el proceso. |
| common/ | Insets, permiso de avisos y helper de notificaciones. |

XML: activity_main.xml, activity_item_form.xml, row_inventory.xml.

## Paso 1. Preparar Room después del proyecto vacío

En build.gradle de app, además de las dependencias comunes:

```groovy
implementation 'androidx.room:room-runtime:2.8.5'
annotationProcessor 'androidx.room:room-compiler:2.8.5'
```

En Java se usa **annotationProcessor**, no ksp. El compilador de Room genera la implementación del DAO y la base. Es normal que InventoryDatabase_Impl no aparezca como archivo que tú escribiste.

Sincroniza Gradle antes de continuar. Una anotación @Entity roja puede ser una dependencia no sincronizada; no la reemplaces por una clase inventada.

## Paso 2. Diseñar la pantalla principal en Design

Abre app > res > layout > activity_main.xml.

1. LinearLayout vertical raíz, width/height match_parent.
2. MaterialToolbar **toolbar** con “Inventario IoT”.
3. LinearLayout vertical con paddingHorizontal 16dp para controles.
4. TextView **summaryText** para total de insumos y cantidad con stock bajo.
5. TextInputLayout **searchEditLayout** con hint “Buscar insumo”; dentro TextInputEditText **searchEdit**.
6. MaterialSwitch **lowSwitch**, texto “Mostrar solo stock bajo (se recuerda)”.
7. MaterialButton **permissionButton**.
8. MaterialButton **addButton**.
9. MaterialButton **examplesButton**.
10. TextView **emptyText** para lista vacía o filtro sin coincidencias.
11. RecyclerView **recycler**, alto 0dp/weight 1 y width match_parent, después del bloque anterior.

En row_inventory.xml crea una MaterialCardView que contenga un LinearLayout vertical:

| ID | Tipo | Uso |
|---|---|---|
| nameText | TextView | Nombre del insumo |
| stockText | TextView | Disponibles y mínimo |
| statusText | TextView | Estado y color |
| noteText | TextView | Nota |
| removeButton | MaterialButton | Quitar una unidad |
| addButton | MaterialButton | Agregar una unidad |
| editButton | MaterialButton | Abrir edición |
| deleteButton | MaterialButton | Confirmar eliminación |
| testButton | MaterialButton | Probar notificación del registro |

Los ID pueden repetirse en layouts **distintos**: el addButton de una fila es otro objeto distinto al addButton principal. Cada binding pertenece al layout que infla. Dentro del mismo XML no debes duplicar un ID para controles diferentes.

Para cada pareja de botones se utilizó LinearLayout horizontal con cada ancho 0dp y weight 1. Así se reparten el ancho; su altura sigue siendo wrap_content.

## Paso 3. Diseñar el formulario

Abre activity_item_form.xml.

1. LinearLayout vertical raíz.
2. Toolbar **toolbar**.
3. NestedScrollView con un solo LinearLayout vertical dentro y padding 16dp.
4. TextInputLayout **nameEditLayout** y su EditText **nameEdit**: textCapSentences, maxLength 80.
5. TextInputLayout **quantityEditLayout** y EditText **quantityEdit**: inputType number.
6. TextInputLayout **minimumEditLayout** y EditText **minimumEdit**: inputType number.
7. TextInputLayout **noteEditLayout** y EditText **noteEdit**: textMultiLine, minLines 2, maxLength 300.
8. TextView de ayuda.
9. MaterialButton **saveButton**.

number muestra un teclado numérico; **no reemplaza una validación en Java**. El usuario podría pegar contenido inválido, dejar vacío o introducir un valor enorme.

## Paso 4. Entity: qué significa que una clase sea tabla

InventoryItem lleva:

```java
@Entity(tableName = "insumos")
public class InventoryItem {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public String name = "";
    public int quantity;
    public int minQuantity;
    @NonNull public String note = "";
}
```

- Una instancia es un insumo.
- Cada instancia guardada es una fila.
- id distingue dos insumos aunque tengan nombres iguales.
- autoGenerate permite que SQLite asigne el ID.
- @NonNull impide null, **no impide un texto vacío**; eso lo revisa InventoryRules.
- quantity y minQuantity son enteros porque hablamos de unidades, no de dinero.
- low() evalúa **quantity <= minQuantity**.

No confundas el modelo con un TextView: el modelo tiene datos; el TextView representa esos datos visualmente.

## Paso 5. DAO: las órdenes a la base

Abre InventoryDao.java. Están:

```java
@Query("SELECT * FROM insumos ORDER BY name COLLATE NOCASE")
LiveData<List<InventoryItem>> observeAll();

@Query("SELECT * FROM insumos WHERE id=:id")
InventoryItem find(long id);

@Insert long insert(InventoryItem item);
@Update int update(InventoryItem item);
@Delete int delete(InventoryItem item);
```

observeAll devuelve una lista observable, no una lista escrita manualmente. Si la tabla cambia, Room vuelve a consultar y LiveData entrega el resultado a la pantalla.

find busca un ID concreto. insert devuelve el ID nuevo. update/delete devuelven cuántas filas afectaron. Esto ayuda a detectar un elemento eliminado en vez de decir que se guardó cuando no ocurrió.

## Paso 6. RoomDatabase: crear la base una vez

InventoryDatabase usa version 1 y nombre **inventario.db**:

```java
Room.databaseBuilder(
    context.getApplicationContext(),
    InventoryDatabase.class,
    "inventario.db"
).build();
```

El singleton comparte esa conexión. No se reconstruye una base en cada tarjeta ni en cada clic.

No se usa fallbackToDestructiveMigration: borrar datos al cambiar versión sería un mal ejemplo. Si después agregas columnas, debes subir la versión y crear una migración; no elimines la base de tus usuarios como solución automática.

## Paso 7. Repository: qué pasa al guardar

InventoryRepository.save:

1. Revisa nombre, cantidad, mínimo y nota.
2. Quita espacios innecesarios del nombre.
3. Si id es cero, crea un registro.
4. Si id no es cero, verifica que siga existiendo y actualiza.
5. Espera a que Room termine la escritura.
6. Evalúa si cambió el estado de stock.
7. Devuelve resultado al hilo principal.

Los métodos se ejecutan desde un **ExecutorService de un solo hilo**. Room no permite consultar/escribir en el hilo principal por defecto y no se quitó esa protección.

El ViewModel llama al Repository y cambia busy. La Activity observa busy para deshabilitar guardados repetidos.

### Por qué +1 y −1 no usan solo el número dibujado

En InventoryRepository.change se vuelve a buscar el registro dentro de una **transacción**:

```java
db.runInTransaction(() -> {
    InventoryItem item = dao.find(id);
    item.quantity = Math.addExact(item.quantity, amount);
    dao.update(item);
});
```

El método real además valida que exista, copia el estado anterior y comprueba límites. Consultar y actualizar como una unidad evita basarse en una tarjeta visual que todavía no se refrescó. No se permite cantidad negativa, y el botón −1 queda deshabilitado al llegar a cero.

Math.addExact detecta un desbordamiento en lugar de convertir accidentalmente un entero grande en negativo.

## Paso 8. Formulario: del campo al objeto

ItemFormActivity.save lee:

```java
String name = b.nameEdit.getText().toString();
int quantity = InventoryRules.parse(b.quantityEdit.getText().toString());
int minimum = InventoryRules.parse(b.minimumEdit.getText().toString());
```

parse rechaza vacío, decimales, negativos y valores mayores a 1000000. Luego validate revisa nombre y valores. Si falla, muestra error y **no llama a Room**.

Se crea InventoryItem, se asigna id si es edición y se envía al ViewModel. Cuando el resultado es correcto, finish devuelve a la lista. No es necesario relanzar MainActivity para verla actualizada: Room/LiveData notifican al observador.

El ID para editar se envía como **item_id, tipo long**, y se lee con getLongExtra. No uses getIntExtra para leer un long: son tipos distintos.

## Paso 9. Lista, búsqueda y preferencia

MainActivity.render obtiene los insumos de LiveData. Aplica:

1. Coincidencia del nombre con lo escrito en searchEdit.
2. Si lowSwitch está activo, solo los que low() indica.
3. Entrega la lista visible al Adapter.
4. Muestra texto de vacío cuando no hay coincidencias.

El buscador filtra la lista cargada; no modifica registros ni envía una petición a Internet.

Para recordar el filtro:

```java
prefs = getSharedPreferences("inventario_config", MODE_PRIVATE);
boolean lowOnly = prefs.getBoolean("solo_bajo", false);
prefs.edit().putBoolean("solo_bajo", checked).apply();
```

Aquí apply es apropiado: es una preferencia pequeña, no una escritura de inventario que necesita confirmar éxito antes de cerrar un formulario.

La búsqueda no se guarda permanentemente para evitar volver días después a una lista aparentemente vacía por un texto olvidado. Android sí conserva el campo durante recreaciones normales.

## Paso 10. Notificaciones por transiciones

No hay alarmas por fecha en este proyecto. Los avisos se publican **cuando cambias o guardas un insumo**:

| Antes | Después | Resultado |
|---|---|---|
| Nuevo registro | Cantidad <= mínimo | Aviso HIGH de stock bajo |
| Cantidad > mínimo | Cantidad <= mínimo | Aviso HIGH |
| Ya estaba bajo | Sigue bajo | No repetir por cada decremento |
| Cantidad <= mínimo | Cantidad > mínimo | Aviso DEFAULT de recuperación |
| Cualquier estado | Eliminado | Cancelar avisos del insumo |

Así practicas una decisión real, no una notificación desconectada de los datos.

La etiqueta **stock-ID** identifica el aviso real; **test-stock-ID** identifica una prueba. El PendingIntent manda item_id al ItemFormActivity. Por eso dos avisos distintos abren sus propios registros.

Editar el mínimo también puede cruzar el umbral y avisar. Por ejemplo cantidad 5/mínimo 2 es suficiente; editar mínimo a 6 lo convierte en bajo.

## Pruebas manuales completas

### A. Alta y persistencia

1. Registra “Resistencia 220 ohm”, cantidad 10 y mínimo 5.
2. Cierra y abre: debe seguir.
3. Edita la nota y guarda.
4. Ve a otra pantalla y vuelve: nota y cantidades correctas.
5. Intenta cargar ejemplos teniendo datos: no se sobreescriben.
6. Elimina un registro con confirmación; solo desaparece ese registro.

### B. Límites

1. Nombre vacío: error.
2. Cantidad vacía: error.
3. Pega “1.5” o un valor muy grande: error.
4. Guarda cantidad cero: permitido y se considera bajo.
5. En cero, −1 no debe estar disponible.
6. Rota con texto sin guardar: se conserva.
7. Cambia el mínimo: el estado de la tarjeta debe recalcularse.

### C. Filtro y búsqueda

1. Ten un insumo bajo y otro suficiente.
2. Busca parte de uno de sus nombres.
3. Borra el texto: vuelven ambos.
4. Activa solo bajos: aparece el bajo.
5. Cierra y abre: el switch mantiene su valor.
6. Desactiva filtro si crees que “faltan” datos: podrían estar ocultos, no eliminados.

### D. Avisos

1. Permite notificaciones.
2. Crea “ESP32 de prueba”, cantidad 3/mínimo 2.
3. Pulsa −1: llega a 2, aviso HIGH.
4. Pulsa −1 otra vez: sigue bajo, no debe repetir por la transición.
5. Pulsa +1 hasta llegar a 3: aviso DEFAULT de recuperación.
6. Toca el aviso: abre exactamente ese insumo.
7. Usa Probar aviso de otro insumo y comprueba su destino.
8. Elimina el insumo: se cancelan sus avisos.
9. Rechaza notificaciones y guarda otro: el dato se guarda aunque no salga aviso.

## Inspeccionar Room y preferencias

Con la app ejecutándose: **View > Tool Windows > App Inspection > Database Inspector**. Selecciona el proceso com.example.lab5_practica2 y la base inventario.db. La tabla insumos muestra las filas reales.

Con Device Explorer: /data/data/com.example.lab5_practica2/databases/ contiene la base y archivos auxiliares WAL/SHM. No copies solo un archivo mientras hay escrituras para hacer un respaldo manual.

El filtro se guarda aparte en /data/data/com.example.lab5_practica2/shared_prefs/inventario_config.xml. **Preferencias y Room no son lo mismo** aunque ambos sean locales.

## Pruebas automáticas incluidas

- InventoryRulesTest: ceros, vacíos, negativos, decimales y límites.
- RoomStorageTest: insertar, consultar, actualizar, eliminar y rollback de una transacción fallida. Usa una base **en memoria exclusiva de la prueba**, no borra tu inventario.
- UiSmokeTest: abrir Activity, controles, canales y formulario vacío.
- NotificationFlowTest: comprueba avisos independientes, apertura del formulario con el ID correcto y cancelación.
- Los tests Example son los de la plantilla y no sustituyen los anteriores.

Desde la Terminal de Practica2:

```powershell
$env:JAVA_HOME = "C:/Program Files/Android/Android Studio/jbr"
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --no-configuration-cache --no-watch-fs
.\gradlew.bat :app:connectedDebugAndroidTest --no-configuration-cache --no-watch-fs
```

## Cómo explicarlo al profesor sin memorizar el código

“Dibujé una sola fila para RecyclerView. Mi Entity representa los insumos; el DAO contiene las consultas y Room genera su implementación. El Repository ejecuta cambios fuera del hilo principal. LiveData avisa a la Activity, que actualiza el Adapter. El filtro se conserva en SharedPreferences. Cuando la cantidad cruza el mínimo se crea una notificación con su canal e ID; al tocarla se abre ese insumo.”

Como ejercicio adicional, cambia el tema a libros prestados o productos de una tienda, manteniendo la estructura. Primero entiende qué datos cambian y cuál condición merece un aviso.
