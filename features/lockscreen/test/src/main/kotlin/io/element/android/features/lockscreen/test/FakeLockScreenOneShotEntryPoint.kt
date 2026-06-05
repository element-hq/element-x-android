/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.features.lockscreen.api.LockScreenOneShotEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError

class FakeLockScreenOneShotEntryPoint : LockScreenOneShotEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: LockScreenOneShotEntryPoint.Callback,
    ): Node = lambdaError()
}
