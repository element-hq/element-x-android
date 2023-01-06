package io.element.android.x.di

import android.content.Context
import com.squareup.anvil.annotations.MergeComponent
import dagger.BindsInstance
import dagger.Component
import io.element.android.x.architecture.NodeFactoriesBindings
import io.element.android.x.architecture.viewmodel.DaggerMavericksBindings

@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
interface AppComponent : DaggerMavericksBindings, NodeFactoriesBindings {

    @Component.Factory
    interface Factory {
        fun create(@ApplicationContext @BindsInstance context: Context): AppComponent
    }
}
