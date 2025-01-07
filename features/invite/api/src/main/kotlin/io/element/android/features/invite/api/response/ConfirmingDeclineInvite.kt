/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.api.response

import io.element.android.libraries.architecture.AsyncAction

data class ConfirmingDeclineInvite(
    val inviteData: InviteData,
) : AsyncAction.Confirming
