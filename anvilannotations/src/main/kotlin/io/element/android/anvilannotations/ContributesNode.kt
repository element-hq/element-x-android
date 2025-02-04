/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.anvilannotations

import kotlin.reflect.KClass

/**
 * Adds Node to the specified component graph.
 * Equivalent to the following declaration:
 *
 * @Module
 * @ContributesTo(Scope::class)
 * abstract class YourNodeModule {

 *  @Binds
 *  @IntoMap
 *  @NodeKey(YourNode::class)
 *  abstract fun bindYourNodeFactory(factory: YourNode.Factory): AssistedNodeFactory<*>
 *}

 */
@Target(AnnotationTarget.CLASS)
annotation class ContributesNode(
    val scope: KClass<*>,
)
