package org.example.project.presentation.viewmodels

import kotlinx.coroutines.cancel
import kotlinx.coroutines.CoroutineScope

/**
 * Simple ViewModel replacement for Desktop applications
 * Provides basic lifecycle management and cleanup
 */
open class ViewModel {

    /**
     * Called when this ViewModel is no longer used and will be destroyed.
     * Override this method to perform any cleanup operations.
     */
    protected open fun onCleared() {
        // Override in subclasses for cleanup
    }

    /**
     * Clears this ViewModel's state and calls onCleared()
     * Call this method when you no longer need the ViewModel
     */
    fun clear() {
        onCleared()
    }
}