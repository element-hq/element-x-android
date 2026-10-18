/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs

/**
 * Renders content which is not a Matrix media as a file, the same way the media viewer does.
 *
 * The content is provided by the caller and written to a temporary file, so that it can be
 * displayed, shared and saved on disk.
 */
interface FileViewerEntryPoint : FeatureEntryPoint {
    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: Params,
        callback: Callback,
    ): Node

    /**
     * @param filename the name of the file, used as the screen title and for the shared and saved files.
     * @param mimeType the mime type of [content], it drives how the content is rendered.
     * @param content the whole content of the file.
     */
    data class Params(
        val filename: String,
        val mimeType: String,
        val content: String,
    ) : NodeInputs

    interface Callback : Plugin {
        fun onDone()
    }
}
