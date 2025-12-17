/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture

import androidx.lifecycle.Lifecycle
import com.bumble.appyx.core.lifecycle.subscribe
import timber.log.Timber

fun Lifecycle.logLifecycle(name: String) {
    subscribe(
        onCreate = { Timber.tag("Lifecycle").d("onCreate $name") },
        onPause = { Timber.tag("Lifecycle").d("onPause $name") },
        onResume = { Timber.tag("Lifecycle").d("onResume $name") },
        onDestroy = { Timber.tag("Lifecycle").d("onDestroy $name") },
    )
}
