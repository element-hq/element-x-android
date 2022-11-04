package io.element.android.x

import androidx.lifecycle.ViewModel
import io.element.android.x.matrix.MatrixInstance
import kotlinx.coroutines.flow.first

class MainViewModel : ViewModel() {
    private val matrix = MatrixInstance.getInstance()

    suspend fun isLoggedIn(): Boolean {
        return matrix.isLoggedIn().first()
    }

    suspend fun restoreSession() {
        matrix.restoreSession()
    }
}