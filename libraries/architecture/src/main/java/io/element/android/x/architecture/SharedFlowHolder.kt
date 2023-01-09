package io.element.android.x.architecture

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SharedFlowHolder<Data>(capacity: Int = 64) {
    private val mutableFlow: MutableSharedFlow<Data> = MutableSharedFlow(extraBufferCapacity = capacity)

    fun asSharedFlow() = mutableFlow.asSharedFlow()

    fun emit(data: Data) = mutableFlow.tryEmit(data)
}
