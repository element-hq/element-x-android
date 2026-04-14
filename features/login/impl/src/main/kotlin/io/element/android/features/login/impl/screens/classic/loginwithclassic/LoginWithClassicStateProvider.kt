/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.classic.loginwithclassic

import android.graphics.Bitmap
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.UserId

open class LoginWithClassicStateProvider : PreviewParameterProvider<LoginWithClassicState> {
    override val values: Sequence<LoginWithClassicState>
        get() = sequenceOf(
            aLoginWithClassicState(),
            aLoginWithClassicState(isElementPro = true, displayName = "Alice"),
        )
}

fun aLoginWithClassicState(
    isElementPro: Boolean = false,
    userId: UserId = UserId("@alice:matrix.org"),
    displayName: String? = null,
    avatar: Bitmap? = null,
    loginWithClassicAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    loginMode: AsyncData<LoginMode> = AsyncData.Uninitialized,
    eventSink: (LoginWithClassicEvent) -> Unit = {},
) = LoginWithClassicState(
    isElementPro = isElementPro,
    userId = userId,
    displayName = displayName,
    avatar = avatar,
    loginWithClassicAction = loginWithClassicAction,
    loginMode = loginMode,
    eventSink = eventSink,
)
