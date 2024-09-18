/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.impl

import android.os.Parcelable
import io.element.android.libraries.architecture.AsyncAction
import kotlinx.parcelize.Parcelize

data class AccountDeactivationState(
    val deactivateFormState: DeactivateFormState,
    val accountDeactivationAction: AsyncAction<Unit>,
    val eventSink: (AccountDeactivationEvents) -> Unit,
) {
    val submitEnabled: Boolean
        get() = accountDeactivationAction is AsyncAction.Uninitialized &&
            deactivateFormState.password.isNotEmpty()
}

@Parcelize
data class DeactivateFormState(
    val eraseData: Boolean,
    val password: String
) : Parcelable {
    companion object {
        val Default = DeactivateFormState(false, "")
    }
}
