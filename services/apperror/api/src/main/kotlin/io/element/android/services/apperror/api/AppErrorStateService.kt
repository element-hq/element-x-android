/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.apperror.api

import androidx.annotation.StringRes
import kotlinx.coroutines.flow.StateFlow

/**
 * Surfaces app-wide error dialogs from anywhere, for the failures that are not tied to the screen the user is on.
 */
interface AppErrorStateService {
    /** The error to display, if any; observed once at the top of the app rather than per screen. */
    val appErrorStateFlow: StateFlow<AppErrorState>

    /**
     * Shows an error dialog with already resolved text.
     *
     * @param title the dialog title.
     * @param body the dialog message; never put user content or secrets in it.
     */
    fun showError(title: String, body: String)

    /**
     * Shows an error dialog from string resources, which is the preferred form since it stays localised.
     *
     * @param titleRes resource id of the dialog title.
     * @param bodyRes resource id of the dialog message.
     */
    fun showError(@StringRes titleRes: Int, @StringRes bodyRes: Int)
}
