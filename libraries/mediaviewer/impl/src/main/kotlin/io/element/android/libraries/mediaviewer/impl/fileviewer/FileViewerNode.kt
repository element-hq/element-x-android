/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.fileviewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.viewfolder.api.TextFileViewer
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.mediaviewer.api.FileViewerEntryPoint

@ContributesNode(AppScope::class)
@AssistedInject
class FileViewerNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: FileViewerPresenter.Factory,
    private val textFileViewer: TextFileViewer,
) : Node(buildContext, plugins = plugins) {
    private val callback: FileViewerEntryPoint.Callback = callback()
    private val inputs = inputs<FileViewerEntryPoint.Params>()

    private val presenter = presenterFactory.create(params = inputs)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        FileViewerView(
            state = state,
            textFileViewer = textFileViewer,
            onBackClick = callback::onDone,
            modifier = modifier,
        )
    }
}
