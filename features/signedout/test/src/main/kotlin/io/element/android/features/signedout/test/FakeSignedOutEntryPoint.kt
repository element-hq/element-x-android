/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.features.signedout.api.SignedOutEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError

class FakeSignedOutEntryPoint(
    private val createNodeResult: (BuildContext, SignedOutEntryPoint.Params) -> Node = { _, _ -> lambdaError() },
) : SignedOutEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: SignedOutEntryPoint.Params,
    ): Node = createNodeResult(buildContext, params)
}
