/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.impl.utils

interface PipController {
    suspend fun canEnterPip(): Boolean
    fun enterPip()
    fun exitPip()
}
