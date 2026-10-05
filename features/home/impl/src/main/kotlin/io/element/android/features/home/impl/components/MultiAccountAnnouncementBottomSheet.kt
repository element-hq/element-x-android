/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.home.impl.R
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.annotatedTextWithBold
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch

/**
 * Bottom sheet announcing that several accounts can now be added.
 * Ref: https://www.figma.com/design/kEAcfun9iSpszeUDvdKZ6b/ER-351--Multi-account-in-EX?node-id=195-43073
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MultiAccountAnnouncementBottomSheet(
    onDismiss: () -> Unit,
    onAddAccountClick: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
    )
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        scrollable = false,
    ) {
        MultiAccountAnnouncementContent(
            onAddAccountClick = {
                coroutineScope.launch {
                    sheetState.hide()
                    onDismiss()
                    onAddAccountClick()
                }
            },
        )
    }
}

@Composable
private fun MultiAccountAnnouncementContent(
    onAddAccountClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BigIcon(
            style = BigIcon.Style.Default(CompoundIcons.UserProfileSolid()),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics { heading() },
            text = stringResource(R.string.screen_multi_account_announcement_title),
            style = ElementTheme.typography.fontHeadingMdBold,
            color = ElementTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        val path = stringResource(CommonStrings.common_settings) + " > " + stringResource(CommonStrings.common_add_another_account)
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = annotatedTextWithBold(
                text = stringResource(R.string.screen_multi_account_announcement_description, path),
                boldText = path,
            ),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        Button(
            text = stringResource(R.string.screen_multi_account_announcement_action),
            leadingIcon = IconSource.Vector(CompoundIcons.Plus()),
            modifier = Modifier.fillMaxWidth(),
            onClick = onAddAccountClick,
        )
        Spacer(Modifier.height(16.dp))
    }
}

@PreviewsDayNight
@Composable
internal fun MultiAccountAnnouncementContentPreview() = ElementPreview(fillMaxSize = true) {
    MultiAccountAnnouncementBottomSheet(
        onDismiss = {},
        onAddAccountClick = {},
    )
}
