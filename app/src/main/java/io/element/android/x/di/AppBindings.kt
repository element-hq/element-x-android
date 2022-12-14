package io.element.android.x.di

import com.squareup.anvil.annotations.ContributesTo
import io.element.android.x.di.AppScope
import io.element.android.x.matrix.Matrix
import kotlinx.coroutines.CoroutineScope

@ContributesTo(AppScope::class)
interface AppBindings {
    fun coroutineScope(): CoroutineScope
    fun matrix(): Matrix
}