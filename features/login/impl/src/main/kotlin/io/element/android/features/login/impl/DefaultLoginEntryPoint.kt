/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.login.api.LoginEntryPoint
import io.element.android.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultLoginEntryPoint : LoginEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: LoginEntryPoint.Params,
        callback: LoginEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<LoginFlowNode>(
            buildContext = buildContext,
            plugins = listOf(
                LoginFlowNode.Params(
                    accountProvider = params.accountProvider,
                    loginHint = params.loginHint,
                ),
                callback,
            )
        )
    }
}
