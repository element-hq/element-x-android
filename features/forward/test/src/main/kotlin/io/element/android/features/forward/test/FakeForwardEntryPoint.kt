/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.forward.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.features.forward.api.ForwardEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError

class FakeForwardEntryPoint : ForwardEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: ForwardEntryPoint.Params,
        callback: ForwardEntryPoint.Callback,
    ): Node = lambdaError()
}
