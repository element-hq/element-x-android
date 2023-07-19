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
