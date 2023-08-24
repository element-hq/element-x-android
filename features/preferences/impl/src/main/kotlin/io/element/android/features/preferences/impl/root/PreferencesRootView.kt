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

package io.element.android.features.preferences.impl.root

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.logout.api.LogoutPreferenceView
import io.element.android.features.preferences.impl.user.UserPreferences
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.components.preferences.PreferenceView
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.LargeHeightPreview
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.SnackbarHost
import io.element.android.libraries.designsystem.utils.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserProvider
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun PreferencesRootView(
    state: PreferencesRootState,
    onBackPressed: () -> Unit,
    onVerifyClicked: () -> Unit,
    onManageAccountClicked: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenRageShake: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenDeveloperSettings: () -> Unit,
    onSuccessLogout: (String?) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)

    // Include pref from other modules
    PreferenceView(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.common_settings),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        UserPreferences(state.myUser)
        if (state.showCompleteVerification) {
            PreferenceText(
                title = stringResource(id = CommonStrings.action_complete_verification),
                icon = Icons.Outlined.VerifiedUser,
                onClick = onVerifyClicked,
            )
            HorizontalDivider()
        }
        if (state.accountManagementUrl != null) {
            PreferenceText(
                title = stringResource(id = CommonStrings.screen_settings_oidc_account),
                icon = Icons.Outlined.ManageAccounts,
                onClick = onManageAccountClicked,
            )
        }
        if (state.showAnalyticsSettings) {
            PreferenceText(
                title = stringResource(id = CommonStrings.common_analytics),
                icon = Icons.Outlined.InsertChart,
                onClick = onOpenAnalytics,
            )
        }
        PreferenceText(
            title = stringResource(id = CommonStrings.screen_notification_settings_title),
            icon = Icons.Outlined.Notifications,
            onClick = onOpenNotificationSettings,
        )
        PreferenceText(
            title = stringResource(id = CommonStrings.action_report_bug),
            icon = Icons.Outlined.BugReport,
            onClick = onOpenRageShake
        )
        PreferenceText(
            title = stringResource(id = CommonStrings.common_about),
            icon = Icons.Outlined.Help,
            onClick = onOpenAbout,
        )
        if (state.showDeveloperSettings) {
            DeveloperPreferencesView(onOpenDeveloperSettings)
        }
        HorizontalDivider()
        LogoutPreferenceView(
            state = state.logoutState,
            onSuccessLogout = onSuccessLogout,
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 24.dp),
            textAlign = TextAlign.Center,
            text = state.version,
            style = ElementTheme.typography.fontBodySmRegular,
            color = ElementTheme.materialColors.secondary,
        )
    }
}

@Composable
fun DeveloperPreferencesView(onOpenDeveloperSettings: () -> Unit) {
    PreferenceText(
        title = stringResource(id = CommonStrings.common_developer_options),
        icon = Icons.Outlined.DeveloperMode,
        onClick = onOpenDeveloperSettings
    )
}

@LargeHeightPreview
@Composable
internal fun PreferencesRootViewLightPreview(@PreviewParameter(MatrixUserProvider::class) matrixUser: MatrixUser) =
    ElementPreviewLight { ContentToPreview(matrixUser) }

@LargeHeightPreview
@Composable
internal fun PreferencesRootViewDarkPreview(@PreviewParameter(MatrixUserProvider::class) matrixUser: MatrixUser) =
    ElementPreviewDark { ContentToPreview(matrixUser) }

@Composable
private fun ContentToPreview(matrixUser: MatrixUser) {
    PreferencesRootView(
        state = aPreferencesRootState().copy(myUser = matrixUser),
        onBackPressed = {},
        onOpenAnalytics = {},
        onOpenRageShake = {},
        onOpenDeveloperSettings = {},
        onOpenAbout = {},
        onVerifyClicked = {},
        onSuccessLogout = {},
        onManageAccountClicked = {},
        onOpenNotificationSettings = {},
    )
}
