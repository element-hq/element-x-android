/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.api.localnetwork

/**
 * Which rationale dialog (if any) should be rendered on top of the caller's UI.
 *
 * [Rationale] is shown before the runtime prompt so the user knows why the permission is needed.
 * [Settings] is shown when the OS says a rationale can no longer be shown (permanently denied).
 */
enum class LocalNetworkPermissionDialog {
    None,
    Rationale,
    Settings,
}
