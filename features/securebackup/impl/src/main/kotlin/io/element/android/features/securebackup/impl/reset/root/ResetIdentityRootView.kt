/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.securebackup.impl.reset.root

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.designsystem.atomic.organisms.InfoListItem
import io.element.android.libraries.designsystem.atomic.organisms.InfoListOrganism
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ResetIdentityRootView(
    state: ResetIdentityRootState,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowStepPage(
        modifier = modifier,
        iconStyle = BigIcon.Style.AlertSolid,
        title = stringResource(R.string.screen_encryption_reset_title),
        isScrollable = true,
        content = { Content() },
        buttons = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = CommonStrings.action_continue),
                onClick = { state.eventSink(ResetIdentityRootEvent.Continue) },
                destructive = true,
            )
        },
        onBackClick = onBack,
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
private fun Content() {
    Column(
        modifier = Modifier.padding(top = 8.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        InfoListOrganism(
            modifier = Modifier.fillMaxWidth(),
            items = persistentListOf(
                InfoListItem(
                    message = stringResource(R.string.screen_encryption_reset_bullet_1),
                    iconComposable = {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = CompoundIcons.Check(),
                            contentDescription = null,
                            tint = ElementTheme.colors.iconSuccessPrimary,
                        )
                    },
                ),
                InfoListItem(
                    message = stringResource(R.string.screen_encryption_reset_bullet_2),
                    iconComposable = {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = CompoundIcons.Close(),
                            contentDescription = null,
                            tint = ElementTheme.colors.iconCriticalPrimary,
                        )
                    },
                ),
                InfoListItem(
                    message = stringResource(R.string.screen_encryption_reset_bullet_3),
                    iconComposable = {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = CompoundIcons.Close(),
                            contentDescription = null,
                            tint = ElementTheme.colors.iconCriticalPrimary,
                        )
                    },
                ),
            ),
            backgroundColor = ElementTheme.colors.bgActionSecondaryHovered,
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.screen_encryption_reset_footer),
            style = ElementTheme.typography.fontBodyMdMedium,
            color = ElementTheme.colors.textActionPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun ResetIdentityRootViewPreview(@PreviewParameter(ResetIdentityRootStateProvider::class) state: ResetIdentityRootState) {
    ElementPreview {
        ResetIdentityRootView(
            state = state,
            onContinue = {},
            onBack = {},
        )
    }
}
