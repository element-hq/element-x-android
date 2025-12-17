/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.di

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.features.login.impl.qrcode.QrCodeLoginFlowNode
import io.element.android.features.login.impl.qrcode.QrCodeLoginManager
import io.element.android.libraries.architecture.AssistedNodeFactory
import kotlin.reflect.KClass

internal class FakeQrCodeLoginGraph(
    private val qrCodeLoginManager: QrCodeLoginManager,
) : QrCodeLoginGraph, QrCodeLoginBindings {
    override fun nodeFactories(): Map<KClass<out Node>, AssistedNodeFactory<*>> {
        return mapOf(
            QrCodeLoginFlowNode::class to object : AssistedNodeFactory<QrCodeLoginFlowNode> {
                override fun create(buildContext: BuildContext, plugins: List<Plugin>): QrCodeLoginFlowNode {
                    error("This factory should not be called in tests")
                }
            }
        )
    }

    override fun qrCodeLoginManager(): QrCodeLoginManager = qrCodeLoginManager

    internal class Builder(
        private val qrCodeLoginManager: QrCodeLoginManager,
    ) : QrCodeLoginGraph.Factory {
        override fun create(): QrCodeLoginGraph {
            return FakeQrCodeLoginGraph(qrCodeLoginManager)
        }
    }
}
