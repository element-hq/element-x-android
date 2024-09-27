/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import io.element.android.features.call.impl.utils.WidgetMessageInterceptor

sealed interface CallScreenEvents {
    data object Hangup : CallScreenEvents
    data class SetupMessageChannels(val widgetMessageInterceptor: WidgetMessageInterceptor) : CallScreenEvents
    data class OnWebViewError(val description: String?) : CallScreenEvents
}
