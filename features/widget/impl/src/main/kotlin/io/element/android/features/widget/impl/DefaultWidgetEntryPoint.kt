/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.widget.impl

import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.widget.api.WidgetEntryPoint
import io.element.android.features.widget.api.WidgetActivityData
import io.element.android.features.widget.impl.ui.WidgetActivity
import io.element.android.libraries.di.annotations.ApplicationContext

@ContributesBinding(AppScope::class)
class DefaultWidgetEntryPoint(
    @ApplicationContext private val context: Context,
) : WidgetEntryPoint {
    companion object {
        const val EXTRA_WIDGET_TYPE = "EXTRA_WIDGET_TYPE"
    }

    override fun startWidget(widgetType: WidgetActivityData) {
        val intent = Intent(context, WidgetActivity::class.java).apply {
            putExtra(EXTRA_WIDGET_TYPE, widgetType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
        }
        context.startActivity(intent)
    }
}

