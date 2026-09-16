/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.notification.CallIntent

/**
 * Represents the consensus state of [CallIntent] among room members.
 * Call members can advertise their intent to use audio or video, clients can
 * use this in the UI and also to decide to start camera or not when joining.
 *
 * This enum distinguishes between full consensus (all members advertise and
 * agree), partial consensus (only some members advertise, but those who do
 * agree), and no consensus (either no one advertises or advertisers disagree).
 */
sealed interface CallIntentConsensus {
    /**
     * All members advertise and agree on the same [callIntent].
     */
    data class Full(val callIntent: CallIntent) : CallIntentConsensus

    /**
     * Some members advertise and agree on the same [callIntent], but not all of them.
     */
    data class Partial(
        /** The call intent that the agreeing members have advertised. */
        val callIntent: CallIntent,
        /** The number of members who advertise and agree on the same [callIntent]. */
        val agreeingCount: Int,
        /** The total number of members in the call. */
        val totalCount: Int,
    ) : CallIntentConsensus

    /**
     * No consensus. No one advertises or advertisers disagree.
     */
    data object None : CallIntentConsensus
}
