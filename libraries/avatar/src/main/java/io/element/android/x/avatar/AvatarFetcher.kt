package io.element.android.x.avatar

import coil.ImageLoader
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import io.element.android.x.matrix.MatrixClient
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl

class AvatarFetcher(
    private val matrixClient: MatrixClient,
    private val avatarData: AvatarData,
    private val options: Options,
    private val imageLoader: ImageLoader
) :
    Fetcher {

    override suspend fun fetch(): FetchResult? {
        val mediaSource = mediaSourceFromUrl(avatarData.url)
        val mediaContent = matrixClient.loadMediaContentForSource(mediaSource)
        return mediaContent.fold(
            { mediaContent ->
                val byteArray = mediaContent.toUByteArray().toByteArray()
                val fetcher = imageLoader.components.newFetcher(byteArray, options, imageLoader)
                fetcher?.first?.fetch()
            },
            {null}
        )
    }

    class Factory(private val matrixClient: MatrixClient) : Fetcher.Factory<AvatarData> {

        override fun create(
            data: AvatarData,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher? {
            return AvatarFetcher(matrixClient, data, options, imageLoader)
        }
    }
}
