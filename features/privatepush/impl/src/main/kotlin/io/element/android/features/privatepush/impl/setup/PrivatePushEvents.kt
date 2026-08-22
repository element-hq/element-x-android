/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.setup

sealed interface PrivatePushEvents {
    data object Continue : PrivatePushEvents
    data object Back : PrivatePushEvents
    data object Later : PrivatePushEvents

    /** Like [Later], and also silences the push-registration error that re-opens the flow. */
    data object DontAskAgain : PrivatePushEvents
    data object InstallFromPlayStore : PrivatePushEvents
    data object InstallFromFdroid : PrivatePushEvents
    data object DownloadFromFeral : PrivatePushEvents
    data object InstallDownloaded : PrivatePushEvents
    data object CopyAddress : PrivatePushEvents
    data object OpenNtfy : PrivatePushEvents
    data object Activate : PrivatePushEvents
    data object GoToInstall : PrivatePushEvents
    data object GoToConfigure : PrivatePushEvents
    data object OpenTroubleshoot : PrivatePushEvents

    /** Sent by the view on ON_RESUME (the member comes back from a store, the installer or ntfy). */
    data object Refresh : PrivatePushEvents
    data object Finish : PrivatePushEvents
}
