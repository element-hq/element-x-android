/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.sync

import io.element.android.libraries.matrix.api.sync.SlidingSyncVersion
import org.matrix.rustcomponents.sdk.SlidingSyncVersion as RustSlidingSyncVersion

internal fun RustSlidingSyncVersion.map(): SlidingSyncVersion {
    return when (this) {
        RustSlidingSyncVersion.None -> SlidingSyncVersion.None
        is RustSlidingSyncVersion.Proxy -> SlidingSyncVersion.Proxy
        RustSlidingSyncVersion.Native -> SlidingSyncVersion.Native
    }
}
