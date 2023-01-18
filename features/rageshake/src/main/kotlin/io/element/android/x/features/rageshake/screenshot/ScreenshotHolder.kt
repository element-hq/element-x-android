/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x.features.rageshake.screenshot

import android.content.Context
import android.graphics.Bitmap
import io.element.android.x.core.bitmap.writeBitmap
import io.element.android.x.di.AppScope
import io.element.android.x.di.ApplicationContext
import io.element.android.x.di.SingleIn
import java.io.File
import javax.inject.Inject

@SingleIn(AppScope::class)
class ScreenshotHolder @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val file = File(context.filesDir, "screenshot.png")

    fun writeBitmap(data: Bitmap) {
        file.writeBitmap(data, Bitmap.CompressFormat.PNG, 85)
    }

    fun getFile() = file.takeIf { it.exists() && it.length() > 0 }

    fun reset() {
        file.delete()
    }
}
