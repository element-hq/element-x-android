/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.linknewdevice

/**
 * Lets the user confirm or refuse a device linking request once they have approved it in the browser.
 *
 * Handed out with the step that is waiting for authorisation; exactly one of the two methods is meant to be called.
 */
interface ContinuationMessageSender {
    /** Tells the other device that the user refused the linking, which ends the flow. */
    suspend fun cancel(): Result<Unit>

    /** Tells the other device that the user approved the linking, so the flow can carry on. */
    suspend fun confirm(): Result<Unit>
}
