package io.element.android.x.matrix.media

import io.element.android.x.matrix.MatrixClient
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl

interface MediaResolver {

    sealed interface Kind {
        data class Thumbnail(val width: Int, val height: Int) : Kind {
            constructor(size: Int) : this(size, size)
        }

        object Content : Kind
    }

    suspend fun resolve(url: String?, kind: Kind): ByteArray?
}


internal class RustMediaResolver(private val client: MatrixClient) : MediaResolver {

    override suspend fun resolve(url: String?, kind: MediaResolver.Kind): ByteArray? {
        if (url.isNullOrEmpty()) return null
        val mediaSource = mediaSourceFromUrl(url)
        return when (kind) {
            is MediaResolver.Kind.Content -> client.loadMediaContentForSource(mediaSource)
            is MediaResolver.Kind.Thumbnail -> client.loadMediaThumbnailForSource(
                mediaSource,
                kind.width.toLong(),
                kind.height.toLong()
            )
        }.getOrNull()
    }


}