package io.element.android.x

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.element.android.x.matrix.MatrixInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val matrix = MatrixInstance.getInstance()

    suspend fun isLoggedIn(): Boolean {
        return matrix.isLoggedIn().first()
    }

    fun startSyncIfLogged(){
        viewModelScope.launch {
            if(!isLoggedIn()) return@launch
            matrix.activeClient().startSync()
        }
    }

    fun stopSyncIfLogged(){
        viewModelScope.launch {
            if (!isLoggedIn()) return@launch
            matrix.activeClient().stopSync()
        }
    }

    suspend fun restoreSession() {
        matrix.restoreSession()
        matrix.activeClient().startSync()
    }
}