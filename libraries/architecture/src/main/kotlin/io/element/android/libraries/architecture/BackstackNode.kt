/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.architecture

import androidx.compose.runtime.Stable
import com.bumble.appyx.core.children.ChildEntry
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack

/**
 * This class is just an helper for configuring a backstack directly in the constructor.
 * With this we can more easily use constructor injection without having a secondary constructor to create the [BackStack] instance.
 * Can be used instead of [ParentNode] in flow nodes.
 */
@Stable
abstract class BackstackNode<NavTarget : Any>(
    val backstack: BackStack<NavTarget>,
    buildContext: BuildContext,
    childKeepMode: ChildEntry.KeepMode = ChildEntry.KeepMode.KEEP,
    plugins: List<Plugin>
) : ParentNode<NavTarget>(
    navModel = backstack,
    buildContext = buildContext,
    plugins = plugins,
    childKeepMode = childKeepMode,
)
