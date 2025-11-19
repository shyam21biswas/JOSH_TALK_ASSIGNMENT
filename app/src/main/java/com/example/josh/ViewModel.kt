package com.example.josh



import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _currentProduct = MutableStateFlow<Product?>(null)
    val currentProduct: StateFlow<Product?> = _currentProduct.asStateFlow()

    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks.asStateFlow()

    private val _taskCount = MutableStateFlow(0)
    val taskCount: StateFlow<Int> = _taskCount.asStateFlow()

    private val _totalDuration = MutableStateFlow(0)
    val totalDuration: StateFlow<Int> = _totalDuration.asStateFlow()

    init {
        loadTasks()
        loadStats()
    }

    fun fetchProduct() {
        viewModelScope.launch {
            _currentProduct.value = repository.getRandomProduct()
        }
    }

    fun saveTask(text: String, audioPath: String, durationSec: Int) {
        viewModelScope.launch {
            val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(Date())

            val task = TaskEntity(
                taskType = "text_reading",
                text = text,
                audioPath = audioPath,
                durationSec = durationSec,
                timestamp = timestamp
            )

            repository.insertTask(task)
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            repository.getAllTasks().collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository.getTaskCount().collect { count ->
                _taskCount.value = count
            }
        }

        viewModelScope.launch {
            repository.getTotalDuration().collect { duration ->
                _totalDuration.value = duration ?: 0
            }
        }
    }
}


//viewfactory



class TaskViewModelFactory(
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}