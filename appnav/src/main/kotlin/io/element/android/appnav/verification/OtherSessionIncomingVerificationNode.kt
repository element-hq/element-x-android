/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.verification

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.verifysession.api.IncomingVerificationEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.DependencyInjectionGraphOwner
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.SessionGraphFactory
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import kotlinx.parcelize.Parcelize

/**
 * Display an incoming verification request received by a session which is not the current one, without switching the current session.
 * It sets up the Session graph of the session receiving the request.
 */
@ContributesNode(AppScope::class)
@AssistedInject
class OtherSessionIncomingVerificationNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    sessionGraphFactory: SessionGraphFactory,
    matrixClientProvider: MatrixClientProvider,
    private val incomingVerificationEntryPoint: IncomingVerificationEntryPoint,
) : ParentNode<OtherSessionIncomingVerificationNode.NavTarget>(
    navModel = PermanentNavModel(
        navTargets = setOf(NavTarget),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
), DependencyInjectionGraphOwner {
    interface Callback : Plugin {
        fun onDone()
    }

    @Parcelize
    object NavTarget : Parcelable

    data class Inputs(
        val sessionId: SessionId,
        val verificationRequest: VerificationRequest.Incoming,
    ) : NodeInputs

    private val callback: Callback = callback()
    private val inputs: Inputs = inputs()
    override val graph = sessionGraphFactory.create(
        requireNotNull(matrixClientProvider.getOrNull(inputs.sessionId)) { "No MatrixClient for session ${inputs.sessionId}" }
    )

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return incomingVerificationEntryPoint.createNode(
            parentNode = this,
            buildContext = buildContext,
            params = IncomingVerificationEntryPoint.Params(inputs.verificationRequest),
            callback = object : IncomingVerificationEntryPoint.Callback {
                override fun onDone() {
                    callback.onDone()
                }
            },
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = navModel,
            modifier = modifier,
        )
    }
}
