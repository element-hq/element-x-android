package io.element.android.x

import androidx.lifecycle.ViewModel
import io.element.android.x.matrix.MatrixInstance

class MainViewModel : ViewModel() {
    private val matrix = MatrixInstance.getInstance()

    suspend fun hasSession(): Boolean {
        return matrix.restoreSession() != null
    }
}