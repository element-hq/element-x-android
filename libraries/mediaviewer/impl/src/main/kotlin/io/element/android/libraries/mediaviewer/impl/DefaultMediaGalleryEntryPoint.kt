/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.mediaviewer.api.MediaGalleryEntryPoint
import io.element.android.libraries.mediaviewer.impl.gallery.root.MediaGalleryRootNode

@ContributesBinding(AppScope::class)
@Inject class DefaultMediaGalleryEntryPoint : MediaGalleryEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): MediaGalleryEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : MediaGalleryEntryPoint.NodeBuilder {
            override fun callback(callback: MediaGalleryEntryPoint.Callback): MediaGalleryEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<MediaGalleryRootNode>(buildContext, plugins)
            }
        }
    }
}
