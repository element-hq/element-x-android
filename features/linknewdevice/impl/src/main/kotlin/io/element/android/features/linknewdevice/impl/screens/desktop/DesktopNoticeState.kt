/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.desktop

import io.element.android.libraries.permissions.api.PermissionsState

data class DesktopNoticeState(
    val cameraPermissionState: PermissionsState,
    val canContinue: Boolean,
    val eventSink: (DesktopNoticeEvent) -> Unit,
)
