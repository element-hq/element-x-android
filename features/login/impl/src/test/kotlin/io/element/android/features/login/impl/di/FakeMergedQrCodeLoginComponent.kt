/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.di

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.features.login.impl.qrcode.FakeQrCodeLoginManager
import io.element.android.features.login.impl.qrcode.QrCodeLoginFlowNode
import io.element.android.features.login.impl.qrcode.QrCodeLoginManager
import io.element.android.libraries.architecture.AssistedNodeFactory
import io.element.android.libraries.architecture.createNode

internal class FakeMergedQrCodeLoginComponent(private val qrCodeLoginManager: QrCodeLoginManager) :
    MergedQrCodeLoginComponent {
    // Ignore this error, it does override a method once code generation is done
    override fun qrCodeLoginManager(): QrCodeLoginManager = qrCodeLoginManager

    class Builder(private val qrCodeLoginManager: QrCodeLoginManager = FakeQrCodeLoginManager()) :
        QrCodeLoginComponent.Builder {
        override fun build(): QrCodeLoginComponent {
            return FakeMergedQrCodeLoginComponent(qrCodeLoginManager)
        }
    }

    override fun nodeFactories(): Map<Class<out Node>, AssistedNodeFactory<*>> {
        return mapOf(
            QrCodeLoginFlowNode::class.java to object : AssistedNodeFactory<QrCodeLoginFlowNode> {
                override fun create(buildContext: BuildContext, plugins: List<Plugin>): QrCodeLoginFlowNode {
                    return createNode<QrCodeLoginFlowNode>(buildContext, plugins)
                }
            }
        )
    }
}
