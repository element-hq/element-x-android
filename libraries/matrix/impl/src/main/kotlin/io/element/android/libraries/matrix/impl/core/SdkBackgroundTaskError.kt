/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.core

import uniffi.matrix_sdk_common.BackgroundTaskFailureReason

/**
 * Error thrown when a background SDK task panics and can't recover.
 * @param task The name of the task that failed.
 * @param reason The cause of this error.
 */
class SdkBackgroundTaskError(
    task: String,
    reason: BackgroundTaskFailureReason,
) : Error() {
    override val message: String = run {
        val message = when (reason) {
            is BackgroundTaskFailureReason.EarlyTermination -> "Early termination"
            is BackgroundTaskFailureReason.Error -> "Error: ${reason.error}"
            is BackgroundTaskFailureReason.Panic -> buildString {
                append("Panic (unrecoverable): ")
                reason.message?.let { append(it) }
                reason.panicBacktrace?.let {
                    append("\n")
                    append(it)
                }
            }
        }
        "SDK background task '$task' failure: \n$message"
    }
}
