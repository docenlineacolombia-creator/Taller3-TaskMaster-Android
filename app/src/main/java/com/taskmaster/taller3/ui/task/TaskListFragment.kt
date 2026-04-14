package com.taskmaster.taller3.ui.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.taskmaster.taller3.R
import com.taskmaster.taller3.data.task.Task
import com.taskmaster.taller3.databinding.FragmentTaskListBinding

/**
 * TaskListFragment – Pantalla principal de la app.
 *
 * Muestra la lista de tareas filtrada según el tab activo.
 * Observa [TaskViewModel.taskListLiveData] (LiveData) y actualiza
 * el RecyclerView automáticamente ante cualquier cambio.
 *
 * Navegación:
 * - FAB → TaskDetailFragment (nueva tarea) usando Safe Args.
 * - Toque en tarea → TaskDetailFragment (edición) con taskId.
 * - Botón engranaje → SettingsFragment.
 */
class TaskListFragment : Fragment() {

    // ViewBinding generado automáticamente desde fragment_task_list.xml
    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    // ViewModel compartido entre Fragments (activityViewModels)
    private val viewModel: TaskViewModel by activityViewModels()

    // Adaptador del RecyclerView
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabFilters()
        setupFab()
        setupSettingsButton()
        observeViewModel()
        updateGreeting()
    }

    // ======================
    // Setup de componentes
    // ======================

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task -> navigateToDetail(task.id) },
            onCheckChange = { task -> viewModel.toggleTaskCompleted(task.id) },
            onDeleteClick = { task -> confirmDelete(task) }
        )

        binding.recyclerTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun setupTabFilters() {
        // TabLayout para filtrar: Todas | Pendientes | Completadas
        binding.tabLayout.addOnTabSelectedListener(object :
            com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                val filter = when (tab?.position) {
                    1 -> TaskViewModel.FILTER_PENDING
                    2 -> TaskViewModel.FILTER_COMPLETED
                    else -> TaskViewModel.FILTER_ALL
                }
                viewModel.setFilter(filter)
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun setupFab() {
        // FAB: navegar a TaskDetailFragment para crear nueva tarea
        // taskId = -1 indica que es una tarea nueva (sin Safe Args de ID existente)
        binding.fabAddTask.setOnClickListener {
            val action = TaskListFragmentDirections
                .actionTaskListFragmentToTaskDetailFragment(taskId = -1)
            findNavController().navigate(action)
        }
    }

    private fun setupSettingsButton() {
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_taskListFragment_to_settingsFragment)
        }
    }

    // ======================
    // Observación de LiveData
    // ======================

    private fun observeViewModel() {
        // Observar la lista filtrada de tareas
        viewModel.taskListLiveData.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
            updateEmptyState(tasks)
            updateTaskCount(tasks.size)
        }
    }

    // ======================
    // Helpers de UI
    // ======================

    private fun updateGreeting() {
        val name = viewModel.preferences.getUserName()
        binding.tvGreeting.text = "¡Hola, $name!"
    }

    private fun updateEmptyState(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            binding.recyclerTasks.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.recyclerTasks.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
        }
    }

    private fun updateTaskCount(count: Int) {
        binding.tvTaskCount.text = "$count ${if (count == 1) "tarea" else "tareas"}"
    }

    private fun confirmDelete(task: Task) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar tarea")
            .setMessage("\u00bfEstás seguro de que deseas eliminar \"${task.title}\"?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteTask(task.id)
                Snackbar.make(binding.root, "Tarea eliminada", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    // Navegar al detalle pasando el taskId con Safe Args
    private fun navigateToDetail(taskId: Int) {
        val action = TaskListFragmentDirections
            .actionTaskListFragmentToTaskDetailFragment(taskId = taskId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Importante: evitar memory leaks al destruir el Fragment
        _binding = null
    }
}
