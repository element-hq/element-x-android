/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.file

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData

open class ViewFileStateProvider : PreviewParameterProvider<ViewFileState> {
    override val values: Sequence<ViewFileState>
        get() = sequenceOf(
            aViewFileState(),
            aViewFileState(lines = AsyncData.Loading()),
            aViewFileState(lines = AsyncData.Failure(Exception("A failure"))),
            aViewFileState(lines = AsyncData.Success(emptyList())),
            aViewFileState(
                name = "logcat.log",
                lines = AsyncData.Success(
                    listOf(
                        "Line 1",
                        "Line 2",
                        "Line 3 lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor" +
                            " incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,",
                        "01-23 13:14:50.740 25818 25818 V verbose",
                        "01-23 13:14:50.740 25818 25818 D debug",
                        "01-23 13:14:50.740 25818 25818 I info",
                        "01-23 13:14:50.740 25818 25818 W warning",
                        "01-23 13:14:50.740 25818 25818 E error",
                        "01-23 13:14:50.740 25818 25818 A assertion",
                    )
                ),
                colorationMode = ColorationMode.Logcat,
            ),
            aViewFileState(
                name = "logs.2024-01-26",
                lines = AsyncData.Success(
                    listOf(
                        "Line 1",
                        "Line 2",
                        "Line 3 lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor" +
                            " incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,",
                        "2024-01-26T10:22:26.947416Z TRACE trace",
                        "2024-01-26T10:22:26.947416Z DEBUG debug",
                        "2024-01-26T10:22:26.947416Z  INFO info",
                        "2024-01-26T10:22:26.947416Z  WARN warn",
                        "2024-01-26T10:22:26.947416Z ERROR error",
                    )
                ),
                colorationMode = ColorationMode.RustLogs,
            )
        )
}

fun aViewFileState(
    name: String = "aName",
    lines: AsyncData<List<String>> = AsyncData.Uninitialized,
    colorationMode: ColorationMode = ColorationMode.None,
) = ViewFileState(
    name = name,
    lines = lines,
    colorationMode = colorationMode,
    eventSink = {},
)
