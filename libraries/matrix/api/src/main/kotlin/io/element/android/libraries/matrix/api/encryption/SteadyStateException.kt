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

package io.element.android.libraries.matrix.api.encryption

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SteadyStateException {
    /**
     * The backup can be deleted.
     */
    data class BackupDisabled(val message: String) : SteadyStateException

    /**
     * The task waiting for notifications coming from the upload task can fall behind so much that it lost some notifications.
     */
    data class Lagged(val message: String) : SteadyStateException

    /**
     * The request(s) to upload the room keys failed.
     */
    data class Connection(val message: String) : SteadyStateException
}
