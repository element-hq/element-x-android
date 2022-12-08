import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.matrix.rustcomponents.sdk.StoppableSpawn

internal fun <T> mxCallbackFlow(block: suspend ProducerScope<T>.() -> StoppableSpawn) =
    callbackFlow {
        val token: StoppableSpawn = block(this)
        awaitClose {
            token.cancel()
        }
    }
