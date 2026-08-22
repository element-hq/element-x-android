/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.features.privatepush.api.PrivatePushEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError

class FakePrivatePushEntryPoint : PrivatePushEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: PrivatePushEntryPoint.Callback,
    ): Node = lambdaError()
}
