/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.api

sealed interface PermissionsEvent {
    /**
     * Request the runtime permission. If the app has already recorded a soft denial for this
     * permission, this event is intercepted to raise [PermissionsState.showDialog] instead of
     * hitting the OS — the intent is for the caller to render `PermissionsView` to prompt the
     * user to open system settings.
     *
     * If the caller renders its own "already denied" UI and wants the OS prompt regardless,
     * use [ForceRequestPermissions].
     */
    data object RequestPermissions : PermissionsEvent

    /**
     * Same as [RequestPermissions] but bypasses the soft-denial intercept — always launches
     * the OS permission dialog. The OS may still auto-deny for permanently-denied permissions.
     */
    data object ForceRequestPermissions : PermissionsEvent

    data object CloseDialog : PermissionsEvent
    data object OpenSystemSettingAndCloseDialog : PermissionsEvent
}
