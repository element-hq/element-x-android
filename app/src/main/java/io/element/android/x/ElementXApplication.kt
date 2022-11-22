package io.element.android.x

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.airbnb.mvrx.Mavericks
import io.element.android.x.matrix.MatrixInstance
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus
import timber.log.Timber

class ElementXApplication : Application(), ImageLoaderFactory {

    private val applicationScope = MainScope() + CoroutineName("ElementX Scope")

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        MatrixInstance.init(this, applicationScope)
        Mavericks.initialize(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader
            .Builder(this)
            .components {
                MatrixInstance.getInstance().registerComponents(this)
            }
            .build()
    }
}