/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.mediaviewer.impl.viewer.MediaViewerNode

@ContributesBinding(AppScope::class)
class DefaultMediaViewerEntryPoint : MediaViewerEntryPoint {
    override fun createParamsForAvatar(filename: String, avatarUrl: String): MediaViewerEntryPoint.Params {
        // We need to fake the MimeType here for the viewer to work.
        val mimeType = MimeTypes.Images
        return MediaViewerEntryPoint.Params(
            mode = MediaViewerEntryPoint.MediaViewerMode.SingleMedia,
            eventId = null,
            mediaInfo = MediaInfo(
                filename = filename,
                fileSize = null,
                caption = null,
                mimeType = mimeType,
                formattedFileSize = "",
                fileExtension = "",
                senderId = UserId("@dummy:server.org"),
                senderName = null,
                senderAvatar = null,
                dateSent = null,
                dateSentFull = null,
                waveform = null,
                duration = null,
            ),
            mediaSource = MediaSource(url = avatarUrl),
            thumbnailSource = null,
            canShowInfo = false,
        )
    }

    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: MediaViewerEntryPoint.Params,
        callback: MediaViewerEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<MediaViewerNode>(
            buildContext = buildContext,
            plugins = listOf(params, callback),
        )
    }
}
