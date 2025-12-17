/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.join

import io.element.android.libraries.matrix.api.room.join.JoinRule
import kotlinx.collections.immutable.toImmutableList
import org.matrix.rustcomponents.sdk.JoinRule as RustJoinRule

fun RustJoinRule.map(): JoinRule {
    return when (this) {
        RustJoinRule.Public -> JoinRule.Public
        RustJoinRule.Private -> JoinRule.Private
        RustJoinRule.Knock -> JoinRule.Knock
        RustJoinRule.Invite -> JoinRule.Invite
        is RustJoinRule.Restricted -> JoinRule.Restricted(rules.map { it.map() }.toImmutableList())
        is RustJoinRule.Custom -> JoinRule.Custom(repr)
        is RustJoinRule.KnockRestricted -> JoinRule.KnockRestricted(rules.map { it.map() }.toImmutableList())
    }
}

fun JoinRule.map(): RustJoinRule {
    return when (this) {
        JoinRule.Public -> RustJoinRule.Public
        JoinRule.Private -> RustJoinRule.Private
        JoinRule.Knock -> RustJoinRule.Knock
        JoinRule.Invite -> RustJoinRule.Invite
        is JoinRule.Restricted -> RustJoinRule.Restricted(rules.map { it.map() })
        is JoinRule.Custom -> RustJoinRule.Custom(value)
        is JoinRule.KnockRestricted -> RustJoinRule.KnockRestricted(rules.map { it.map() })
    }
}
