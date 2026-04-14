package com.taskmaster.taller3.ui.task

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskmaster.taller3.R
import com.taskmaster.taller3.data.task.Task
import com.taskmaster.taller3.databinding.ItemTaskBinding

/**
 * Adaptador para el RecyclerView de tareas.
 *
 * Usa [ListAdapter] con [DiffUtil] para actualizaciones eficientes de la lista:
 * solo se animan los ítems que realmente cambiaron, no toda la lista.
 *
 * @param onTaskClick    Se llama cuando el usuario toca una tarea (para editar).
 * @param onCheckChange  Se llama cuando el usuario alterna el estado completado.
 * @param onDeleteClick  Se llama cuando el usuario presiona el botón eliminar.
 */
class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onCheckChange: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskDescription.text = task.description.ifEmpty { "Sin descripción" }
            binding.checkboxCompleted.isChecked = task.isCompleted

            // Indicador de recordatorio
            binding.ivReminderIcon.visibility =
                if (task.hasReminder) android.view.View.VISIBLE else android.view.View.GONE

            // Hora del recordatorio
            if (task.hasReminder && task.reminderTime.isNotEmpty()) {
                binding.tvReminderTime.text = "⏰ ${task.reminderTime}"
                binding.tvReminderTime.visibility = android.view.View.VISIBLE
            } else {
                binding.tvReminderTime.visibility = android.view.View.GONE
            }

            // Chip de prioridad
            val (priorityText, priorityColor) = when (task.priority) {
                2 -> Pair("★ Alta", R.color.priority_high)
                1 -> Pair("● Media", R.color.priority_medium)
                else -> Pair("○ Baja", R.color.priority_low)
            }
            binding.chipPriority.text = priorityText
            binding.chipPriority.setTextColor(
                ContextCompat.getColor(binding.root.context, priorityColor)
            )

            // Efecto tachado para tareas completadas
            if (task.isCompleted) {
                binding.tvTaskTitle.paintFlags =
                    binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTaskTitle.alpha = 0.5f
            } else {
                binding.tvTaskTitle.paintFlags =
                    binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTaskTitle.alpha = 1.0f
            }

            // Listeners
            binding.root.setOnClickListener { onTaskClick(task) }
            binding.checkboxCompleted.setOnClickListener { onCheckChange(task) }
            binding.btnDelete.setOnClickListener { onDeleteClick(task) }
        }
    }

    /**
     * DiffCallback para ListAdapter: determina si dos items son el mismo
     * y si su contenido cambió. Permite animaciones precisas.
     */
    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem == newItem
    }
}
