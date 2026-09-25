/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Where a native call is drawn, wrapped around the logged-in content.
 *
 * A wrapper rather than an overlay, because the two states of a call need different things from the
 * layout: minimized, the call takes a strip of height above the content; maximized, it covers it. The
 * [Modifier] handed back to [content] has consumed whatever window insets the call has already taken,
 * so the screens underneath do not pad for the status bar a second time.
 *
 * With no call running this renders [content] and nothing else.
 */
interface NativeCallHost {
    @Composable
    fun Render(modifier: Modifier, content: @Composable (Modifier) -> Unit)
}
