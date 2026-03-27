/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.widget.test

import io.element.android.features.widget.api.WidgetEntryPoint
import io.element.android.features.widget.api.WidgetActivityData
import io.element.android.tests.testutils.lambda.lambdaError

class FakeWidgetEntryPoint(
    var startWidgetResult: (WidgetActivityData) -> Unit = { lambdaError() },
) : WidgetEntryPoint {
    override fun startWidget(widgetType: WidgetActivityData) {
        startWidgetResult(widgetType)
    }
}

