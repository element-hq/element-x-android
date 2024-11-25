/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import io.element.android.libraries.matrix.api.createroom.JoinRuleOverride

enum class RoomAccess {
    Anyone,
    Knocking
}

fun RoomAccess.toJoinRule(): JoinRuleOverride {
    return when (this) {
        RoomAccess.Anyone -> JoinRuleOverride.None
        RoomAccess.Knocking -> JoinRuleOverride.Knock
    }
}
