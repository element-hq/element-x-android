package io.element.android.x.di

import android.content.Context
import io.element.android.x.core.di.bindings
import io.element.android.x.matrix.MatrixClient
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@SingleIn(AppScope::class)
class SessionComponentsOwner @Inject constructor(@ApplicationContext private val context: Context) {

    private val sessionComponents = ConcurrentHashMap<String, SessionComponent>()
    var activeSessionComponent: SessionComponent? = null
        private set

    fun setActive(sessionId: String) {
        val sessionComponent = sessionComponents[sessionId]
        if (activeSessionComponent != sessionComponent) {
            activeSessionComponent = sessionComponent
        }
    }

    fun create(matrixClient: MatrixClient) {
        val sessionId = matrixClient.sessionId
        val sessionComponent =
            context.bindings<SessionComponent.ParentBindings>().sessionComponentBuilder()
                .client(matrixClient).build()
        sessionComponents[sessionId] = sessionComponent
        setActive(sessionId)
    }

    fun releaseActiveSession() {
        activeSessionComponent?.also {
            release(it.matrixClient().sessionId)
        }
    }

    fun release(sessionId: String) {
        val sessionComponent = sessionComponents.remove(sessionId)
        if (activeSessionComponent == sessionComponent) {
            activeSessionComponent = null
        }
    }
}
