/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.bugreport

import android.os.Parcelable
import io.element.android.libraries.architecture.AsyncAction
import kotlinx.parcelize.Parcelize

data class BugReportState(
    val formState: BugReportFormState,
    val hasCrashLogs: Boolean,
    val screenshotUri: String?,
    val sendingProgress: Float,
    val sending: AsyncAction<Unit>,
    val eventSink: (BugReportEvents) -> Unit
) {
    val submitEnabled = sending !is AsyncAction.Loading
    val isDescriptionInError = sending is AsyncAction.Failure &&
        sending.error is BugReportFormError.DescriptionTooShort
}

@Parcelize
data class BugReportFormState(
    val description: String,
    val sendLogs: Boolean,
    val canContact: Boolean,
    val sendScreenshot: Boolean
) : Parcelable {
    companion object {
        val Default = BugReportFormState(
            description = "",
            sendLogs = true,
            canContact = false,
            sendScreenshot = false
        )
    }
}
