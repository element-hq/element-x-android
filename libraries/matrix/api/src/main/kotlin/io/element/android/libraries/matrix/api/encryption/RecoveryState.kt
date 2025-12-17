/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.encryption

enum class RecoveryState {
    /**
     * Special value, when the SDK is waiting for the first sync to be done.
     */
    WAITING_FOR_SYNC,

    /**
     * Values mapped from the SDK.
     */
    UNKNOWN,
    ENABLED,
    DISABLED,
    INCOMPLETE,
}
