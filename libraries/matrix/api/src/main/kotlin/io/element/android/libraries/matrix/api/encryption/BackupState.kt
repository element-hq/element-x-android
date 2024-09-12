/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.encryption

enum class BackupState {
    /**
     * Special value, when the SDK is waiting for the first sync to be done.
     */
    WAITING_FOR_SYNC,

    /**
     * Values mapped from the SDK.
     */
    UNKNOWN,
    CREATING,
    ENABLING,
    RESUMING,
    ENABLED,
    DOWNLOADING,
    DISABLING
}
