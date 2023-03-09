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

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node

/**
 * This interface represents an entrypoint to a feature. Should be used to return the entrypoint node of the feature without exposing the internal types.
 */
interface FeatureEntryPoint

/**
 * Can be used when the feature only exposes a simple node without the need of plugins.
 */
interface SimpleFeatureEntryPoint : FeatureEntryPoint {
    fun createNode(parentNode: Node, buildContext: BuildContext): Node
}
