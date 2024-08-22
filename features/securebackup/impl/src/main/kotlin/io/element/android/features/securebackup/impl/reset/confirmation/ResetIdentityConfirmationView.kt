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

package io.element.android.features.securebackup.impl.reset.confirmation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.ui.strings.CommonStrings

private const val COLORED_PATTERN = "XXXXX"

@Composable
fun ResetIdentityConfirmationView(
    state: ResetIdentityConfirmationState,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val splitSubtitle = stringResource(R.string.screen_reset_identity_confirmation_subtitle, COLORED_PATTERN)
        .split(COLORED_PATTERN)
    val subtitle = buildAnnotatedString {
        if (splitSubtitle.size == 2) {
            append(splitSubtitle[0])
            withStyle(
                style = SpanStyle(
                    color = ElementTheme.colors.textLinkExternal,
                    fontWeight = FontWeight.SemiBold,
                )
            ) {
                append(state.host)
            }
            append(splitSubtitle[1])
        } else {
            // Translation issue? Forget about the colored part in this case.
            append(stringResource(R.string.screen_reset_identity_confirmation_subtitle, state.host))
        }
    }
    FlowStepPage(
        modifier = modifier,
        iconStyle = BigIcon.Style.Default(CompoundIcons.UserProfileSolid()),
        title = AnnotatedString(stringResource(R.string.screen_reset_identity_confirmation_title)),
        subTitle = subtitle,
        onBackClick = onBack,
        buttons = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(CommonStrings.action_continue),
                onClick = onContinue,
            )
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(CommonStrings.action_cancel),
                onClick = onBack,
            )
        }
    )
}

@PreviewsDayNight
@Composable
internal fun ResetIdentityConfirmationViewPreview() {
    ElementPreview {
        ResetIdentityConfirmationView(
            state = ResetIdentityConfirmationState("element.io"),
            onContinue = {},
            onBack = {}
        )
    }
}
