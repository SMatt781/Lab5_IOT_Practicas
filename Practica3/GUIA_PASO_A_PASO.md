# Práctica 3: Mi presupuesto, archivos JSON y notificaciones

Tema diferente a la agenda y al inventario: registro de **gastos mensuales**. Se guardan en un archivo JSON privado, no en Room. El presupuesto de cada mes se guarda en SharedPreferences. Puedes exportar/importar con el selector de archivos de Android y recibir avisos al registrar gastos o alcanzar límites.

Se eligió este ejemplo para practicar archivos, Gson, ContentResolver/URI, preferencias y canales dentro de un solo flujo de varias pantallas.

## Primera prueba guiada

1. Abre Practica3 en Android Studio y ejecuta app.
2. Pulsa **Permitir notificaciones**.
3. El mes actual comienza con presupuesto S/ 500.00. Escribe **100** y pulsa Guardar presupuesto.
4. Pulsa **Registrar gasto**.
5. Concepto: “Almuerzo”. Importe: **30.50**. Fecha: hoy. Categoría: Alimentación. Guarda.
6. En el resumen verás gastado 30.50 y disponible 69.50.
7. Crea otro gasto por **50**. Total 80.50: se emite advertencia de al menos 80%.
8. Crea otro por **25**. Total 105.50: se emite aviso importante de presupuesto alcanzado o superado.
9. Toca el aviso de presupuesto: abre el mes correspondiente en el resumen.
10. Pulsa Exportar JSON, elige una carpeta/nombre en el selector y guarda.
11. Pulsa Importar JSON y elige el mismo archivo: los gastos no se duplican, porque ya tienen sus mismos ID.
12. Cierra y abre la app: archivo y presupuesto se recuperan.

El presupuesto no es un consejo financiero: es un dato de práctica que tú eliges. Los importes y límites son para aprender programación.


## Antes de empezar: abrir y ejecutar sin perderte

1. Abre Android Studio. Si tienes otro proyecto, ve a **File > Open**.
2. Selecciona **la carpeta de esta práctica**, no la carpeta Lab5 ni solo app. Esa carpeta contiene settings.gradle, gradlew.bat y app.
3. Si Android Studio te pregunta si confías en el proyecto, confirma solo porque es tu carpeta de trabajo.
4. Espera a **Gradle Sync**. Si no empieza: **File > Sync Project with Gradle Files**.
5. En **Settings > Build, Execution, Deployment > Build Tools > Gradle**, usa el **JDK integrado de Android Studio (jbr)**. No cambies a un Java antiguo instalado por otro programa.
6. En la rama Version_panda3 conserva AGP 9.1.1, Gradle 9.3.1 y compileSdk 36.1 (targetSdk 36). Instala Android SDK Platform 36.1 y Build-Tools 36.0.0 desde Tools > SDK Manager. Usa el JDK integrado de Panda 3 (17 o superior compatible con Gradle); no se exige Java 25. Consulta COMPATIBILIDAD_PANDA3.md en la carpeta Lab5 para el paso a paso. No aceptes actualizar AGP a 9.3 si trabajarás con Panda 3.
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


## Mapa específico

Todos los Java principales están en app/src/main/java/com/example/lab5_practica3/.

| Archivo | Responsabilidad |
|---|---|
| Expense.java | Registro de un gasto con UUID, fecha e importe en céntimos. |
| MoneyRules.java | Parsear/importes, validar y determinar nivel de presupuesto. |
| LedgerCodec.java | Convertir JSON, validar su formato y unir sin duplicados. |
| ExpenseStore.java | Leer/escribir el archivo privado con AtomicFile. |
| ExpenseRepository.java | Operaciones en segundo plano, preferencias, SAF y avisos. |
| ExpenseViewModel.java | Ocupación y resultado para las pantallas. |
| ExpenseAdapter.java | Mostrar cada gasto con row_expense.xml. |
| MainActivity.java | Mes, resumen, presupuesto, importar/exportar y lista. |
| ExpenseFormActivity.java | Formulario de alta/edición. |
| BudgetApp.java | Crear canales al iniciar. |
| common/ | Helpers de interfaz y notificaciones. |

XML: activity_main.xml, activity_expense_form.xml, row_expense.xml.

## Paso 1. Diseñar MainActivity en Design

Abre app > res > layout > activity_main.xml.

1. LinearLayout vertical raíz match_parent.
2. MaterialToolbar **toolbar**.
3. LinearLayout vertical interior con paddingHorizontal 16dp.
4. Una fila horizontal de mes:
   - MaterialButton **previousButton**.
   - TextView **monthText**, ancho 0dp/weight 1, gravity center.
   - MaterialButton **nextButton**.
5. TextView **summaryText**, donde Java escribirá gastado, presupuesto y disponible en líneas separadas.
6. **LinearProgressIndicator**, ID **progress**, max 100. No es un TextView ni una imagen estática.
7. TextInputLayout **budgetEditLayout** con hint “Presupuesto del mes (S/)” y TextInputEditText **budgetEdit**, inputType numberDecimal.
8. MaterialButton **budgetButton**.
9. MaterialButton **permissionButton**.
10. MaterialButton **addButton**.
11. Fila horizontal con **exportButton** e **importButton**, ambos MaterialButton con ancho 0dp/weight 1.
12. TextView **emptyText**.
13. RecyclerView **recycler** fuera del bloque, alto 0dp y weight 1.

La barra muestra el porcentaje consumido con un máximo visual de 100. El texto sí puede mostrar que gastaste más que tu presupuesto: disponible se vuelve negativo. No se recorta el dato real a 100%.

## Paso 2. Diseñar el formulario

Abre activity_expense_form.xml.

1. LinearLayout vertical raíz y MaterialToolbar.
2. NestedScrollView con un único LinearLayout vertical interno.
3. TextInputLayout **titleEditLayout**, hint “Concepto del gasto”, con **titleEdit**, textCapSentences/maxLength 100.
4. TextInputLayout **amountEditLayout**, hint “Importe (S/)”, con **amountEdit**, numberDecimal.
5. MaterialButton **dateButton**.
6. Desplegable MaterialAutoCompleteTextView **categoryInput**, dentro de TextInputLayout de menú expuesto.
7. TextInputLayout **noteEditLayout** con **noteEdit**, textMultiLine, minLines 2, maxLength 300.
8. MaterialButton **saveButton**.

Las categorías se cargan desde Java: Alimentación, Transporte, Estudios, Hogar y Otros. El menú no acepta una categoría arbitraria, porque el validador espera una de estas.

En row_expense.xml la tarjeta contiene titleText, amountText, detailText, noteText y botones editButton, deleteButton y testButton. El Adapter repite esta fila.

## Paso 3. Modelo y céntimos: evitar errores de dinero

Un gasto tiene:

```java
public class Expense {
    public String id, date, title, category, note;
    public long cents;
}
```

**No se guardó el importe como double.** El número decimal 0.1 no siempre tiene representación binaria exacta. Guardar **céntimos enteros** hace que S/ 12.50 sea exactamente 1250.

La conversión está en MoneyRules.parse:

```java
long value = new BigDecimal(text.replace(',', '.'))
    .movePointRight(2)
    .longValueExact();
```

El código real valida el formato antes de convertir. Admite coma o punto decimal, hasta dos decimales, importe positivo y un límite. No acepta separadores de miles: escribe **1500.50**, no 1,500.50.

Para mostrar se usa MoneyRules.format; para rellenar el EditText al editar, editable devuelve un texto sin símbolo ni separadores.

El UUID es un identificador aleatorio estable. No depende de la posición de la tarjeta ni cambia al editar. Dos gastos con el mismo título pueden ser registros distintos.

## Paso 4. Guardar JSON dentro de la app

ExpenseStore crea:

```java
File dir = new File(context.getFilesDir(), "finanzas");
AtomicFile file = new AtomicFile(new File(dir, "gastos.json"));
```

getFilesDir es almacenamiento **interno privado de la app**. No es una carpeta pública Downloads; por eso no necesita permiso general de almacenamiento ni Internet.

El JSON tiene esta estructura conceptual:

```json
{
  "version": 1,
  "expenses": [
    {
      "id": "un-UUID-real-generado-por-Java",
      "date": "2026-10-20",
      "title": "Almuerzo",
      "category": "Alimentación",
      "note": "",
      "cents": 1250
    }
  ]
}
```

El ID anterior es explicativo: no pegues ese texto como UUID válido. La aplicación genera los correctos con UUID.randomUUID().toString().

version permite identificar el formato de archivo; no es la versión de Android ni de la app.

### Por qué AtomicFile y no escribir encima sin protección

Una escritura puede fallar por falta de espacio o interrupción. AtomicFile administra un archivo temporal y recuperación para no dejar medio JSON como si fuera correcto.

ExpenseStore.write:

1. Convierte la lista a JSON antes de abrir la escritura.
2. Codifica en UTF-8.
3. Comprueba el límite didáctico de 2 MB.
4. startWrite abre la operación.
5. Escribe los bytes.
6. finishWrite confirma.
7. Si hay IOException, failWrite revierte.

AtomicFile es una mejora pequeña sobre FileOutputStream de clase; sigue siendo archivo local. No es una base de datos externa. El Executor de un hilo evita que dos escrituras de esta app se mezclen.

### Leer todo el archivo, no solo la primera línea

ExpenseStore.read usa openRead y try-with-resources para cerrar el flujo. readText lee bloques hasta terminar y limita el tamaño. Gson puede producir JSON de varias líneas; usar solo readLine una vez dejaría información fuera.

Si no existe el archivo, devuelve lista vacía. Si existe pero está dañado, informa el error: no lo transforma silenciosamente en una lista vacía y luego lo sobreescribe.

## Paso 5. Validación del JSON y de formularios

MoneyRules.validate revisa:

- UUID y fecha válidos.
- Concepto de 1 a 100 caracteres.
- Importe positivo dentro del límite.
- Categoría reconocida.
- Nota de hasta 300 caracteres.

LedgerCodec.decode también exige objeto con version y expenses, versión 1, máximo 10000 registros y ningún ID duplicado dentro del archivo.

La validación se hace **antes de escribir**. Si una importación falla no se reemplaza el contenido actual por un JSON incorrecto.

En ExpenseFormActivity.save se leen los campos, se construye Expense, se valida y se llama al ViewModel. El formulario solo termina al confirmarse la escritura.

## Paso 6. Preferencias por mes

ExpenseRepository usa SharedPreferences **presupuestos**. Cada mes tiene una clave:

```text
budget_2026-10
budget_2026-11
```

El valor es long en céntimos. Si nunca configuraste un mes, usa 50000 = S/ 500.00.

Al guardar se valida el mes con YearMonth.parse y el importe con MoneyRules. Se hace commit en el hilo de trabajo para verificar la escritura. Cambiar de mes no modifica el presupuesto del mes anterior.

**Exportar gastos no exporta presupuestos.** Se decidió separarlos para que distingas el archivo JSON de las preferencias. La importación de gastos no sobreescribe los presupuestos configurados.

## Paso 7. Filtrar por mes y recalcular

MainActivity mantiene un YearMonth. Las flechas usan minusMonths(1) o plusMonths(1).

1. La fecha ISO de cada gasto permite reconocer a qué yyyy-MM pertenece.
2. ExpenseRepository.total suma los céntimos de ese mes.
3. Se calcula disponible = presupuesto − gastado.
4. progress muestra min(100, gastado * 100 / presupuesto).
5. El Adapter recibe solo los gastos del mes seleccionado.
6. Los gastos se ordenan por fecha descendente.

Si no ves un gasto recién creado, revisa el mes: puede estar en otro, no se perdió. Registrar desde un mes distinto al actual propone el primer día de ese mes; puedes cambiar la fecha.

El resultado se recalcula cada vez que LiveData cambia; no se guarda un total separado que podría quedar desactualizado.

## Paso 8. Editar y navegar

MainActivity.edit envía:

```java
startActivity(new Intent(this, ExpenseFormActivity.class)
    .putExtra("expense_id", e.id));
```

El ID aquí es **String**, por eso se usa getStringExtra. Compara con la práctica 1 (int) y 2 (long): el tipo del extra debe coincidir al escribir y leer.

El formulario carga ese registro una vez. Al guardar elimina de la lista en memoria el objeto con ese ID y agrega su nueva versión. El ID se conserva. Luego se escribe el archivo completo y se actualiza LiveData.

Eliminar pide confirmación, quita solo ese UUID, guarda la nueva lista, cancela su aviso de gasto y recalcula los límites del mes.

## Paso 9. Exportar con SAF: qué significa una URI

El botón Exportar utiliza un contrato moderno equivalente al flujo ACTION_CREATE_DOCUMENT explicado en clase:

```java
new ActivityResultContracts.CreateDocument("application/json")
```

1. Android abre su selector.
2. Tú eliges nombre y carpeta.
3. Android devuelve una **Uri**, no una ruta File normal.
4. El Repository abre esa Uri con ContentResolver.openOutputStream.
5. Escribe el JSON completo en UTF-8 y cierra el flujo.
6. La pantalla informa éxito o error.

No conviertas uri.getPath() a File para intentar escribir: muchas Uri son content:// y pertenecen a un proveedor. Por eso existe ContentResolver.

No se pide READ_EXTERNAL_STORAGE ni MANAGE_EXTERNAL_STORAGE. El usuario elige explícitamente el documento que la app puede usar.

## Paso 10. Importar sin duplicar

OpenDocument abre el selector. Se confirma antes de importar.

1. Se abre InputStream desde ContentResolver.
2. Se lee como máximo 2 MB.
3. Se valida **todo** el documento.
4. Se recupera la lista actual desde el archivo interno.
5. LedgerCodec.merge une por UUID.
6. Los ID que ya existen conservan **el registro local**.
7. Los nuevos se agregan.
8. Se escribe de forma atómica y se actualiza la pantalla.

Importar dos veces el mismo archivo es **idempotente**: produce la misma lista, no el doble de gastos.

Esto **no es una sincronización completa entre celulares**. Si editaste el mismo UUID en dos lugares, el dato local gana en esta práctica. Se explica así para no prometer resolución de conflictos que no existe. Tampoco se exportan adjuntos ni fotos.

El selector concede acceso para esta operación; no se conserva un acceso permanente al documento porque no se necesita volver a abrirlo días después sin que lo elijas.

## Paso 11. Avisos de gasto y presupuesto

Hay dos tipos de destino:

- Aviso de gasto: abre ExpenseFormActivity con expense_id.
- Aviso de presupuesto: abre MainActivity con month para mostrar el mes afectado.

Reglas:

| Cambio | Canal |
|---|---|
| Nuevo gasto guardado | DEFAULT, aviso con su concepto e importe |
| Pasar de menos del 80% a 80%-99% | DEFAULT, advertencia |
| Llegar al 100% o superarlo | HIGH, presupuesto alcanzado/superado |
| Bajar de nivel al eliminar/editar o ampliar presupuesto | DEFAULT, estado actualizado |

Se avisa al **cambiar de nivel**, no en cada actualización de pantalla. Registrar un gasto de otro mes calcula el presupuesto de ese otro mes.

La etiqueta budget-yyyy-MM actualiza el aviso del mes; expense-UUID distingue cada gasto. Un aviso importante puede coincidir con el normal del gasto que causó el límite: representan dos cosas distintas.

Cambiar el presupuesto también puede cruzar niveles. Si gastaste 90 y lo reduces de 200 a 100, entras en advertencia aunque no hayas creado un gasto nuevo.

No hay AlarmManager aquí: el evento relevante es la operación que acabas de realizar, no una hora futura.

## Pruebas manuales completas

### A. Importes y persistencia

1. Registra 12.50 y 0.10: total 12.60.
2. Prueba 12,50 si tu teclado permite coma.
3. Intenta 0, negativo, vacío y tres decimales: no se guarda.
4. Edita 12.50 a 15: el total cambia, no aparece un gasto duplicado.
5. Cierra y abre: todo se conserva.
6. Rota durante edición: el texto no debe perderse.

### B. Meses

1. Configura presupuesto 100 en el mes actual.
2. Cambia al siguiente: usa su propio presupuesto, inicialmente 500.
3. Configura 200 allí.
4. Vuelve al anterior: sigue en 100.
5. Registra un gasto con fecha del siguiente mes.
6. Comprueba que solo aparece al seleccionar ese mes.

### C. Notificaciones

1. Permite avisos, presupuesto 100.
2. Registra 79: aviso normal de gasto.
3. Registra 1: entra en 80%, advertencia.
4. Registra 20: alcanza 100%, aviso HIGH.
5. Toca aviso de gasto: abre ese registro.
6. Toca aviso de presupuesto: abre el mes correcto.
7. Elimina el gasto de 20: baja a advertencia.
8. Aumenta presupuesto a 300: vuelve a nivel disponible.
9. Si rechazas permiso, todos los cambios se guardan igualmente.

### D. Exportación e importación

1. Crea dos gastos.
2. Exporta a una carpeta elegida y confirma el mensaje de operación completada.
3. Revisa el archivo con un editor: version y expenses, importes en cents.
4. Importa el mismo archivo dos veces: no se duplican.
5. Para simular otro origen, crea un gasto nuevo, expórtalo, elimina **solo ese gasto** en la app e importa el archivo: vuelve a agregarse.
6. Cambia a mano version a 99 en una copia del archivo; intenta importar: debe fallar y dejar los datos actuales.
7. Intenta elegir un archivo que no sea JSON válido.
8. Cancela el selector: no debe guardar/importar nada.
9. Los presupuestos no cambian al importar.
10. Evita usar datos personales reales en los ejemplos que compartirás.

## Dónde observar el archivo y las preferencias

En Device Explorer: /data/data/com.example.lab5_practica3/files/finanzas/gastos.json. Ahí ves el JSON privado real.

En shared_prefs/presupuestos.xml ves valores long por mes. Un archivo exportado está donde **tú** elegiste en el selector y no necesariamente en la carpeta privada.

No edites el JSON privado mientras la app está escribiendo. Para aprender, observa una copia o utiliza el flujo de exportación.

## Pruebas automáticas incluidas

- BudgetRulesTest: precisión en céntimos, formatos, límites, umbrales, JSON, UUID duplicados y fusión idempotente.
- JsonStorageTest: persistencia al recrear el Store y rechazo de formato inválido sin sobreescribir.
- UiSmokeTest: pantallas, controles, canales y formulario vacío.
- NotificationFlowTest: verifica el canal, el ID enviado al formulario y la cancelación del aviso.
- Los Example son pruebas originales de la plantilla.

Desde la Terminal de Practica3:

```powershell
$env:JAVA_HOME = "C:/Program Files/Android/Android Studio/jbr"
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --no-configuration-cache --no-watch-fs
.\gradlew.bat :app:connectedDebugAndroidTest --no-configuration-cache --no-watch-fs
```

El test del archivo toma una copia de los registros y los restaura al terminar. Haz pruebas de este tipo en un emulador de estudio y no mientras editas datos importantes en el mismo proceso.

## Cómo estudiarlo sin aprender todo a la vez

1. Solo crea un gasto en memoria y muéstralo.
2. Añade validación de dos campos.
3. Conviértelo a JSON y observa el String.
4. Escríbelo en el archivo y recupéralo al reiniciar.
5. Añade edición con UUID.
6. Añade presupuesto en preferencias.
7. Calcula el umbral.
8. Publica un aviso inmediato.
9. Añade PendingIntent al registro correcto.
10. Por último practica exportación/importación con Uri.

Así separas “diseñar”, “guardar”, “avisar” y “compartir un archivo”, aunque en la aplicación terminada trabajen juntos.
