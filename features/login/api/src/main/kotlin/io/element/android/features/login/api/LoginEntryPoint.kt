/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.api

import android.os.Parcelable
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.libraries.architecture.FeatureEntryPoint
import kotlinx.parcelize.Parcelize

interface LoginEntryPoint : FeatureEntryPoint {
    data class Params(
        val flowType: LoginFlowType
    )

    fun nodeBuilder(parentNode: Node, buildContext: BuildContext): NodeBuilder

    interface NodeBuilder {
        fun params(params: Params): NodeBuilder
        fun build(): Node
    }
}

@Parcelize
enum class LoginFlowType : Parcelable {
    SIGN_IN_MANUAL,
    SIGN_IN_QR_CODE,
    SIGN_UP
}
