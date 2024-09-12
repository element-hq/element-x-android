/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.fullscreenintent.api

data class FullScreenIntentPermissionsState(
    val permissionGranted: Boolean,
    val shouldDisplayBanner: Boolean,
    val dismissFullScreenIntentBanner: () -> Unit,
    val openFullScreenIntentSettings: () -> Unit,
)
