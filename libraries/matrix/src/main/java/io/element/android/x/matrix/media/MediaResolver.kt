package io.element.android.x.matrix.media

import io.element.android.x.matrix.MatrixClient
import org.matrix.rustcomponents.sdk.MediaSource
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl

interface MediaResolver {

    sealed interface Kind {
        data class Thumbnail(val width: Int, val height: Int) : Kind {
            constructor(size: Int) : this(size, size)
        }

        object Content : Kind
    }

    data class Meta(
        val source: MediaSource,
        val kind: Kind
    )

    suspend fun resolve(url: String?, kind: Kind): ByteArray?

    suspend fun resolve(meta: Meta): ByteArray?
}


internal class RustMediaResolver(private val client: MatrixClient) : MediaResolver {

    override suspend fun resolve(url: String?, kind: MediaResolver.Kind): ByteArray? {
        if (url.isNullOrEmpty()) return null
        val mediaSource = mediaSourceFromUrl(url)
        return resolve(MediaResolver.Meta(mediaSource, kind))
    }

    override suspend fun resolve(meta: MediaResolver.Meta): ByteArray? {
        return when (meta.kind) {
            is MediaResolver.Kind.Content -> client.loadMediaContentForSource(meta.source)
            is MediaResolver.Kind.Thumbnail -> client.loadMediaThumbnailForSource(
                meta.source,
                meta.kind.width.toLong(),
                meta.kind.height.toLong()
            )
        }.getOrNull()
    }


}