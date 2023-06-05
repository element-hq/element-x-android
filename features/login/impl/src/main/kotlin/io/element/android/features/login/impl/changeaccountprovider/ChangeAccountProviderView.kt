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

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package io.element.android.features.login.impl.changeaccountprovider

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.changeaccountprovider.item.ChangeAccountProviderItem
import io.element.android.features.login.impl.changeaccountprovider.item.ChangeAccountProviderItemView
import io.element.android.features.login.impl.changeserver.ChangeServerError
import io.element.android.features.login.impl.changeserver.SlidingSyncNotSupportedDialog
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.button.ButtonWithProgress
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag

/**
 * https://www.figma.com/file/o9p34zmiuEpZRyvZXJZAYL/FTUE?type=design&node-id=604-60817
 */
@Composable
fun ChangeAccountProviderView(
    state: ChangeAccountProviderState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    onOtherProviderClicked: () -> Unit = {},
    onChangeServerSuccess: () -> Unit = {},
) {
    val eventSink = state.eventSink
    val scrollState = rememberScrollState()
    val isLoading by remember(state.changeServerAction) {
        derivedStateOf {
            state.changeServerAction is Async.Loading
        }
    }
    val invalidHomeserverError = (state.changeServerAction as? Async.Failure)?.error as? ChangeServerError.InlineErrorMessage
    val slidingSyncNotSupportedError = (state.changeServerAction as? Async.Failure)?.error as? ChangeServerError.SlidingSyncAlert
    val focusManager = LocalFocusManager.current

    fun submit() {
        // Clear focus to prevent keyboard issues with textfields
        focusManager.clearFocus(force = true)

        eventSink(ChangeAccountProviderEvents.Submit)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onClick = onBackPressed) }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(
                        state = scrollState,
                    )
            ) {
                IconTitleSubtitleMolecule(
                    modifier = Modifier.padding(top = 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
                    iconImageVector = Icons.Filled.Home,
                    title = stringResource(id = R.string.screen_change_account_provider_title),
                    subTitle = stringResource(id = R.string.screen_change_account_provider_subtitle)
                )

                if (slidingSyncNotSupportedError != null) {
                    SlidingSyncNotSupportedDialog(onLearnMoreClicked = {
                        eventSink(ChangeAccountProviderEvents.ClearError)
                    }, onDismiss = {
                        eventSink(ChangeAccountProviderEvents.ClearError)
                    })
                }
                ChangeAccountProviderItemView(
                    item = ChangeAccountProviderItem(
                        title = "matrix.org",
                        subtitle = stringResource(id = R.string.screen_change_account_provider_matrix_org_subtitle),
                        isPublic = true,
                        isMatrix = true,
                    ),
                    onClick = {
                        TODO()
                    }
                )
                ChangeAccountProviderItemView(
                    item = ChangeAccountProviderItem(
                        title = stringResource(id = R.string.screen_change_account_provider_other),
                    ),
                    onClick = onOtherProviderClicked
                )

                Spacer(Modifier.height(32.dp))
                ButtonWithProgress(
                    text = stringResource(id = R.string.screen_change_server_submit),
                    showProgress = isLoading,
                    onClick = ::submit,
                    enabled = state.submitEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.changeServerContinue)
                )
                if (state.changeServerAction is Async.Success) {
                    onChangeServerSuccess()
                }
            }
        }
    }
}

@Preview
@Composable
fun ChangeAccountProviderViewLightPreview(@PreviewParameter(ChangeAccountProviderStateProvider::class) state: ChangeAccountProviderState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun ChangeAccountProviderViewDarkPreview(@PreviewParameter(ChangeAccountProviderStateProvider::class) state: ChangeAccountProviderState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: ChangeAccountProviderState) {
    ChangeAccountProviderView(
        state = state,
        onBackPressed = { }
    )
}
