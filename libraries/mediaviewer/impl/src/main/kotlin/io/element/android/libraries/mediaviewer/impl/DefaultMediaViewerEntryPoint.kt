/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.mediaviewer.impl.viewer.MediaViewerNode
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultMediaViewerEntryPoint @Inject constructor() : MediaViewerEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): MediaViewerEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : MediaViewerEntryPoint.NodeBuilder {
            override fun callback(callback: MediaViewerEntryPoint.Callback): MediaViewerEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun params(params: MediaViewerEntryPoint.Params): MediaViewerEntryPoint.NodeBuilder {
                plugins += params
                return this
            }

            override fun avatar(filename: String, avatarUrl: String): MediaViewerEntryPoint.NodeBuilder {
                // We need to fake the MimeType here for the viewer to work.
                val mimeType = MimeTypes.Images
                return params(
                    MediaViewerEntryPoint.Params(
                        mediaInfo = MediaInfo(
                            filename = filename,
                            caption = null,
                            mimeType = mimeType,
                            formattedFileSize = "",
                            fileExtension = "",
                            senderName = null,
                            dateSent = null,
                        ),
                        mediaSource = MediaSource(url = avatarUrl),
                        thumbnailSource = null,
                        canDownload = false,
                        canShare = false,
                    )
                )
            }

            override fun build(): Node {
                return parentNode.createNode<MediaViewerNode>(buildContext, plugins)
            }
        }
    }
}
