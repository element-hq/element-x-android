/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.test

import android.content.Context
import android.content.Intent
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError

class FakeLockScreenEntryPoint : LockScreenEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        navTarget: LockScreenEntryPoint.Target,
        callback: LockScreenEntryPoint.Callback,
    ): Node = lambdaError()

    override fun pinUnlockIntent(context: Context): Intent = lambdaError()
}
