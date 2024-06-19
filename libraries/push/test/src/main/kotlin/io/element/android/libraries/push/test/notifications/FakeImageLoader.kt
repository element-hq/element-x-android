/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
