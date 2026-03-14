package com.elfennani.kiroku.domain.model

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}

suspend fun <T> resourceOf(block: suspend () -> T): Resource<T> {
    return try {
        print("[RESOURCE_OF] Fetching Data...")
        val data =block()
        print("[RESOURCE_OF] Got Data $data!")
        Resource.Success(data)
    } catch (e: Exception) {
        // TODO: Handle exceptions
        print("[RESOURCE_OF] Error: ${e.message}")
        Resource.Error(e.message ?: "Unknown error")
    }
}