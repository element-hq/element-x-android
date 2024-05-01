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

import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.children.nodeOrNull
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun <NavTarget : Any> ParentNode<NavTarget>.childNode(navTarget: NavTarget): Node? {
    val childMap = children.value
    val key = childMap.keys.find { it.navTarget == navTarget }
    return childMap[key]?.nodeOrNull
}

suspend inline fun <reified N : Node, NavTarget : Any> ParentNode<NavTarget>.waitForChildAttached(crossinline predicate: (NavTarget) -> Boolean): N =
    suspendCancellableCoroutine { continuation ->
        lifecycleScope.launch {
            children.collect { childMap ->
                val expectedChildNode = childMap.entries
                    .map { it.key.navTarget }
                    .lastOrNull(predicate)
                    ?.let {
                        childNode(it) as? N
                    }
                if (expectedChildNode != null && !continuation.isCompleted) {
                    continuation.resume(expectedChildNode)
                }
            }
        }.invokeOnCompletion {
            continuation.cancel()
        }
    }

/**
 * Wait for a child to be attached to the parent node, only using the NavTarget
 */
suspend inline fun <NavTarget : Any> ParentNode<NavTarget>.waitForNavTargetAttached(crossinline predicate: (NavTarget) -> Boolean) =
    suspendCancellableCoroutine { continuation ->
        lifecycleScope.launch {
            children.collect { childMap ->
                val node = childMap.entries
                    .map { it.key.navTarget }
                    .lastOrNull(predicate)
                if (node != null && !continuation.isCompleted) {
                    continuation.resume(Unit)
                    cancel()
                }
            }
        }.invokeOnCompletion {
            continuation.cancel()
        }
    }
