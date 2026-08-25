/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api

import android.content.Context

/**
 * Creates a [PttInputSource]. Contributed into a map multibinding keyed by [PttInputSourceId] (see
 * [io.element.android.features.ptt.api.di.PttInputSourceKey]); the coordinator instantiates only the
 * sources whose [isAvailable] returns true on this device/build.
 */
interface PttInputSourceFactory {
    val id: PttInputSourceId

    /** Whether this source can run here (e.g. an OEM adapter gates on [android.os.Build.MANUFACTURER]). */
    fun isAvailable(context: Context): Boolean

    fun create(context: Context): PttInputSource
}
