/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import android.content.Context
import android.os.Build
import coil3.ImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Provider
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import okhttp3.OkHttpClient

interface ImageLoaderFactory {
    fun newImageLoader(): ImageLoader
    fun newImageLoader(matrixMediaLoader: MatrixMediaLoader): ImageLoader
}

@ContributesBinding(AppScope::class)
class DefaultImageLoaderFactory(
    @ApplicationContext private val context: Context,
    private val okHttpClient: Provider<OkHttpClient>,
) : ImageLoaderFactory {
    private val okHttpNetworkFetcherFactory = OkHttpNetworkFetcherFactory(
        callFactory = {
            // Use newBuilder, see https://coil-kt.github.io/coil/network/#using-a-custom-okhttpclient
            okHttpClient().newBuilder().build()
        }
    )

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(okHttpNetworkFetcherFactory)
            }
            .build()
    }

    override fun newImageLoader(matrixMediaLoader: MatrixMediaLoader): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(okHttpNetworkFetcherFactory)
                // Add gif support
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(AvatarDataKeyer())
                add(MediaRequestDataKeyer())
                add(AvatarDataFetcherFactory(matrixMediaLoader))
                add(MediaRequestDataFetcherFactory(matrixMediaLoader))
            }
            .build()
    }
}
