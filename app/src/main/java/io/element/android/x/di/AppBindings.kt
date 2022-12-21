package io.element.android.x.di

import com.squareup.anvil.annotations.ContributesTo
import io.element.android.x.matrix.Matrix
import io.element.android.x.node.LoggedInFlowNode
import io.element.android.x.node.RootFlowNode
import kotlinx.coroutines.CoroutineScope

@ContributesTo(AppScope::class)
interface AppBindings {
    fun coroutineScope(): CoroutineScope
    fun matrix(): Matrix
    fun sessionComponentsOwner(): SessionComponentsOwner
}
