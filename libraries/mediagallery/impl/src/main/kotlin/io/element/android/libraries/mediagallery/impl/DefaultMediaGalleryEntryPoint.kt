/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediagallery.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.mediagallery.api.MediaGalleryEntryPoint
import io.element.android.libraries.mediagallery.impl.gallery.MediaGalleryNode
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultMediaGalleryEntryPoint @Inject constructor() : MediaGalleryEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): MediaGalleryEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : MediaGalleryEntryPoint.NodeBuilder {
            override fun callback(callback: MediaGalleryEntryPoint.Callback): MediaGalleryEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun params(params: MediaGalleryEntryPoint.Params): MediaGalleryEntryPoint.NodeBuilder {
                plugins += params
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<MediaGalleryNode>(buildContext, plugins)
            }
        }
    }
}
