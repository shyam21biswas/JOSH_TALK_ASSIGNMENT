package com.example.josh

import kotlinx.coroutines.flow.Flow


class TaskRepository(
    private val taskDao: TaskDao,
    private val apiService: ApiService
) {
    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getTaskCount(): Flow<Int> = taskDao.getTaskCount()

    fun getTotalDuration(): Flow<Int?> = taskDao.getTotalDuration()

    suspend fun getRandomProduct(): Product? {
        return try {
            val response = apiService.getProducts()
            response.products.randomOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
