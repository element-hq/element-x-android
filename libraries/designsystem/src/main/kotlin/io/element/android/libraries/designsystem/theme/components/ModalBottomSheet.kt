/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.preview.sheetStateForPreview
import io.element.android.libraries.designsystem.utils.LocalUiTestMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = if (ElementTheme.isLightTheme) 0.dp else BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    content: @Composable ColumnScope.() -> Unit,
) {
    val safeSheetState = if (LocalInspectionMode.current) sheetStateForPreview() else sheetState
    // If we're running in UI test mode, we want to use a different shape to avoid
    // this issue: https://issuetracker.google.com/issues/366255137
    val safeShape = if (LocalUiTestMode.current) RoundedCornerShape(12.dp) else shape
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = safeSheetState,
        shape = safeShape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
fun SheetState.hide(coroutineScope: CoroutineScope, then: suspend () -> Unit) {
    coroutineScope.launch {
        hide()
        then()
    }
}

// This preview and its screenshots are blank, see: https://issuetracker.google.com/issues/283843380
@Preview(group = PreviewGroup.BottomSheets)
@Composable
internal fun ModalBottomSheetLightPreview() =
    ElementPreviewLight { ContentToPreview() }

// This preview and its screenshots are blank, see: https://issuetracker.google.com/issues/283843380
@Preview(group = PreviewGroup.BottomSheets)
@Composable
internal fun ModalBottomSheetDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@OptIn(ExperimentalMaterial3Api::class)
@ExcludeFromCoverage
@Composable
private fun ContentToPreview() {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        ModalBottomSheet(
            onDismissRequest = {},
        ) {
            Text(
                text = "Sheet Content",
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
                    .background(color = Color.Green)
            )
        }
    }
}
