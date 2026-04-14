package com.taskmaster.taller3.ui.task

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.taskmaster.taller3.R
import com.taskmaster.taller3.data.task.Task
import com.taskmaster.taller3.databinding.FragmentTaskDetailBinding
import com.taskmaster.taller3.receiver.TaskReminderReceiver
import java.util.Calendar

/**
 * TaskDetailFragment – Crear o editar una tarea.
 *
 * Recibe el argumento [taskId] vía Safe Args:
 * - taskId == -1 → modo creación
 * - taskId > 0   → modo edición (carga los datos de la tarea existente)
 *
 * Al guardar:
 * 1. Crea o actualiza la tarea en el ViewModel.
 * 2. Si el recordatorio está activado, programa un PendingIntent con AlarmManager
 *    que disparará al TaskReminderReceiver en 30 segundos (o en la hora elegida).
 * 3. Navega de vuelta a TaskListFragment.
 */
class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels()

    // Safe Args: recibe el argumento 'taskId' desde el nav_graph
    private val args: TaskDetailFragmentArgs by navArgs()

    // Hora del recordatorio seleccionada por el usuario
    private var selectedHour: Int = -1
    private var selectedMinute: Int = -1

    // Modo edición: tarea original (null si es nueva)
    private var existingTask: Task? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPrioritySpinner()
        loadTaskIfEditing()
        setupTimePicker()
        setupReminderSwitch()
        setupSaveButton()
    }

    // ======================
    // Setup de componentes
    // ======================

    private fun setupPrioritySpinner() {
        val priorities = listOf("○ Baja", "● Media", "★ Alta")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter
    }

    /**
     * Si args.taskId > -1, estamos en modo edición: pre-llenamos el formulario.
     */
    private fun loadTaskIfEditing() {
        val taskId = args.taskId
        if (taskId != -1) {
            existingTask = viewModel.getTaskById(taskId)
            existingTask?.let { task ->
                binding.etTitle.setText(task.title)
                binding.etDescription.setText(task.description)
                binding.spinnerPriority.setSelection(task.priority)
                binding.switchReminder.isChecked = task.hasReminder

                if (task.hasReminder && task.reminderTime.isNotEmpty()) {
                    binding.tvSelectedTime.text = "⏰ Recordatorio a las ${task.reminderTime}"
                    binding.tvSelectedTime.visibility = View.VISIBLE
                    // Parsear la hora guardada
                    val parts = task.reminderTime.split(":")
                    if (parts.size == 2) {
                        selectedHour = parts[0].toIntOrNull() ?: -1
                        selectedMinute = parts[1].toIntOrNull() ?: -1
                    }
                }

                binding.layoutReminderTime.visibility =
                    if (task.hasReminder) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupTimePicker() {
        binding.btnPickTime.setOnClickListener {
            // MaterialTimePicker: selector de hora moderno de Material Design 3
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(if (selectedHour >= 0) selectedHour else 9)
                .setMinute(if (selectedMinute >= 0) selectedMinute else 0)
                .setTitleText("Hora del recordatorio")
                .build()

            picker.addOnPositiveButtonClickListener {
                selectedHour = picker.hour
                selectedMinute = picker.minute
                val timeStr = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.tvSelectedTime.text = "⏰ Recordatorio a las $timeStr"
                binding.tvSelectedTime.visibility = View.VISIBLE
            }

            picker.show(parentFragmentManager, "time_picker")
        }
    }

    private fun setupReminderSwitch() {
        // Mostrar/ocultar sección de hora según el switch
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutReminderTime.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        // Estado inicial
        binding.layoutReminderTime.visibility =
            if (binding.switchReminder.isChecked) View.VISIBLE else View.GONE
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener { saveTask() }
    }

    // ======================
    // Guardar tarea
    // ======================

    private fun saveTask() {
        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.tilTitle.error = "El título no puede estar vacío"
            return
        }
        binding.tilTitle.error = null

        val description = binding.etDescription.text.toString().trim()
        val hasReminder = binding.switchReminder.isChecked
        val priority = binding.spinnerPriority.selectedItemPosition
        val reminderTime = if (hasReminder && selectedHour >= 0) {
            String.format("%02d:%02d", selectedHour, selectedMinute)
        } else ""

        val task = if (existingTask != null) {
            // Modo edición: conservar id y createdAt originales
            existingTask!!.copy(
                title = title,
                description = description,
                hasReminder = hasReminder,
                reminderTime = reminderTime,
                priority = priority
            )
        } else {
            // Modo creación: id = 0 (el repositorio lo asignará automáticamente)
            Task(
                id = 0,
                title = title,
                description = description,
                hasReminder = hasReminder,
                reminderTime = reminderTime,
                priority = priority
            )
        }

        val savedTask = if (existingTask != null) {
            viewModel.updateTask(task)
            task
        } else {
            viewModel.addTask(task)
        }

        // Programar recordatorio si el usuario lo activó
        if (hasReminder) {
            scheduleReminder(savedTask)
            viewModel.preferences.incrementRemindersSent()
        }

        Toast.makeText(
            requireContext(),
            if (existingTask != null) "✅ Tarea actualizada" else "✅ Tarea guardada",
            Toast.LENGTH_SHORT
        ).show()

        // Volver al listado usando navigateUp() (pop del back stack)
        findNavController().navigateUp()
    }

    // ======================
    // BroadcastReceiver + AlarmManager
    // ======================

    /**
     * Programa un [PendingIntent] que disparará al [TaskReminderReceiver].
     *
     * Si la hora fue seleccionada por el usuario, se programa a esa hora del día.
     * Si no se seleccionó hora, se dispara en 30 segundos (modo de prueba).
     *
     * El Intent lleva los datos de la tarea como extras para que el Receiver
     * pueda mostrar el título y descripción en la notificación.
     */
    private fun scheduleReminder(task: Task) {
        val intent = Intent(requireContext(), TaskReminderReceiver::class.java).apply {
            action = "com.taskmaster.taller3.ACTION_TASK_REMINDER"
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(TaskReminderReceiver.EXTRA_TASK_DESC, task.description)
        }

        // requestCode único por tarea para que cada alarma sea independiente
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            task.id, // requestCode único
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Calcular el tiempo de disparo
        val triggerAtMillis = if (selectedHour >= 0) {
            // Usar la hora exacta seleccionada por el usuario
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // Si la hora ya pasó hoy, programar para mañana
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            calendar.timeInMillis
        } else {
            // Modo de prueba: 30 segundos desde ahora
            System.currentTimeMillis() + 30_000
        }

        // Usar setExactAndAllowWhileIdle para que funcione en modo Doze
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
