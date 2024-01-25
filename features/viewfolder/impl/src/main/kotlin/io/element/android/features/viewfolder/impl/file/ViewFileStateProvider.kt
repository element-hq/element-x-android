/*
 * Copyright (c) 2024 New Vector Ltd
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
                )
            )
        )
}

fun aViewFileState(
    name: String = "aName",
    lines: AsyncData<List<String>> = AsyncData.Uninitialized,
) = ViewFileState(
    name = name,
    lines = lines,
    eventSink = {},
)
