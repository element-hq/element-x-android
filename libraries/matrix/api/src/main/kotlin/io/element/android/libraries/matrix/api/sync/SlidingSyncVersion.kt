/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.sync

sealed interface SlidingSyncVersion {
    data object None : SlidingSyncVersion
    data object Proxy : SlidingSyncVersion
    data object Native : SlidingSyncVersion
}
