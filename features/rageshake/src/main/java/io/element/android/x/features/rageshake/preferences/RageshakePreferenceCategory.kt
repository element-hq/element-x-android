package io.element.android.x.features.rageshake.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.designsystem.components.preferences.PreferenceCategory
import io.element.android.x.designsystem.components.preferences.PreferenceSlide
import io.element.android.x.designsystem.components.preferences.PreferenceSwitch
import io.element.android.x.designsystem.components.preferences.PreferenceText
import io.element.android.x.element.resources.R as ElementR
import io.element.android.x.features.rageshake.detection.RageshakeDetectionViewModel
import io.element.android.x.features.rageshake.detection.RageshakeDetectionViewState

@Composable
fun RageshakePreferences(
    onOpenRageShake: () -> Unit = {},
) {
    RageshakePreferencesContent(
        onOpenRageShake = onOpenRageShake,
    )
}

@Composable
fun RageshakePreferencesContent(
    modifier: Modifier = Modifier,
    viewModel: RageshakeDetectionViewModel = mavericksViewModel(),
    onOpenRageShake: () -> Unit = {},
) {
    val state: RageshakeDetectionViewState by viewModel.collectAsState()
    Column(modifier = modifier) {
        PreferenceCategory(title = stringResource(id = ElementR.string.send_bug_report)) {
            PreferenceText(
                title = stringResource(id = ElementR.string.send_bug_report),
                icon = Icons.Default.BugReport,
                onClick = onOpenRageShake
            )
        }
        PreferenceCategory(title = stringResource(id = ElementR.string.settings_rageshake)) {
            if (state.isSupported) {
                PreferenceSwitch(
                    title = stringResource(id = ElementR.string.send_bug_report_rage_shake),
                    isChecked = state.isEnabled,
                    onCheckedChange = viewModel::onEnableClicked
                )
                PreferenceSlide(
                    title = stringResource(id = ElementR.string.settings_rageshake_detection_threshold),
                    // summary = stringResource(id = ElementR.string.settings_rageshake_detection_threshold_summary),
                    value = state.sensitivity,
                    enabled = state.isEnabled,
                    steps = 3 /* 5 possible values - steps are in ]0, 1[ */,
                    onValueChange = viewModel::onSensitivityChange
                )
            } else {
                PreferenceText(title = "Rageshaking is not supported by your device")
            }
        }
    }
}

@Composable
@Preview
fun RageshakePreferencePreview() {
    RageshakePreferences()
}
