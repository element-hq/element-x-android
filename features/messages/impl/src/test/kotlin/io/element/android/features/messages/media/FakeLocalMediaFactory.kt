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

package io.element.android.features.messages.media

import android.net.Uri
import io.element.android.features.messages.fixtures.aLocalMedia
import io.element.android.features.messages.impl.media.local.LocalMedia
import io.element.android.features.messages.impl.media.local.LocalMediaFactory
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.MediaFile

class FakeLocalMediaFactory(private val localMediaUri: Uri) : LocalMediaFactory {

    var fallbackMimeType: String = MimeTypes.OctetStream

    override fun createFromUri(uri: Uri, mimeType: String?, name: String?): LocalMedia {
        return aLocalMedia(uri, mimeType ?: fallbackMimeType)
    }
}
