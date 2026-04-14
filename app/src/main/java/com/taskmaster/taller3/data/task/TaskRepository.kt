package com.taskmaster.taller3.data.task

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Repositorio de tareas.
 *
 * Responsabilidades:
 * 1. Mantener una lista de tareas en memoria (fuente de verdad en tiempo de ejecución).
 * 2. Persistir los cambios en SharedPreferences usando JSON (Gson).
 * 3. Reconstruir la lista desde SharedPreferences al iniciar la app.
 *
 * Patrón: Repository (capa de datos desacoplada de la UI).
 */
class TaskRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "tasks_prefs"
        private const val KEY_TASK_LIST = "task_list"
        private const val KEY_NEXT_ID = "next_task_id"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    // Lista mutable en memoria: se carga desde SharedPreferences al instanciar
    private var tasksInMemory: MutableList<Task> = loadTasksFromPrefs()

    // Contador de ID autoincrementable, persistido también en SharedPreferences
    private var nextId: Int = prefs.getInt(KEY_NEXT_ID, 1)

    // ==============================
    // Operaciones CRUD públicas
    // ==============================

    /**
     * Devuelve una copia inmutable de la lista de tareas.
     * Usamos una copia para evitar que el ViewModel mute la lista directamente.
     */
    fun getAllTasks(): List<Task> = tasksInMemory.toList()

    /**
     * Agrega una nueva tarea con un ID autogenerado.
     * @param task Tarea a agregar (el campo id será reemplazado por el id generado).
     * @return La tarea con el ID definitivo asignado.
     */
    fun addTask(task: Task): Task {
        val newTask = task.copy(id = nextId++)
        tasksInMemory.add(newTask)
        persist()
        return newTask
    }

    /**
     * Actualiza una tarea existente (identifica por id).
     * Si no existe ninguna tarea con ese id, no hace nada.
     */
    fun updateTask(updated: Task) {
        val index = tasksInMemory.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            tasksInMemory[index] = updated
            persist()
        }
    }

    /**
     * Elimina una tarea por id.
     */
    fun deleteTask(taskId: Int) {
        tasksInMemory.removeAll { it.id == taskId }
        persist()
    }

    /**
     * Busca una tarea por id. Retorna null si no existe.
     */
    fun getTaskById(id: Int): Task? = tasksInMemory.find { it.id == id }

    /**
     * Alterna el estado completado/pendiente de una tarea.
     */
    fun toggleTaskCompleted(taskId: Int) {
        val task = getTaskById(taskId) ?: return
        updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    // ==============================
    // Persistencia (privado)
    // ==============================

    /**
     * Lee la lista de tareas desde SharedPreferences.
     * El JSON almacenado es deserializado a List<Task> usando Gson.
     * Si no hay datos o el JSON está corrupto, devuelve una lista vacía.
     */
    private fun loadTasksFromPrefs(): MutableList<Task> {
        val json = prefs.getString(KEY_TASK_LIST, null) ?: return mutableListOf()
        val type = object : TypeToken<List<Task>>() {}.type
        return try {
            val list: List<Task> = gson.fromJson(json, type)
            list.toMutableList()
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
    }

    /**
     * Serializa la lista de tareas en memoria a JSON y la guarda en SharedPreferences.
     * También persiste el contador de ID para que no se repitan IDs entre sesiones.
     */
    private fun persist() {
        prefs.edit()
            .putString(KEY_TASK_LIST, gson.toJson(tasksInMemory))
            .putInt(KEY_NEXT_ID, nextId)
            .apply()
    }
}
