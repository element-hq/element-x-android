/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.analytics.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.appconfig.AnalyticsConfig
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.analytics.api.AnalyticsOptInEvents
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.organisms.InfoListItem
import io.element.android.libraries.designsystem.atomic.organisms.InfoListOrganism
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.background.OnboardingBackground
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.ClickableLinkText
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.buildAnnotatedStringWithStyledPart
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AnalyticsOptInView(
    state: AnalyticsOptInState,
    onClickTerms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink

    fun onAcceptTerms() {
        eventSink(AnalyticsOptInEvents.EnableAnalytics(true))
    }

    fun onDeclineTerms() {
        eventSink(AnalyticsOptInEvents.EnableAnalytics(false))
    }

    BackHandler(onBack = ::onDeclineTerms)
    HeaderFooterPage(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        background = { OnboardingBackground() },
        header = { AnalyticsOptInHeader(state, onClickTerms) },
        content = { AnalyticsOptInContent() },
        footer = {
            AnalyticsOptInFooter(
                onAcceptTerms = ::onAcceptTerms,
                onDeclineTerms = ::onDeclineTerms,
            )
        }
    )
}

private const val LINK_TAG = "link"

@Composable
private fun AnalyticsOptInHeader(
    state: AnalyticsOptInState,
    onClickTerms: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconTitleSubtitleMolecule(
            modifier = Modifier.padding(top = 60.dp, bottom = 28.dp),
            title = stringResource(id = R.string.screen_analytics_prompt_title, state.applicationName),
            subTitle = stringResource(id = R.string.screen_analytics_prompt_help_us_improve),
            iconStyle = BigIcon.Style.Default(CompoundIcons.Chart())
        )
        if (state.hasPolicyLink) {
            val text = buildAnnotatedStringWithStyledPart(
                R.string.screen_analytics_prompt_read_terms,
                R.string.screen_analytics_prompt_read_terms_content_link,
                color = Color.Unspecified,
                underline = false,
                bold = true,
                tagAndLink = LINK_TAG to AnalyticsConfig.POLICY_LINK,
            )
            ClickableLinkText(
                annotatedString = text,
                onClick = { onClickTerms() },
                modifier = Modifier
                    .padding(8.dp),
                style = ElementTheme.typography.fontBodyMdRegular
                    .copy(
                        color = ElementTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
            )
        }
    }
}

@Composable
private fun AnalyticsOptInContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = BiasAlignment(
            horizontalBias = 0f,
            verticalBias = -0.4f
        )
    ) {
        InfoListOrganism(
            items = persistentListOf(
                InfoListItem(
                    message = stringResource(id = R.string.screen_analytics_prompt_data_usage),
                    iconVector = CompoundIcons.CheckCircle(),
                ),
                InfoListItem(
                    message = stringResource(id = R.string.screen_analytics_prompt_third_party_sharing),
                    iconVector = CompoundIcons.CheckCircle(),
                ),
                InfoListItem(
                    message = stringResource(id = R.string.screen_analytics_prompt_settings),
                    iconVector = CompoundIcons.CheckCircle(),
                ),
            ),
            textStyle = ElementTheme.typography.fontBodyLgMedium,
            iconTint = ElementTheme.colors.iconSuccessPrimary,
        )
    }
}

@Composable
private fun AnalyticsOptInFooter(
    onAcceptTerms: () -> Unit,
    onDeclineTerms: () -> Unit,
) {
    ButtonColumnMolecule {
        Button(
            text = stringResource(id = CommonStrings.action_ok),
            onClick = onAcceptTerms,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(
            text = stringResource(id = CommonStrings.action_not_now),
            size = ButtonSize.Medium,
            onClick = onDeclineTerms,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun AnalyticsOptInViewPreview(@PreviewParameter(AnalyticsOptInStateProvider::class) state: AnalyticsOptInState) = ElementPreview {
    AnalyticsOptInView(
        state = state,
        onClickTerms = {},
    )
}
