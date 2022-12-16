package io.element.android.x

import android.app.Application
import androidx.startup.AppInitializer
import io.element.android.x.core.di.DaggerComponentOwner
import io.element.android.x.core.di.bindings
import io.element.android.x.di.AppBindings
import io.element.android.x.di.AppComponent
import io.element.android.x.di.DaggerAppComponent
import io.element.android.x.di.SessionComponentsOwner
import io.element.android.x.initializer.CoilInitializer
import io.element.android.x.initializer.MatrixInitializer
import io.element.android.x.initializer.MavericksInitializer

class ElementXApplication : Application(), DaggerComponentOwner {

    private lateinit var appComponent: AppComponent
    private var sessionComponentsOwner: SessionComponentsOwner? = null

    override val daggerComponent: Any
        get() = listOfNotNull(sessionComponentsOwner?.activeSessionComponent, appComponent)

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(applicationContext)
        sessionComponentsOwner = bindings<AppBindings>().sessionComponentsOwner()
        AppInitializer.getInstance(this).apply {
            initializeComponent(MatrixInitializer::class.java)
            initializeComponent(CoilInitializer::class.java)
            initializeComponent(MavericksInitializer::class.java)
        }
    }


}
