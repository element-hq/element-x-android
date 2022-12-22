@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.rageshake.bugreport

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.core.compose.LogCompositions
import io.element.android.x.designsystem.ElementXTheme
import io.element.android.x.designsystem.components.LabelledCheckbox
import io.element.android.x.designsystem.components.dialogs.ErrorDialog
import io.element.android.x.element.resources.R as ElementR

@Composable
fun BugReportScreen(
    viewModel: BugReportViewModel = mavericksViewModel(),
    onDone: () -> Unit = { },
) {
    val state: BugReportViewState by viewModel.collectAsState()
    val formState: BugReportFormState by viewModel.formState
    LogCompositions(tag = "Rageshake", msg = "Root")
    if (state.sending is Success) {
        onDone()
    }
    BugReportContent(
        state = state,
        formState = formState,
        onDescriptionChanged = viewModel::onSetDescription,
        onSetSendLog = viewModel::onSetSendLog,
        onSetSendCrashLog = viewModel::onSetSendCrashLog,
        onSetCanContact = viewModel::onSetCanContact,
        onSetSendScreenshot = viewModel::onSetSendScreenshot,
        onSubmit = viewModel::onSubmit,
        onFailureDialogClosed = viewModel::onFailureDialogClosed,
        onDone = onDone,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportContent(
    state: BugReportViewState,
    formState: BugReportFormState,
    modifier: Modifier = Modifier,
    onDescriptionChanged: (String) -> Unit = {},
    onSetSendLog: (Boolean) -> Unit = {},
    onSetSendCrashLog: (Boolean) -> Unit = {},
    onSetCanContact: (Boolean) -> Unit = {},
    onSetSendScreenshot: (Boolean) -> Unit = {},
    onSubmit: () -> Unit = {},
    onFailureDialogClosed: () -> Unit = { },
    onDone: () -> Unit = { },
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .verticalScroll(
                        state = scrollState,
                    )
                    .padding(horizontal = 16.dp),
            ) {
                val isError = state.sending is Fail
                val isFormEnabled = state.sending !is Loading
                // Title
                Text(
                    text = stringResource(id = ElementR.string.send_bug_report),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                )
                // Form
                Text(
                    text = stringResource(id = ElementR.string.send_bug_report_description),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    fontSize = 16.sp,
                )
                Column(
                    // modifier = Modifier.weight(1f),
                ) {
                    OutlinedTextField(
                        value = formState.description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        enabled = isFormEnabled,
                        label = {
                            Text(text = stringResource(id = ElementR.string.send_bug_report_placeholder))
                        },
                        supportingText = {
                            Text(text = stringResource(id = ElementR.string.send_bug_report_description_in_english))
                        },
                        onValueChange = onDescriptionChanged,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        // TODO Error text too short
                    )
                }
                LabelledCheckbox(
                    checked = state.sendLogs,
                    onCheckedChange = onSetSendLog,
                    enabled = isFormEnabled,
                    text = stringResource(id = ElementR.string.send_bug_report_include_logs)
                )
                if (state.hasCrashLogs) {
                    LabelledCheckbox(
                        checked = state.sendCrashLogs,
                        onCheckedChange = onSetSendCrashLog,
                        enabled = isFormEnabled,
                        text = stringResource(id = ElementR.string.send_bug_report_include_crash_logs)
                    )
                }
                LabelledCheckbox(
                    checked = state.canContact,
                    onCheckedChange = onSetCanContact,
                    enabled = isFormEnabled,
                    text = stringResource(id = ElementR.string.you_may_contact_me)
                )
                if (state.screenshotUri != null) {
                    LabelledCheckbox(
                        checked = state.sendScreenshot,
                        onCheckedChange = onSetSendScreenshot,
                        enabled = isFormEnabled,
                        text = stringResource(id = ElementR.string.send_bug_report_include_screenshot)
                    )
                    if (state.sendScreenshot) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            val context = LocalContext.current
                            val model = ImageRequest.Builder(context)
                                .data(state.screenshotUri)
                                .build()
                            AsyncImage(
                                modifier = Modifier.fillMaxWidth(fraction = 0.5f),
                                model = model,
                                contentDescription = null
                            )
                        }
                    }
                }
                // Submit
                Button(
                    onClick = onSubmit,
                    enabled = state.submitEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Text(text = stringResource(id = ElementR.string.action_send))
                }
            }
            when (state.sending) {
                Uninitialized -> Unit
                is Loading -> {
                    CircularProgressIndicator(
                        progress = state.sendingProgress,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Fail -> ErrorDialog(
                    content = state.sending.error.toString(),
                    onDismiss = onFailureDialogClosed,
                )
                is Success -> onDone()
            }
        }
    }
}

@Composable
@Preview
fun BugReportContentPreview() {
    ElementXTheme(darkTheme = false) {
        BugReportContent(
            state = BugReportViewState(),
            formState = BugReportFormState.Default
        )
    }
}
