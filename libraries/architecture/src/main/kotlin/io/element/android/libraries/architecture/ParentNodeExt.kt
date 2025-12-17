/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
 * Wait for a child to be attached to the parent node, only using the NavTarget.
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
