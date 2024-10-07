/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.oidc.api

import android.app.Activity
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node

interface OidcEntryPoint {
    fun canUseCustomTab(): Boolean
    fun openUrlInCustomTab(activity: Activity, darkTheme: Boolean, url: String)
    fun createFallbackWebViewNode(parentNode: Node, buildContext: BuildContext, url: String): Node
}
