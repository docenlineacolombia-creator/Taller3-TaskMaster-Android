package com.taskmaster.taller3.ui.task

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.taskmaster.taller3.data.task.Task
import com.taskmaster.taller3.data.task.TaskPreferences
import com.taskmaster.taller3.data.task.TaskRepository

/**
 * TaskViewModel – Capa de presentación (MVVM).
 *
 * Responsabilidades:
 * - Exponer LiveData<List<Task>> a los Fragments (UI reactiva).
 * - Delegar operaciones CRUD al TaskRepository.
 * - Mantener el filtro activo (todas / pendientes / completadas).
 * - Sobrevivir a rotaciones de pantalla (hereda de AndroidViewModel).
 *
 * Los Fragments observan [taskListLiveData] y se actualizan automáticamente
 * cada vez que el repositorio cambia la lista.
 */
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorio de datos (SharedPreferences + Gson)
    private val repository = TaskRepository(application)

    // Preferencias del usuario
    val preferences = TaskPreferences(application)

    // ===========================
    // LiveData privado (mutable)
    // ===========================

    /** Lista COMPLETA de tareas (fuente de verdad interna) */
    private val _allTasks = MutableLiveData<List<Task>>(repository.getAllTasks())

    /** Filtro activo: 0=Todas, 1=Pendientes, 2=Completadas */
    private val _filter = MutableLiveData(FILTER_ALL)

    /** Tarea seleccionada para edición (compartida entre Fragments) */
    private val _selectedTask = MutableLiveData<Task?>(null)

    // ===========================
    // LiveData público (inmutable)
    // ===========================

    /**
     * Lista de tareas FILTRADA según el filtro activo.
     * Los Fragments deben observar ésta, no [_allTasks].
     *
     * Se recalcula automáticamente cuando cambia [_allTasks] o [_filter].
     * Usamos [MediatorLiveData] mediante la función de extensión [map].
     */
    val taskListLiveData: LiveData<List<Task>> = _filter.map { filter ->
        applyFilter(_allTasks.value ?: emptyList(), filter)
    }

    /** Filtro activo expuesto (para que el UI pueda saber qué tab está activo) */
    val filterLiveData: LiveData<Int> = _filter

    /** Tarea seleccionada (para pasar al fragmento de detalle sin Safe Args cuando se edita) */
    val selectedTask: LiveData<Task?> = _selectedTask

    companion object {
        const val FILTER_ALL = 0
        const val FILTER_PENDING = 1
        const val FILTER_COMPLETED = 2
    }

    // ===========================
    // Operaciones CRUD
    // ===========================

    fun addTask(task: Task): Task {
        val saved = repository.addTask(task)
        refreshList()
        return saved
    }

    fun updateTask(task: Task) {
        repository.updateTask(task)
        refreshList()
    }

    fun deleteTask(taskId: Int) {
        repository.deleteTask(taskId)
        refreshList()
    }

    fun toggleTaskCompleted(taskId: Int) {
        repository.toggleTaskCompleted(taskId)
        refreshList()
    }

    fun getTaskById(id: Int): Task? = repository.getTaskById(id)

    // ===========================
    // Filtros
    // ===========================

    fun setFilter(filter: Int) {
        _filter.value = filter
        // Forzar re-emisión de la lista filtrada
        refreshList()
    }

    // ===========================
    // Selección para edición
    // ===========================

    fun selectTask(task: Task?) {
        _selectedTask.value = task
    }

    // ===========================
    // Helpers privados
    // ===========================

    /**
     * Recarga la lista desde el repositorio y notifica a los observadores de LiveData.
     */
    private fun refreshList() {
        val allTasks = repository.getAllTasks()
        _allTasks.value = allTasks
        // Forzar re-evaluación del filtro
        _filter.value = _filter.value
    }

    private fun applyFilter(tasks: List<Task>, filter: Int): List<Task> {
        return when (filter) {
            FILTER_PENDING -> tasks.filter { !it.isCompleted }
            FILTER_COMPLETED -> tasks.filter { it.isCompleted }
            else -> tasks // FILTER_ALL
        }.let { filtered ->
            // Orden por prioridad descendente, luego por fecha de creación
            when (preferences.getSortOrder()) {
                TaskPreferences.SORT_BY_PRIORITY -> filtered.sortedByDescending { it.priority }
                TaskPreferences.SORT_BY_TITLE -> filtered.sortedBy { it.title.lowercase() }
                else -> filtered.sortedByDescending { it.createdAt }
            }
        }
    }
}
