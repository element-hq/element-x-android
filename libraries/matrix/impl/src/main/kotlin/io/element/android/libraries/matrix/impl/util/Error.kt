/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.util

import org.matrix.rustcomponents.sdk.ClientException
import timber.log.Timber

fun logError(throwable: Throwable) {
    when (throwable) {
        is ClientException.Generic -> {
            Timber.e("Error ${throwable.msg}", throwable)
        }
        else -> {
            Timber.e("Error", throwable)
        }
    }
}
