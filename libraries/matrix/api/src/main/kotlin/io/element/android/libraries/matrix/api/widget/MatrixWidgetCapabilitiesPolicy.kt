/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.widget

/** Defines how a widget driver obtains approval for negotiated Matrix capabilities. */
sealed interface MatrixWidgetCapabilitiesPolicy {
    /** The existing trusted Call flow; never use this policy for arbitrary room widgets. */
    data object ElementCall : MatrixWidgetCapabilitiesPolicy

    /** Deny negotiated Matrix capabilities when the host has no interactive permission UI. */
    data object DenyAll : MatrixWidgetCapabilitiesPolicy

    /**
     * Await native approval of the exact requested permissions. A callback can approve or deny,
     * but cannot add permissions. Grants belong to this driver, not the widget origin globally.
     * OpenID and WebView camera/microphone access require their own separate consent.
     */
    data class AskUser(val approve: suspend (List<MatrixWidgetPermission>) -> Boolean) : MatrixWidgetCapabilitiesPolicy
}
