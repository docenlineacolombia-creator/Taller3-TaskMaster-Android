package com.taskmaster.taller3.data.task

/**
 * Modelo de datos de una tarea.
 *
 * @param id          Identificador único autogenerado.
 * @param title       Título de la tarea (obligatorio).
 * @param description Descripción opcional.
 * @param reminderTime Hora del recordatorio en formato "HH:mm" (vacío si no hay recordatorio).
 * @param hasReminder true si el recordatorio está activado.
 * @param isCompleted true si la tarea ha sido marcada como completada.
 * @param createdAt   Timestamp de creación en milisegundos.
 * @param priority    Nivel de prioridad: 0=Baja, 1=Media, 2=Alta.
 */
data class Task(
    val id: Int,
    val title: String,
    val description: String = "",
    val reminderTime: String = "",
    val hasReminder: Boolean = false,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val priority: Int = 0  // 0=Baja, 1=Media, 2=Alta
)
