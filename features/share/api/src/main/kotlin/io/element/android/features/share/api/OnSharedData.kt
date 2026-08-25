/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.api

/**
 * Post-processing to be done once a [ShareIntentData] has been consumed.
 */
fun interface OnSharedData {
    /**
     * @param data the shared content that has just been handled, so any temporary copy of it can be released.
     */
    operator fun invoke(data: ShareIntentData)
}
