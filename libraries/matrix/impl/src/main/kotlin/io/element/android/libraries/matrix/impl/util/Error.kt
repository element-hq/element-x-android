/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.util

import org.matrix.rustcomponents.sdk.ClientException
import timber.log.Timber

fun logError(throwable: Throwable) {
    when (throwable) {
        is ClientException.Generic -> {
            Timber.e(throwable, "Error ${throwable.msg}")
        }
        else -> {
            Timber.e(throwable, "Error")
        }
    }
}
