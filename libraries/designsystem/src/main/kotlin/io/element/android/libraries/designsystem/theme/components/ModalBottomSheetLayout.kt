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

// This is actually expected, as we should remove this component soon and use ModalBottomSheet instead
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.modifiers.applyIf
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewGroup

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModalBottomSheetLayout(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    sheetShape: Shape = MaterialTheme.shapes.large.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)),
    sheetElevation: Dp = ModalBottomSheetDefaults.Elevation,
    sheetBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    scrimColor: Color = ModalBottomSheetDefaults.scrimColor,
    displayHandle: Boolean = false,
    useSystemPadding: Boolean = true,
    content: @Composable () -> Unit = {}
) {
    androidx.compose.material.ModalBottomSheetLayout(
        sheetContent = {
            Column(
                Modifier.fillMaxWidth()
                    .applyIf(useSystemPadding, ifTrue = {
                        navigationBarsPadding()
                    })
            ) {
                if (displayHandle) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp))
                            .size(width = 32.dp, height = 4.dp)
                            .align(Alignment.CenterHorizontally),
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                sheetContent()
            }
        },
        modifier = modifier,
        sheetState = sheetState,
        sheetShape = sheetShape,
        sheetElevation = sheetElevation,
        sheetBackgroundColor = sheetBackgroundColor,
        sheetContentColor = sheetContentColor,
        scrimColor = scrimColor,
        content = content,
    )
}

@Preview(group = PreviewGroup.BottomSheets)
@Composable
internal fun ModalBottomSheetLayoutLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview(group = PreviewGroup.BottomSheets)
@Composable
internal fun ModalBottomSheetLayoutDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@OptIn(ExperimentalMaterialApi::class)
@ExcludeFromCoverage
@Composable
private fun ContentToPreview() {
    ModalBottomSheetLayout(
        modifier = Modifier.height(140.dp),
        displayHandle = true,
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Expanded, density = LocalDensity.current),
        sheetContent = {
            Text(text = "Sheet Content", modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
                .background(color = Color.Green))
        }
    ) {
        Text(text = "Content", modifier = Modifier.background(color = Color.Red))
    }
}
