/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.widget.impl.ui

import io.element.android.features.widget.api.WidgetActivityData
import io.element.android.libraries.matrix.api.core.SessionId

fun WidgetActivityData.getSessionId(): SessionId {
    return sessionId
}

