/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.classic.loginwithclassic

import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.UserId

@Stable
data class LoginWithClassicState(
    val isElementPro: Boolean,
    val userId: UserId,
    val displayName: String?,
    val avatar: Bitmap?,
    val loginWithClassicAction: AsyncAction<Unit>,
    val loginMode: AsyncData<LoginMode>,
    val eventSink: (LoginWithClassicEvent) -> Unit,
)
