/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.oidc.impl

import android.app.Activity
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.oidc.impl.webview.OidcNode
import io.element.android.libraries.oidc.api.OidcEntryPoint
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultOidcEntryPoint @Inject constructor(
    private val customTabAvailabilityChecker: CustomTabAvailabilityChecker,
) : OidcEntryPoint {
    override fun canUseCustomTab(): Boolean {
        return customTabAvailabilityChecker.supportCustomTab()
    }

    override fun openUrlInCustomTab(activity: Activity, darkTheme: Boolean, url: String) {
        assert(canUseCustomTab()) { "Custom tab is not supported in this device." }
        activity.openUrlInChromeCustomTab(null, darkTheme, url)
    }

    override fun createFallbackWebViewNode(parentNode: Node, buildContext: BuildContext, url: String): Node {
        assert(!canUseCustomTab()) { "Custom tab should be used instead of the fallback node." }
        val inputs = OidcNode.Inputs(OidcDetails(url))
        return parentNode.createNode<OidcNode>(buildContext, listOf(inputs))
    }
}
