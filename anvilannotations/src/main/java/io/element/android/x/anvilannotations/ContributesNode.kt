package io.element.android.x.anvilannotations

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
