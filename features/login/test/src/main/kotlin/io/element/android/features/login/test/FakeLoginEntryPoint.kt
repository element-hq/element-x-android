/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.features.login.api.LoginEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError

class FakeLoginEntryPoint(
    private val createNodeResult: (BuildContext, LoginEntryPoint.Params) -> Node = { _, _ -> lambdaError() },
) : LoginEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: LoginEntryPoint.Params,
        callback: LoginEntryPoint.Callback,
    ): Node = createNodeResult(buildContext, params)
}
