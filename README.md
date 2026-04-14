# 🎯 TaskMaster – Taller 3

**App Android de Lista de Tareas con Fragments, Persistencia y Recordatorios**

![Android](https://img.shields.io/badge/Android-API%2033+-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange)
![Navigation](https://img.shields.io/badge/Navigation-Safe%20Args-blue)

---

## 📱 Pantallas de la App

| TaskListFragment | TaskDetailFragment | SettingsFragment |
|---|---|---|
| Lista de tareas con RecyclerView | Creación/edición de tarea | Configuración de la app |
| FAB para agregar tarea | Selector de hora, switch de recordatorio | Preferencias del usuario |
| Menú de filtros (todas/pendientes/completadas) | Safe Args para pasar datos | Tema claro/oscuro |

---

## 🏗️ Arquitectura

```
app/
├── data/
│   └── task/
│       ├── Task.kt              ← data class
│       ├── TaskRepository.kt    ← SharedPreferences + Gson
│       └── TaskPreferences.kt   ← preferencias globales
├── ui/
│   └── task/
│       ├── TaskViewModel.kt     ← LiveData + lógica
│       ├── TaskListFragment.kt  ← lista principal
│       ├── TaskDetailFragment.kt ← crear/editar
│       ├── SettingsFragment.kt  ← configuración
│       └── TaskAdapter.kt       ← RecyclerView adapter
└── receiver/
    └── TaskReminderReceiver.kt  ← BroadcastReceiver
```

---

## ✅ Taller 3 – Lo que se implementó

### Fragments (3 screens)
- **TaskListFragment**: Lista de tareas con RecyclerView, filtros por estado, FAB para agregar
- **TaskDetailFragment**: Formulario de creación/edición con TimePicker, switch de recordatorio
- **SettingsFragment**: Preferencias de usuario con tema y nombre

### Navigation Component + Safe Args
- `nav_graph.xml` con 3 destinos y acciones definidas
- `TaskListFragmentDirections` → `TaskDetailFragmentArgs` para pasar `taskId` con Safe Args
- `navigateUp()` para volver al listado

### SharedPreferences
- `TaskRepository` serializa/deserializa la lista de tareas como JSON usando Gson
- `TaskPreferences` guarda configuración del usuario (nombre, tema)
- Los datos persisten al cerrar y reabrir la app

### LiveData
- `TaskViewModel` expone `taskListLiveData: LiveData<List<Task>>`
- Los Fragments observan los cambios y actualizan la UI automáticamente
- `filterLiveData` para filtrar tareas en tiempo real

### BroadcastReceiver
- `TaskReminderReceiver` hereda de `BroadcastReceiver`
- Recibe el `Intent` con los datos de la tarea y **muestra una notificación local**
- `AlarmManager` programa la notificación para 30 segundos después de guardar la tarea con recordatorio activo
- Canal de notificaciones `REMINDER_CHANNEL` creado en `Application` class

### Opción de recordatorio elegida: **Notificación local** ✅

---

## 🚀 Cómo ejecutar

1. Clonar el repositorio: `git clone https://github.com/docenlineacolombia-creator/Taller3-TaskMaster-Android.git`
2. Abrir con Android Studio Hedgehog o superior
3. Esperar sincronización de Gradle
4. Ejecutar en emulador API 33+ o dispositivo físico

---

## 📸 Capturas de pantalla

Ver carpeta `docs/` para capturas del funcionamiento.

---

## 📦 Dependencias principales

```gradle
navigation-fragment-ktx:2.7.6
navigation-ui-ktx:2.7.6
navigation-safe-args-gradle-plugin:2.7.6
lifecycle-viewmodel-ktx:2.7.0
lifecycle-livedata-ktx:2.7.0
gson:2.10.1
material:1.11.0
```

---

## 👨‍💻 Historial de Commits

- `feat: estructura inicial del proyecto Android - Taller 3`
- `feat: data layer - Task model, Repository con SharedPreferences, LiveData`
- `feat: navigation graph con Safe Args y 3 fragments`
- `feat: TaskListFragment con RecyclerView y filtros`
- `feat: TaskDetailFragment con formulario y BroadcastReceiver`
- `feat: SettingsFragment y TaskPreferences`
- `feat: TaskReminderReceiver - notificación local con AlarmManager`
- `docs: README.md actualizado con documentación completa`
