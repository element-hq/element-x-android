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

package io.element.android.libraries.matrix.impl.media

import io.element.android.libraries.matrix.api.media.AudioInfo
import org.matrix.rustcomponents.sdk.AudioInfo as RustAudioInfo

fun RustAudioInfo.map(): AudioInfo = AudioInfo(
//    duration = duration?.toLong(),
    duration = 0,
    size = size?.toLong(),
    mimeType = mimetype
)

fun AudioInfo.map(): RustAudioInfo = RustAudioInfo(
//    duration = duration?.toULong(),
    duration = null,
    size = size?.toULong(),
    mimetype = mimeType,
)
