package io.element.android.x.initializer

import android.content.Context
import androidx.startup.Initializer
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import io.element.android.x.core.di.bindings
import io.element.android.x.di.AppBindings

class CoilInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        Coil.setImageLoader(ElementImageLoaderFactory(context))
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

private class ElementImageLoaderFactory(
    private val context: Context
) :
    ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader
            .Builder(context)
            .components {
                context.bindings<AppBindings>().matrix().registerCoilComponents(this)
            }
            .build()
    }


}