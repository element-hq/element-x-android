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

import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import kotlin.properties.ReadOnlyProperty

interface NodeInputs

interface NodeInputsProvider<I : NodeInputs> : Plugin {
    fun inputs(): I
}

inline fun <reified I : NodeInputs> nodeInputsProvider(inputs: I) = object : NodeInputsProvider<I> {
    override fun inputs() = inputs
}

fun <I : NodeInputs> nodeInputs() = ReadOnlyProperty<Node, I> { thisRef, _ ->
    thisRef.plugins<NodeInputsProvider<I>>().first().inputs()
}
