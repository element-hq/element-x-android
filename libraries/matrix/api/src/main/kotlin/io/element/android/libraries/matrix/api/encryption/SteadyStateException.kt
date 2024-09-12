/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
