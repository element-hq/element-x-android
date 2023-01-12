package io.element.android.x.matrix.ui.media

import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import io.element.android.x.di.ApplicationContext
import io.element.android.x.matrix.MatrixClient
import javax.inject.Inject

class LoggedInImageLoaderFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val matrixClient: MatrixClient,
) : ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader
            .Builder(context)
            .components {
                add(MediaKeyer())
                add(MediaFetcher.Factory(matrixClient))
            }
            .build()
    }
}

class NotLoggedInImageLoaderFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) : ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader
            .Builder(context)
            .build()
    }
}

