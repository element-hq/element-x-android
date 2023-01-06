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

package io.element.android.x.matrix.ui

import coil.ComponentRegistry
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.ui.media.MediaFetcher
import io.element.android.x.matrix.ui.media.MediaKeyer
import javax.inject.Inject

class MatrixUi @Inject constructor() {

    fun registerCoilComponents(
        builder: ComponentRegistry.Builder,
        activeClientProvider: () -> MatrixClient?
    ) {
        builder.add(MediaKeyer())
        builder.add(MediaFetcher.Factory(activeClientProvider))
    }
}
