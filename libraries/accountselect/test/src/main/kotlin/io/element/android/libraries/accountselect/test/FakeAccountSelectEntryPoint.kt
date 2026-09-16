/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.libraries.accountselect.api.AccountSelectEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError

class FakeAccountSelectEntryPoint(
    private val createNodeResult: (BuildContext, AccountSelectEntryPoint.Callback) -> Node = { _, _ -> lambdaError() },
) : AccountSelectEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: AccountSelectEntryPoint.Callback,
    ): Node = createNodeResult(buildContext, callback)
}
