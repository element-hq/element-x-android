/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import coil3.ImageLoader
import coil3.test.FakeImageLoaderEngine
import coil3.test.intercept
import org.robolectric.RuntimeEnvironment

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
