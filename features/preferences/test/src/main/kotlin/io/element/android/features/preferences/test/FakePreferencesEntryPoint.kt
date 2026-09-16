/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError

class FakePreferencesEntryPoint : PreferencesEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: PreferencesEntryPoint.Params,
        callback: PreferencesEntryPoint.Callback,
    ): Node {
        lambdaError()
    }

    override fun createAppDeveloperSettingsNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: PreferencesEntryPoint.DeveloperSettingsCallback,
    ): Node {
        lambdaError()
    }
}
