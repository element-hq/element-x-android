/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.pip

import android.os.Build
import androidx.annotation.RequiresApi

interface PipView {
    @RequiresApi(Build.VERSION_CODES.O)
    fun setPipOrientation(orientation: Int?)
    @RequiresApi(Build.VERSION_CODES.O)
    fun setPipParams()
    @RequiresApi(Build.VERSION_CODES.O)
    fun enterPipMode(): Boolean
    fun hangUp()
}
