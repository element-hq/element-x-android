/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture.appyx

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import com.bumble.appyx.core.composable.Child
import com.bumble.appyx.core.composable.ChildRenderer
import com.bumble.appyx.core.composable.ChildTransitionScope
import com.bumble.appyx.core.navigation.NavKey
import com.bumble.appyx.core.navigation.NavModel
import com.bumble.appyx.core.navigation.transition.JumpToEndTransitionHandler
import com.bumble.appyx.core.navigation.transition.TransitionBounds
import com.bumble.appyx.core.navigation.transition.TransitionDescriptor
import com.bumble.appyx.core.navigation.transition.TransitionHandler
import com.bumble.appyx.core.navigation.transition.TransitionParams
import com.bumble.appyx.core.node.LocalMovableContentMap
import com.bumble.appyx.core.node.LocalNodeTargetVisibility
import com.bumble.appyx.core.node.LocalSharedElementScope
import com.bumble.appyx.core.node.ParentNode
import io.element.android.libraries.core.coroutine.withPreviousValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import timber.log.Timber
import kotlin.reflect.KClass

//////////////////////////////////////////////////////////////////////////////////////////////////////////
// All the components in this file come from Appyx, and they've been modified to fix an issue with
// the saved state. The parts that are modified are marked.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

@Immutable
class SafeChildrenTransitionScope<T : Any, S>(
    val transitionHandler: TransitionHandler<T, S>,
    val transitionParams: TransitionParams,
    val navModel: NavModel<T, S>
) {

    @Composable
    inline fun <reified V : T> ParentNode<T>.children(
        noinline block: @Composable ChildTransitionScope<S>.(
            child: ChildRenderer,
            transitionDescriptor: TransitionDescriptor<T, S>
        ) -> Unit,
    ) {
        safeChildren(V::class, block)
    }

    @Composable
    inline fun <reified V : T> ParentNode<T>.children(
        noinline block: @Composable ChildTransitionScope<S>.(child: ChildRenderer) -> Unit,
    ) {
        safeChildren(V::class, block)
    }

    @Composable
    @SuppressLint("ComposableNaming")
    fun ParentNode<T>.safeChildren(
        clazz: KClass<out T>,
        block: @Composable ChildTransitionScope<S>.(ChildRenderer) -> Unit,
    ) {
        _safeChildren(clazz) { scope, child, _ ->
            scope.block(child)
        }
    }

    @Composable
    @SuppressLint("ComposableNaming")
    fun ParentNode<T>.safeChildren(
        clazz: KClass<out T>,
        block: @Composable ChildTransitionScope<S>.(
            ChildRenderer,
            TransitionDescriptor<T, S>
        ) -> Unit,
    ) {
        _safeChildren(clazz) { scope, child, descriptor ->
            scope.block(
                child,
                descriptor,
            )
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    private fun ParentNode<T>._safeChildren(
        clazz: KClass<out T>,
        block: @Composable (
            transitionScope: ChildTransitionScope<S>,
            child: ChildRenderer,
            transitionDescriptor: TransitionDescriptor<T, S>
        ) -> Unit
    ) {
        val saveableStateHolder = rememberSaveableStateHolder()

        val disposedNavKeys = remember { mutableSetOf<NavKey<T>>() }

        LaunchedEffect(navModel) {
            navModel
                .removedElementKeys()
                .map { list ->
                    list.filter { clazz.isInstance(it.navTarget) }
                }
                ////////// MODIFIED ////////////
                .filter { it.isNotEmpty() }
                .collect { deletedKeys ->
                    deletedKeys.forEach { navKey ->
                        // Wait for the NavKey to be disposed before removing its key from saveableStateHolder:
                        // Otherwise, the child SaveableStateRegistry will be removed but not the `SavedState`, which will accumulate
                        // and may cause TransactionTooLargeExceptions
                        while (!disposedNavKeys.contains(navKey)) {
                            delay(10)
                        }
                        disposedNavKeys.remove(navKey)
                        Timber.v("Removed NavKey ${navKey} from saveableStateHolder. NavTarget: ${navKey.navTarget}")
                        saveableStateHolder.removeState(navKey)
                    }
                }
                ////////// END OF MODIFIED ////////////
        }

        val screenStateFlow = remember {
            this@SafeChildrenTransitionScope
                .navModel
                .screenState
        }

        val children by screenStateFlow.collectAsState()

        children
            .onScreen
            .filter { clazz.isInstance(it.key.navTarget) }
            .forEach { navElement ->
                key(navElement.key.id) {
                    CompositionLocalProvider(
                        LocalNodeTargetVisibility provides
                            children.onScreenWithVisibleTargetState.contains(navElement)
                    ) {
                        Child(
                            navElement,
                            saveableStateHolder,
                            transitionParams,
                            transitionHandler,
                            block
                        )

                        ////////// MODIFIED ////////////
                        DisposableEffect(navElement.key) {
                            onDispose {
                                Timber.v("Disposed NavKey ${navElement.key}. NavTarget: ${navElement.key.navTarget}")
                                disposedNavKeys.add(navElement.key)
                            }
                        }
                        ////////// END OF MODIFIED ////////////
                    }
                }
            }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
inline fun <reified NavTarget : Any, State> ParentNode<NavTarget>.SafeChildren(
    navModel: NavModel<NavTarget, State>,
    modifier: Modifier = Modifier,
    transitionHandler: TransitionHandler<NavTarget, State> = remember { JumpToEndTransitionHandler() },
    withSharedElementTransition: Boolean = false,
    withMovableContent: Boolean = false,
    noinline block: @Composable SafeChildrenTransitionScope<NavTarget, State>.() -> Unit = {
        children<NavTarget> { child ->
            child()
        }
    }
) {
    val density = LocalDensity.current.density
    var transitionBounds by remember { mutableStateOf(IntSize(0, 0)) }
    val transitionParams by remember(transitionBounds) {
        derivedStateOf {
            TransitionParams(
                bounds = TransitionBounds(
                    width = Dp(transitionBounds.width / density),
                    height = Dp(transitionBounds.height / density)
                )
            )
        }
    }
    if (withSharedElementTransition) {
        SharedTransitionLayout(modifier = modifier
            .onSizeChanged {
                transitionBounds = it
            }
        ) {
            CompositionLocalProvider(
                /** LocalSharedElementScope will be consumed by children UI to apply shareElement modifier */
                LocalSharedElementScope provides this,
                LocalMovableContentMap provides if (withMovableContent) mutableMapOf() else null
            ) {
                block(
                    SafeChildrenTransitionScope(
                        transitionHandler = transitionHandler,
                        transitionParams = transitionParams,
                        navModel = navModel
                    )
                )
            }
        }
    } else {
        Box(modifier = modifier
            .onSizeChanged {
                transitionBounds = it
            }
        ) {
            CompositionLocalProvider(
                /** If sharedElement is not supported for this Node - provide null otherwise children
                 * can consume ascendant's LocalSharedElementScope */
                LocalSharedElementScope provides null,
                LocalMovableContentMap provides if (withMovableContent) mutableMapOf() else null
            ) {
                block(
                    SafeChildrenTransitionScope(
                        transitionHandler = transitionHandler,
                        transitionParams = transitionParams,
                        navModel = navModel
                    )
                )
            }
        }
    }
}

internal fun <T: Any, S> NavModel<T, S>.removedElementKeys(): Flow<List<NavKey<T>>> {
    return this.elements.withPreviousValue()
        .map { (previous, current) ->
            val previousKeys = previous?.map { it.key }.orEmpty()
            val currentKeys = current.map { it.key }
            previousKeys.filter { element ->
                !currentKeys.contains(element)
            }
        }
}
