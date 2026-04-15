/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.widget.api

/**
 * Entry point for the widget feature.
 */
interface WidgetEntryPoint {
    /**
     * Start a widget of the given type.
     * @param widgetType The type of widget to start.
     */
    fun startWidget(widgetType: WidgetActivityData)
}

