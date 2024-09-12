/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils.snackbar

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A message to be displayed in a [Snackbar].
 * @param messageResId The message to be displayed.
 * @param duration The duration of the message. The default value is [SnackbarDuration.Short].
 * @param actionResId The action text to be displayed. The default value is `null`.
 * @param isDisplayed Used to track if the current message is already displayed or not.
 * @param action The action to be performed when the action is clicked.
 */
data class SnackbarMessage(
    @StringRes val messageResId: Int,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    @StringRes val actionResId: Int? = null,
    val isDisplayed: AtomicBoolean = AtomicBoolean(false),
    val action: () -> Unit = {},
)
