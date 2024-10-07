/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.fullscreenintent.api

fun aFullScreenIntentPermissionsState(
    permissionGranted: Boolean = true,
    shouldDisplay: Boolean = false,
    openFullScreenIntentSettings: () -> Unit = {},
    dismissFullScreenIntentBanner: () -> Unit = {},
) = FullScreenIntentPermissionsState(
    permissionGranted = permissionGranted,
    shouldDisplayBanner = shouldDisplay,
    openFullScreenIntentSettings = openFullScreenIntentSettings,
    dismissFullScreenIntentBanner = dismissFullScreenIntentBanner,
)
