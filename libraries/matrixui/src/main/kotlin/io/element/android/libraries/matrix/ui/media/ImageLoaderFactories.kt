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
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClient
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Provider

interface LoggedInImageLoaderFactory {
    fun newImageLoader(matrixClient: MatrixClient): ImageLoader
}

@ContributesBinding(AppScope::class)
class DefaultLoggedInImageLoaderFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: Provider<OkHttpClient>,
) : LoggedInImageLoaderFactory {
    override fun newImageLoader(matrixClient: MatrixClient): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            // Use newBuilder, see https://coil-kt.github.io/coil/network/#using-a-custom-okhttpclient
                            okHttpClient.get().newBuilder().build()
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
                add(AvatarDataFetcherFactory(matrixClient))
                add(MediaRequestDataFetcherFactory(matrixClient))
            }
            .build()
    }
}

class NotLoggedInImageLoaderFactory @Inject constructor(
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
                            okHttpClient.get().newBuilder().build()
                        }
                    )
                )
            }
            .build()
    }
}
