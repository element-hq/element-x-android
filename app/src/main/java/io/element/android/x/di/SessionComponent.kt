package io.element.android.x.di

import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeSubcomponent
import dagger.BindsInstance
import dagger.Subcomponent
import io.element.android.x.core.di.DaggerMavericksBindings
import io.element.android.x.core.di.NodeFactoriesBindings
import io.element.android.x.matrix.MatrixClient

@SingleIn(SessionScope::class)
@MergeSubcomponent(SessionScope::class)
interface SessionComponent: DaggerMavericksBindings, NodeFactoriesBindings {

    fun matrixClient(): MatrixClient

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun client(matrixClient: MatrixClient): Builder
        fun build(): SessionComponent
    }

    @ContributesTo(AppScope::class)
    interface ParentBindings {
        fun sessionComponentBuilder(): Builder
    }
}
