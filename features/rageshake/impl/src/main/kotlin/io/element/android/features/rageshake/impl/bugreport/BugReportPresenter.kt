/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.bugreport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import dev.zacsweers.metro.Inject
import io.element.android.features.rageshake.api.reporter.BugReporter
import io.element.android.features.rageshake.api.reporter.BugReporterListener
import io.element.android.features.rageshake.impl.crash.CrashDataStore
import io.element.android.features.rageshake.impl.screenshot.ScreenshotHolder
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.annotations.AppCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class BugReportPresenter(
    private val bugReporter: BugReporter,
    private val crashDataStore: CrashDataStore,
    private val screenshotHolder: ScreenshotHolder,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
) : Presenter<BugReportState> {
    private class BugReporterUploadListener(
        private val sendingProgress: MutableFloatState,
        private val sendingAction: MutableState<AsyncAction<Unit>>
    ) : BugReporterListener {
        override fun onUploadCancelled() {
            sendingProgress.floatValue = 0f
            sendingAction.value = AsyncAction.Uninitialized
        }

        override fun onUploadFailed(reason: String?) {
            sendingProgress.floatValue = 0f
            sendingAction.value = AsyncAction.Failure(Exception(reason))
        }

        override fun onProgress(progress: Int) {
            sendingProgress.floatValue = progress.toFloat() / 100
            sendingAction.value = AsyncAction.Loading
        }

        override fun onUploadSucceed() {
            sendingProgress.floatValue = 0f
            sendingAction.value = AsyncAction.Success(Unit)
        }
    }

    @Composable
    override fun present(): BugReportState {
        val screenshotUri = rememberSaveable {
            mutableStateOf(
                screenshotHolder.getFileUri()
            )
        }
        val crashInfo: String by remember {
            crashDataStore.crashInfo()
        }.collectAsState(initial = "")

        val sendingProgress = remember {
            mutableFloatStateOf(0f)
        }
        val sendingAction: MutableState<AsyncAction<Unit>> = remember {
            mutableStateOf(AsyncAction.Uninitialized)
        }
        val formState: MutableState<BugReportFormState> = rememberSaveable {
            mutableStateOf(BugReportFormState.Default)
        }
        val uploadListener = BugReporterUploadListener(sendingProgress, sendingAction)

        fun handleEvent(event: BugReportEvents) {
            when (event) {
                BugReportEvents.SendBugReport -> {
                    if (formState.value.description.length < 10) {
                        sendingAction.value = AsyncAction.Failure(BugReportFormError.DescriptionTooShort)
                    } else {
                        sendingAction.value = AsyncAction.Loading
                        appCoroutineScope.sendBugReport(formState.value, crashInfo.isNotEmpty(), uploadListener)
                    }
                }
                BugReportEvents.ResetAll -> appCoroutineScope.resetAll()
                is BugReportEvents.SetDescription -> updateFormState(formState) {
                    copy(description = event.description)
                }
                is BugReportEvents.SetCanContact -> updateFormState(formState) {
                    copy(canContact = event.canContact)
                }
                is BugReportEvents.SetSendLog -> updateFormState(formState) {
                    copy(sendLogs = event.sendLog)
                }
                is BugReportEvents.SetSendScreenshot -> updateFormState(formState) {
                    copy(sendScreenshot = event.sendScreenshot)
                }
                is BugReportEvents.SetSendPushRules -> updateFormState(formState) {
                    copy(sendPushRules = event.sendPushRules)
                }
                BugReportEvents.ClearError -> {
                    sendingProgress.floatValue = 0f
                    sendingAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return BugReportState(
            hasCrashLogs = crashInfo.isNotEmpty(),
            sendingProgress = sendingProgress.floatValue,
            sending = sendingAction.value,
            formState = formState.value,
            screenshotUri = screenshotUri.value,
            eventSink = ::handleEvent,
        )
    }

    private fun updateFormState(formState: MutableState<BugReportFormState>, operation: BugReportFormState.() -> BugReportFormState) {
        formState.value = operation(formState.value)
    }

    private fun CoroutineScope.sendBugReport(
        formState: BugReportFormState,
        hasCrashLogs: Boolean,
        listener: BugReporterListener,
    ) = launch {
        bugReporter.sendBugReport(
            withDevicesLogs = formState.sendLogs,
            withCrashLogs = hasCrashLogs && formState.sendLogs,
            withScreenshot = formState.sendScreenshot,
            problemDescription = formState.description,
            canContact = formState.canContact,
            sendPushRules = formState.sendPushRules,
            listener = listener
        )
    }

    private fun CoroutineScope.resetAll() = launch {
        screenshotHolder.reset()
        crashDataStore.reset()
    }
}
