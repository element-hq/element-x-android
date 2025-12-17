/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.core

import io.element.android.libraries.matrix.api.core.ProgressCallback
import org.matrix.rustcomponents.sdk.ProgressWatcher
import org.matrix.rustcomponents.sdk.TransmissionProgress

internal class ProgressWatcherWrapper(private val progressCallback: ProgressCallback) : ProgressWatcher {
    override fun transmissionProgress(progress: TransmissionProgress) {
        progressCallback.onProgress(progress.current.toLong(), progress.total.toLong())
    }
}

internal fun ProgressCallback.toProgressWatcher(): ProgressWatcher {
    return ProgressWatcherWrapper(this)
}
