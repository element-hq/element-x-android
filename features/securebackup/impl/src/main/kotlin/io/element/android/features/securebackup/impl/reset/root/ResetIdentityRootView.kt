/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.visuallist.VisualList
import io.element.android.libraries.designsystem.components.visuallist.VisualListItemData
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ResetIdentityRootView(
    state: ResetIdentityRootState,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val variant = state.variant
    val isResetTheOnlyOption = variant?.isResetTheOnlyOption == true
    FlowStepPage(
        modifier = modifier,
        iconStyle = BigIcon.Style.Default(CompoundIcons.UserProfile()),
        title = stringResource(
            if (isResetTheOnlyOption) R.string.screen_encryption_reset_no_options_title else R.string.screen_encryption_reset_title
        ),
        subTitle = stringResource(
            if (isResetTheOnlyOption) R.string.screen_encryption_reset_no_options_subtitle else R.string.screen_encryption_reset_subtitle
        ),
        isScrollable = true,
        content = { Content(variant) },
        buttons = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.screen_encryption_reset_action_continue_reset),
                onClick = {
                    if (isResetTheOnlyOption) {
                        onContinue()
                    } else {
                        state.eventSink(ResetIdentityRootEvent.Continue)
                    }
                },
                enabled = variant != null,
                destructive = !isResetTheOnlyOption,
            )
            if (isResetTheOnlyOption) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(CommonStrings.screen_identity_confirmation_sign_out),
                    onClick = { state.eventSink(ResetIdentityRootEvent.SignOut) },
                )
            }
        },
        // There is nothing to go back to when resetting is the only way to continue.
        onBackClick = onBack.takeUnless { isResetTheOnlyOption },
    )

    if (state.displayConfirmationDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.screen_reset_encryption_confirmation_alert_title),
            content = stringResource(R.string.screen_reset_encryption_confirmation_alert_subtitle),
            submitText = stringResource(R.string.screen_reset_encryption_confirmation_alert_action),
            onSubmitClick = {
                state.eventSink(ResetIdentityRootEvent.DismissDialog)
                onContinue()
            },
            destructiveSubmit = true,
            onDismiss = { state.eventSink(ResetIdentityRootEvent.DismissDialog) }
        )
    }
}

@Composable
private fun Content(variant: ResetIdentityRootVariant?) {
    Column(
        modifier = Modifier.padding(top = 8.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        when (variant) {
            null -> Unit
            ResetIdentityRootVariant.HasConfirmationOptions -> {
                ConsequencesList(showDevicesItem = true)
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElementTheme.colors.bgCriticalSubtle, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    text = stringResource(R.string.screen_encryption_reset_footer),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textCriticalPrimary,
                )
            }
            ResetIdentityRootVariant.NoOptionsWithEncryptedChats -> ConsequencesList(showDevicesItem = false)
            ResetIdentityRootVariant.NoOptionsWithoutEncryptedChats -> {
                VisualList(
                    modifier = Modifier.fillMaxWidth(),
                    items = persistentListOf(
                        visualListItem(
                            message = stringResource(R.string.screen_encryption_reset_no_chats_message),
                            icon = CompoundIcons.Check(),
                            positive = true,
                        )
                    ),
                )
            }
        }
    }
}

@Composable
private fun ConsequencesList(showDevicesItem: Boolean) {
    val items: ImmutableList<VisualListItemData> = buildList {
        add(visualListItem(stringResource(R.string.screen_encryption_reset_bullet_1), CompoundIcons.CheckCircle(), positive = true))
        add(visualListItem(stringResource(R.string.screen_encryption_reset_bullet_2), CompoundIcons.VisibilityOff(), positive = false))
        add(visualListItem(stringResource(R.string.screen_encryption_reset_bullet_3), CompoundIcons.User(), positive = false))
        if (showDevicesItem) {
            add(visualListItem(stringResource(R.string.screen_encryption_reset_bullet_4), CompoundIcons.Devices(), positive = false))
        }
    }.toImmutableList()
    VisualList(
        modifier = Modifier.fillMaxWidth(),
        items = items,
    )
}

private fun visualListItem(message: String, icon: ImageVector, positive: Boolean) = VisualListItemData(
    message = message,
    iconComposable = {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = icon,
            contentDescription = null,
            tint = if (positive) ElementTheme.colors.iconSuccessPrimary else ElementTheme.colors.iconSecondary,
        )
    },
)

@PreviewsDayNight
@Composable
internal fun ResetIdentityRootViewPreview(@PreviewParameter(ResetIdentityRootStatePreviewParam::class) state: ResetIdentityRootState) {
    ElementPreview {
        ResetIdentityRootView(
            state = state,
            onContinue = {},
            onBack = {},
        )
    }
}
