package io.element.android.x.features.logout

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.designsystem.ElementXTheme
import io.element.android.x.designsystem.components.ProgressDialog
import io.element.android.x.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.x.designsystem.components.preferences.PreferenceCategory
import io.element.android.x.designsystem.components.preferences.PreferenceText
import io.element.android.x.element.resources.R as ElementR

@Composable
fun LogoutPreference(
    viewModel: LogoutViewModel = mavericksViewModel(),
    onSuccessLogout: () -> Unit = { },
) {
    val state: LogoutViewState by viewModel.collectAsState()
    if (state.logoutAction is Success) {
        onSuccessLogout()
        return
    }

    val openDialog = remember { mutableStateOf(false) }

    LogoutPreferenceContent(
        onClick = {
            openDialog.value = true
        }
    )

    // Log out confirmation dialog
    if (openDialog.value) {
        ConfirmationDialog(
            title = stringResource(id = ElementR.string.action_sign_out),
            content = stringResource(id = ElementR.string.action_sign_out_confirmation_simple),
            submitText = stringResource(id = ElementR.string.action_sign_out),
            onCancelClicked = {
                openDialog.value = false
            },
            onSubmitClicked = {
                openDialog.value = false
                viewModel.logout()
            },
            onDismiss = {
                openDialog.value = false
            }
        )
    }

    if (state.logoutAction is Loading) {
        ProgressDialog(text = "Login out...")
    }
}

@Composable
fun LogoutPreferenceContent(
    onClick: () -> Unit = {},
) {
    PreferenceCategory(title = stringResource(id = ElementR.string.settings_general_title)) {
        PreferenceText(
            title = stringResource(id = ElementR.string.action_sign_out),
            icon = Icons.Default.Logout,
            onClick = onClick
        )
    }
}

@Composable
@Preview
fun LogoutContentPreview() {
    ElementXTheme(darkTheme = false) {
        LogoutPreference()
    }
}
