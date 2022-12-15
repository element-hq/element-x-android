package io.element.android.x.di

import android.content.Context
import com.squareup.anvil.annotations.MergeComponent
import dagger.BindsInstance
import dagger.Component

@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@ApplicationContext @BindsInstance context: Context): AppComponent
    }
}