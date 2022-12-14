package io.element.android.x

import android.app.Application
import androidx.startup.AppInitializer
import io.element.android.x.core.di.DaggerComponentOwner
import io.element.android.x.di.DaggerAppComponent
import io.element.android.x.initializer.CoilInitializer
import io.element.android.x.initializer.MavericksInitializer
import io.element.android.x.initializer.TimberInitializer
import io.element.android.x.matrix.MatrixInstance
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus

class ElementXApplication : Application(), DaggerComponentOwner {

    override lateinit var daggerComponent: Any

    private val applicationScope = MainScope() + CoroutineName("ElementX Scope")

    override fun onCreate() {
        super.onCreate()
        daggerComponent = DaggerAppComponent.factory().create(this)
        MatrixInstance.init(this, applicationScope)
        AppInitializer.getInstance(this).apply {
            initializeComponent(TimberInitializer::class.java)
            initializeComponent(CoilInitializer::class.java)
            initializeComponent(MavericksInitializer::class.java)
        }
    }
}
