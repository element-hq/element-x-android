/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.user

import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.api.user.UserStatus
import org.matrix.rustcomponents.sdk.UserCall
import org.matrix.rustcomponents.sdk.UserStatus as RustUserStatus

fun UserStatus.into(): RustUserStatus {
    return RustUserStatus(emoji = emoji, text = text)
}

fun RustUserStatus.into(): UserStatus {
    return UserStatus(emoji = emoji, text = text)
}

fun DisplayedStatus.Companion.from(userStatus: RustUserStatus?, userCall: UserCall?): DisplayedStatus? {
    return when {
        userStatus != null -> DisplayedStatus.UserSet(userStatus.into())
        userCall != null -> DisplayedStatus.InCall(userCall.callJoinedTs?.toLong())
        else -> null
    }
}
