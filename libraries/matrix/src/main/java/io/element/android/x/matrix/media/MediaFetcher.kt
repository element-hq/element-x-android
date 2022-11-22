package io.element.android.x.matrix.media

import coil.ImageLoader
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import io.element.android.x.matrix.Matrix
import java.nio.ByteBuffer

internal class MediaFetcher(
    private val mediaResolver: MediaResolver,
    private val meta: MediaResolver.Meta,
    private val options: Options,
    private val imageLoader: ImageLoader
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val byteArray = mediaResolver.resolve(meta) ?: return null
        val byteBuffer = ByteBuffer.wrap(byteArray)
        return imageLoader.components.newFetcher(byteBuffer, options, imageLoader)?.first?.fetch()
    }

    class Factory(private val matrix: Matrix) : Fetcher.Factory<MediaResolver.Meta> {
        override fun create(
            data: MediaResolver.Meta,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            val activeClient = matrix.activeClient()
            return MediaFetcher(
                mediaResolver = activeClient.mediaResolver(),
                meta = data,
                options = options,
                imageLoader = imageLoader
            )
        }
    }
}