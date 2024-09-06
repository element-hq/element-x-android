/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.test.FakeImageLoaderEngine
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoilApi::class)
class FakeImageLoader {
    private val coilRequests = mutableListOf<Any>()

    private var cache: ImageLoader? = null

    fun getImageLoader(): ImageLoader {
        return cache ?: ImageLoader.Builder(RuntimeEnvironment.getApplication())
            .components {
                val engine = FakeImageLoaderEngine.Builder()
                    .intercept(
                        predicate = {
                            coilRequests.add(it)
                            true
                        },
                        drawable = ColorDrawable(Color.BLUE)
                    )
                    .build()
                add(engine)
            }
            .build()
            .also {
                cache = it
            }
    }

    fun getCoilRequests(): List<Any> {
        return coilRequests.toList()
    }
}
