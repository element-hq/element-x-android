/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.share.api

import kotlinx.coroutines.CoroutineScope

interface ShareService {
    fun observeFeatureFlag(coroutineScope: CoroutineScope)
}
