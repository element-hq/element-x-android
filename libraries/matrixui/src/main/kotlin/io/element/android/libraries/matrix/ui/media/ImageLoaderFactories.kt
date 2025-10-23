/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
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
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import okhttp3.OkHttpClient

interface LoggedInImageLoaderFactory {
    fun newImageLoader(matrixMediaLoader: MatrixMediaLoader): ImageLoader
}

@ContributesBinding(AppScope::class)
class DefaultLoggedInImageLoaderFactory(
    @ApplicationContext private val context: Context,
    private val okHttpClient: Provider<OkHttpClient>,
) : LoggedInImageLoaderFactory {
    override fun newImageLoader(matrixMediaLoader: MatrixMediaLoader): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            // Use newBuilder, see https://coil-kt.github.io/coil/network/#using-a-custom-okhttpclient
                            okHttpClient().newBuilder().build()
                        }
                    )
                )
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

@Inject
class NotLoggedInImageLoaderFactory(
    @ApplicationContext private val context: Context,
    private val okHttpClient: Provider<OkHttpClient>,
) {
    fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            // Use newBuilder, see https://coil-kt.github.io/coil/network/#using-a-custom-okhttpclient
                            okHttpClient().newBuilder().build()
                        }
                    )
                )
            }
            .build()
    }
}
