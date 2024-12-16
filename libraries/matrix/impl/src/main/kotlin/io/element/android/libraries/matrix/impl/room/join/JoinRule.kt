/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.join

import io.element.android.libraries.matrix.api.room.join.JoinRule
import org.matrix.rustcomponents.sdk.JoinRule as RustJoinRule

fun RustJoinRule.map(): JoinRule {
    return when (this) {
        RustJoinRule.Public -> JoinRule.Public
        RustJoinRule.Private -> JoinRule.Private
        RustJoinRule.Knock -> JoinRule.Knock
        RustJoinRule.Invite -> JoinRule.Invite
        is RustJoinRule.Restricted -> JoinRule.Restricted(rules.map { it.map() })
        is RustJoinRule.Custom -> JoinRule.Custom(repr)
        is RustJoinRule.KnockRestricted -> JoinRule.KnockRestricted(rules.map { it.map() })
    }
}
