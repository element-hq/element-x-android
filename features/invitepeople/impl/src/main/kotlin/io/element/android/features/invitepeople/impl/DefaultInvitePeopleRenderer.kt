/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.invitepeople.api.InvitePeopleRenderer
import io.element.android.features.invitepeople.api.InvitePeopleState
import io.element.android.libraries.di.SessionScope
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultInvitePeopleRenderer @Inject constructor() : InvitePeopleRenderer {
    @Composable
    override fun Render(state: InvitePeopleState, modifier: Modifier) {
        if (state is DefaultInvitePeopleState) {
            InvitePeopleView(
                state = state,
                modifier = modifier
            )
        } else {
            error("Unsupported state type: ${state::javaClass}")
        }
    }
}
