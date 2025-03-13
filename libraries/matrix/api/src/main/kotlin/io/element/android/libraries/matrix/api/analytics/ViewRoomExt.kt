/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.analytics

import im.vector.app.features.analytics.plan.ViewRoom
import io.element.android.libraries.matrix.api.room.MatrixRoom

fun MatrixRoom.toAnalyticsViewRoom(
    trigger: ViewRoom.Trigger? = null,
    selectedSpace: MatrixRoom? = null,
    viaKeyboard: Boolean? = null,
): ViewRoom {
    val activeSpace = selectedSpace?.toActiveSpace() ?: ViewRoom.ActiveSpace.Home

    return ViewRoom(
        isDM = info.isDirect,
        isSpace = info.isSpace,
        trigger = trigger,
        activeSpace = activeSpace,
        viaKeyboard = viaKeyboard
    )
}

private fun MatrixRoom.toActiveSpace(): ViewRoom.ActiveSpace {
    return if (info.isPublic) ViewRoom.ActiveSpace.Public else ViewRoom.ActiveSpace.Private
}
