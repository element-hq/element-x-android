/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.element.android.features.securebackup.impl.setup

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.api.CustomRecoveryPassphraseStrength
import io.element.android.features.enterprise.api.CustomRecoveryPassphraseStrengthResult
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.features.wellknown.test.FakeSessionWellknownRetriever
import io.element.android.features.wellknown.test.aCustomRecoveryPassphraseRequirements
import io.element.android.features.wellknown.test.anElementWellKnown
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.test.A_RECOVERY_KEY
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.wellknown.api.SessionWellknownRetriever
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@Suppress("LargeClass")
class SecureBackupSetupPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createSecureBackupSetupPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val preFetch = awaitItem()
            assertThat(preFetch.wellknownLoaded).isFalse()
            assertThat(preFetch.setupState).isEqualTo(SetupState.Init)

            val loaded = awaitItem()
            assertThat(loaded.wellknownLoaded).isTrue()
            assertThat(loaded.isChangeRecoveryKeyUserStory).isFalse()
            assertThat(loaded.setupState).isEqualTo(SetupState.Init)
            assertThat(loaded.showSaveConfirmationDialog).isFalse()
            assertThat(loaded.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Setup,
                    formattedRecoveryKey = null,
                    displayTextFieldContents = true,
                    inProgress = false,
                )
            )
        }
    }

    @Test
    fun `present - create recovery key and save it`() = runTest {
        val encryptionService = FakeEncryptionService(
            enableRecoveryLambda = { _, _ -> Result.success(A_RECOVERY_KEY) },
        )
        val presenter = createSecureBackupSetupPresenter(
            encryptionService = encryptionService
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val initialState = awaitItem() // post-fetch, wellknownLoaded=true
            initialState.eventSink.invoke(SecureBackupSetupEvents.CreateRecoveryKey)
            val creatingState = awaitItem()
            assertThat(creatingState.setupState).isEqualTo(SetupState.Creating)
            assertThat(creatingState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Setup,
                    formattedRecoveryKey = null,
                    displayTextFieldContents = true,
                    inProgress = true,
                )
            )
            val createdState = awaitItem()
            assertThat(createdState.setupState).isEqualTo(SetupState.Created(A_RECOVERY_KEY))
            assertThat(createdState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Setup,
                    formattedRecoveryKey = A_RECOVERY_KEY,
                    displayTextFieldContents = true,
                    inProgress = false,
                )
            )
            createdState.eventSink.invoke(SecureBackupSetupEvents.RecoveryKeyHasBeenSaved)
            val createdAndSaveState = awaitItem()
            assertThat(createdAndSaveState.setupState).isInstanceOf(SetupState.CreatedAndSaved::class.java)
            createdAndSaveState.eventSink.invoke(SecureBackupSetupEvents.Done)
            val doneState = awaitItem()
            assertThat(doneState.showSaveConfirmationDialog).isTrue()
            doneState.eventSink.invoke(SecureBackupSetupEvents.DismissDialog)
            val doneStateCancelled = awaitItem()
            assertThat(doneStateCancelled.showSaveConfirmationDialog).isFalse()
        }
    }

    @Test
    fun `present - initial state change key`() = runTest {
        val presenter = createSecureBackupSetupPresenter(
            isChangeRecoveryKeyUserStory = true,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val initialState = awaitItem()
            assertThat(initialState.wellknownLoaded).isTrue()
            assertThat(initialState.isChangeRecoveryKeyUserStory).isTrue()
            assertThat(initialState.setupState).isEqualTo(SetupState.Init)
            assertThat(initialState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Change,
                    formattedRecoveryKey = null,
                    displayTextFieldContents = true,
                    inProgress = false,
                )
            )
        }
    }

    @Test
    fun `present - handle errors`() = runTest {
        val encryptionService = FakeEncryptionService(
            enableRecoveryLambda = { _, _ -> Result.failure(IllegalStateException("Test error")) }
        )
        val presenter = createSecureBackupSetupPresenter(
            isChangeRecoveryKeyUserStory = false,
            encryptionService = encryptionService
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val initialState = awaitItem()
            assertThat(initialState.isChangeRecoveryKeyUserStory).isFalse()
            assertThat(initialState.setupState).isEqualTo(SetupState.Init)

            initialState.eventSink(SecureBackupSetupEvents.CreateRecoveryKey)
            val creatingState = awaitItem()
            assertThat(creatingState.setupState).isEqualTo(SetupState.Creating)
            val failedState = awaitItem()
            assertThat(failedState.setupState).isInstanceOf(SetupState.Error::class.java)
            failedState.eventSink(SecureBackupSetupEvents.DismissDialog)

            val finalState = awaitItem()
            assertThat(finalState.setupState).isEqualTo(SetupState.Init)
        }
    }

    @Test
    fun `present - change recovery key and save it`() = runTest {
        val encryptionService = FakeEncryptionService()
        val presenter = createSecureBackupSetupPresenter(
            isChangeRecoveryKeyUserStory = true,
            encryptionService = encryptionService
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val initialState = awaitItem()
            initialState.eventSink.invoke(SecureBackupSetupEvents.CreateRecoveryKey)
            val creatingState = awaitItem()
            assertThat(creatingState.setupState).isEqualTo(SetupState.Creating)
            assertThat(creatingState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Change,
                    formattedRecoveryKey = null,
                    displayTextFieldContents = true,
                    inProgress = true,
                )
            )
            val createdState = awaitItem()
            assertThat(createdState.setupState).isEqualTo(SetupState.Created(FakeEncryptionService.FAKE_RECOVERY_KEY))
            assertThat(createdState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Change,
                    formattedRecoveryKey = FakeEncryptionService.FAKE_RECOVERY_KEY,
                    displayTextFieldContents = true,
                    inProgress = false,
                )
            )
            createdState.eventSink.invoke(SecureBackupSetupEvents.RecoveryKeyHasBeenSaved)
            val createdAndSaveState = awaitItem()
            assertThat(createdAndSaveState.setupState).isInstanceOf(SetupState.CreatedAndSaved::class.java)
            createdAndSaveState.eventSink.invoke(SecureBackupSetupEvents.Done)
            val doneState = awaitItem()
            assertThat(doneState.showSaveConfirmationDialog).isTrue()
            doneState.eventSink.invoke(SecureBackupSetupEvents.DismissDialog)
            val doneStateCancelled = awaitItem()
            assertThat(doneStateCancelled.showSaveConfirmationDialog).isFalse()
        }
    }

    @Test
    fun `present - wellknownLoaded flips false to true exactly once after fetch resolves`() = runTest {
        val presenter = createSecureBackupSetupPresenter(
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val preFetch = awaitItem()
            assertThat(preFetch.wellknownLoaded).isFalse()
            assertThat(preFetch.customRecoveryPassphraseRequirements).isNull()

            val loaded = awaitItem()
            assertThat(loaded.wellknownLoaded).isTrue()
            assertThat(loaded.customRecoveryPassphraseRequirements).isNotNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - custom spec absent leaves customRecoveryPassphraseRequirements null and uses generated-key path`() = runTest {
        val enableRecoveryLambda = lambdaRecorder<Boolean, String?, Result<String>> { _, _ -> Result.success(A_RECOVERY_KEY) }
        val encryptionService = FakeEncryptionService(enableRecoveryLambda = enableRecoveryLambda)
        val presenter = createSecureBackupSetupPresenter(
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = null))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val preFetch = awaitItem()
            assertThat(preFetch.wellknownLoaded).isFalse()
            val loaded = awaitItem()
            assertThat(loaded.wellknownLoaded).isTrue()
            assertThat(loaded.customRecoveryPassphraseRequirements).isNull()
            assertThat(loaded.canSubmitCustomPassphrase).isFalse()
            loaded.eventSink.invoke(SecureBackupSetupEvents.CreateRecoveryKey)
            awaitItem() // creating
            advanceUntilIdle()
            enableRecoveryLambda.assertions().isCalledOnce()
                .with(value(false), value(null))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - custom spec present surfaces validation errors and blocks continue`() = runTest {
        val enableRecoveryLambda = lambdaRecorder<Boolean, String?, Result<String>> { _, _ -> Result.success(A_RECOVERY_KEY) }
        val encryptionService = FakeEncryptionService(enableRecoveryLambda = enableRecoveryLambda)
        val presenter = createSecureBackupSetupPresenter(
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // initial, pre-fetch
            val withSpec = awaitItem()
            assertThat(withSpec.customRecoveryPassphraseRequirements).isNotNull()
            assertThat(withSpec.customEntryStep).isEqualTo(CustomEntryStep.Entry)
            assertThat(withSpec.canContinueFromEntry).isFalse()
            assertThat(withSpec.canSubmitCustomPassphrase).isFalse()

            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase("abc"))
            val afterShortInput = awaitItem()
            assertThat(afterShortInput.customPassphrase).isEqualTo("abc")
            assertThat(afterShortInput.customPassphraseMeetsMinLength).isFalse()
            assertThat(afterShortInput.canContinueFromEntry).isFalse()

            // Continue is a no-op when entry is invalid: step stays Entry.
            afterShortInput.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
            expectNoEvents()
            enableRecoveryLambda.assertions().isNeverCalled()
        }
    }

    @Test
    fun `present - custom spec strength is null while empty and delegates to the enterprise estimator once user types`() = runTest {
        // The estimation algorithm lives in the enterprise module; here we only verify the presenter
        // suppresses the indicator while the field is empty and otherwise forwards the service result.
        val presenter = createSecureBackupSetupPresenter(
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
            enterpriseService = FakeEnterpriseService(
                isCustomRecoveryPassphraseEnabledResult = true,
                estimateCustomRecoveryPassphraseStrengthResult = { passphrase ->
                    if (passphrase.length > 5) {
                        CustomRecoveryPassphraseStrengthResult(CustomRecoveryPassphraseStrength.Strong, score = 0.9f)
                    } else {
                        CustomRecoveryPassphraseStrengthResult(CustomRecoveryPassphraseStrength.Weak, score = 0.1f)
                    }
                },
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val withSpec = awaitItem()
            // Field is empty → indicator suppressed (the estimator is never consulted).
            assertThat(withSpec.customPassphraseStrength).isNull()

            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase("abc"))
            val afterShort = awaitItem()
            assertThat(afterShort.customPassphraseStrength?.strength)
                .isEqualTo(CustomRecoveryPassphraseStrength.Weak)

            afterShort.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase("Abcdefg1!@#xyz"))
            val afterStrong = awaitItem()
            assertThat(afterStrong.customPassphraseStrength?.strength)
                .isEqualTo(CustomRecoveryPassphraseStrength.Strong)

            // Clearing the field brings the indicator back to null.
            afterStrong.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(""))
            val cleared = awaitItem()
            assertThat(cleared.customPassphraseStrength).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - custom spec ContinueCustomPassphrase advances Entry to Confirm when valid`() = runTest {
        val presenter = createSecureBackupSetupPresenter(
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // initial
            val withSpec = awaitItem()
            assertThat(withSpec.customEntryStep).isEqualTo(CustomEntryStep.Entry)

            val valid = "Ab12!@cd"
            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(valid))
            val typed = awaitItem()
            assertThat(typed.canContinueFromEntry).isTrue()

            typed.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
            val advanced = awaitItem()
            assertThat(advanced.customEntryStep).isEqualTo(CustomEntryStep.Confirm)
            assertThat(advanced.customPassphrase).isEqualTo(valid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - custom spec BackToCustomEntry returns to Entry preserving both fields`() = runTest {
        val presenter = createSecureBackupSetupPresenter(
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // initial
            val withSpec = awaitItem()

            val valid = "Ab12!@cd"
            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(valid))
            awaitItem()
            withSpec.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
            val onConfirm = awaitItem()
            assertThat(onConfirm.customEntryStep).isEqualTo(CustomEntryStep.Confirm)
            onConfirm.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphraseConfirm(valid))
            awaitItem()

            onConfirm.eventSink.invoke(SecureBackupSetupEvents.BackToCustomEntry)
            val backOnEntry = awaitItem()
            assertThat(backOnEntry.customEntryStep).isEqualTo(CustomEntryStep.Entry)
            assertThat(backOnEntry.customPassphrase).isEqualTo(valid)
            assertThat(backOnEntry.customPassphraseConfirm).isEqualTo(valid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - custom spec SubmitCustomPassphrase from Entry step is a no-op`() = runTest {
        val enableRecoveryLambda = lambdaRecorder<Boolean, String?, Result<String>> { _, _ -> Result.success(A_RECOVERY_KEY) }
        val encryptionService = FakeEncryptionService(enableRecoveryLambda = enableRecoveryLambda)
        val presenter = createSecureBackupSetupPresenter(
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // initial
            val withSpec = awaitItem()

            val valid = "Ab12!@cd"
            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(valid))
            val typed = awaitItem()
            // Both fields contain matching content via a stale path: simulate by also setting confirm
            // while still on the Entry step (e.g., previously typed). Submit must still be gated.
            typed.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphraseConfirm(valid))
            val bothTyped = awaitItem()
            assertThat(bothTyped.customEntryStep).isEqualTo(CustomEntryStep.Entry)
            assertThat(bothTyped.canSubmitCustomPassphrase).isFalse()

            bothTyped.eventSink.invoke(SecureBackupSetupEvents.SubmitCustomPassphrase)
            expectNoEvents()
            enableRecoveryLambda.assertions().isNeverCalled()
        }
    }

    @Test
    fun `present - custom spec present submit with valid input forwards passphrase to SDK`() = runTest {
        val enableRecoveryLambda = lambdaRecorder<Boolean, String?, Result<String>> { _, _ -> Result.success(A_RECOVERY_KEY) }
        val encryptionService = FakeEncryptionService(enableRecoveryLambda = enableRecoveryLambda)
        val presenter = createSecureBackupSetupPresenter(
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // initial
            val withSpec = awaitItem()

            val valid = "Ab12!@cd"
            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(valid))
            val afterKey = awaitItem()
            afterKey.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
            val onConfirm = awaitItem()
            assertThat(onConfirm.customEntryStep).isEqualTo(CustomEntryStep.Confirm)
            onConfirm.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphraseConfirm(valid))
            val ready = awaitItem()
            assertThat(ready.canSubmitCustomPassphrase).isTrue()

            ready.eventSink.invoke(SecureBackupSetupEvents.SubmitCustomPassphrase)
            awaitItem() // creating
            advanceUntilIdle()
            enableRecoveryLambda.assertions().isCalledOnce()
                .with(value(false), value(valid))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - custom spec error retains typed passphrase and Confirm step across DismissDialog`() = runTest {
        val encryptionService = FakeEncryptionService(
            enableRecoveryLambda = { _, _ -> Result.failure(IllegalStateException("boom")) },
        )
        val presenter = createSecureBackupSetupPresenter(
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val withSpec = awaitItem()

            val valid = "Ab12!@cd"
            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(valid))
            awaitItem()
            withSpec.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
            val onConfirm = awaitItem()
            onConfirm.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphraseConfirm(valid))
            val ready = awaitItem()
            assertThat(ready.canSubmitCustomPassphrase).isTrue()

            ready.eventSink.invoke(SecureBackupSetupEvents.SubmitCustomPassphrase)
            awaitItem() // creating
            val errored = awaitItem()
            assertThat(errored.setupState).isInstanceOf(SetupState.Error::class.java)

            errored.eventSink.invoke(SecureBackupSetupEvents.DismissDialog)
            val afterDismiss = awaitItem()
            assertThat(afterDismiss.setupState).isEqualTo(SetupState.Init)
            assertThat(afterDismiss.customPassphrase).isEqualTo(valid)
            assertThat(afterDismiss.customPassphraseConfirm).isEqualTo(valid)
            assertThat(afterDismiss.customEntryStep).isEqualTo(CustomEntryStep.Confirm)
            assertThat(afterDismiss.canSubmitCustomPassphrase).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - custom spec submit success auto-advances to CreatedAndSaved without manual save`() = runTest {
        val encryptionService = FakeEncryptionService(
            enableRecoveryLambda = { _, _ -> Result.success(A_RECOVERY_KEY) },
        )
        val presenter = createSecureBackupSetupPresenter(
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val withSpec = awaitItem()

            val valid = "Ab12!@cd"
            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(valid))
            awaitItem()
            withSpec.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
            val onConfirm = awaitItem()
            onConfirm.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphraseConfirm(valid))
            val ready = awaitItem()
            assertThat(ready.canSubmitCustomPassphrase).isTrue()

            ready.eventSink.invoke(SecureBackupSetupEvents.SubmitCustomPassphrase)
            awaitItem() // creating
            advanceUntilIdle()

            // The presenter's auto-skip LaunchedEffect should have advanced state machine past
            // Created without requiring a manual RecoveryKeyHasBeenSaved event. The terminal
            // state observed is CreatedAndSaved.
            val finalState = expectMostRecentItem()
            assertThat(finalState.setupState).isInstanceOf(SetupState.CreatedAndSaved::class.java)
            // The SDK-derived base58 key must not leak through the recoveryKeyViewState in
            // the custom-passphrase flow.
            assertThat(finalState.recoveryKeyViewState.formattedRecoveryKey).isNull()
            // The state machine's KeyCreatedAndSaved variant carries a key too — confirm
            // the presenter scrubbed it before dispatching SdkHasCreatedKey, so observers
            // of setupState don't see the SDK-generated base58 key either.
            assertThat(finalState.setupState).isEqualTo(SetupState.CreatedAndSaved(formattedRecoveryKey = ""))
            assertThat(finalState.setupState.recoveryKey()).isEqualTo("")
            // Typed passphrase is cleared once the SDK call returns successfully.
            assertThat(finalState.customPassphrase).isEmpty()
            assertThat(finalState.customPassphraseConfirm).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - enterprise feature disabled suppresses spec and forces auto-gen path`() = runTest {
        val enableRecoveryLambda = lambdaRecorder<Boolean, String?, Result<String>> { _, _ -> Result.success(A_RECOVERY_KEY) }
        val encryptionService = FakeEncryptionService(enableRecoveryLambda = enableRecoveryLambda)
        val presenter = createSecureBackupSetupPresenter(
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
            enterpriseService = FakeEnterpriseService(isCustomRecoveryPassphraseEnabledResult = false),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val loaded = awaitItem()
            assertThat(loaded.wellknownLoaded).isTrue()
            // Even though the homeserver advertised a spec, the enterprise gate suppresses it.
            assertThat(loaded.customRecoveryPassphraseRequirements).isNull()
            assertThat(loaded.canSubmitCustomPassphrase).isFalse()
            loaded.eventSink.invoke(SecureBackupSetupEvents.CreateRecoveryKey)
            awaitItem() // creating
            advanceUntilIdle()
            enableRecoveryLambda.assertions().isCalledOnce()
                .with(value(false), value(null))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - well-known fetch failure falls back to generated-key path`() = runTest {
        val presenter = createSecureBackupSetupPresenter(
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Error(IllegalStateException("boom"))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val preFetch = awaitItem()
            assertThat(preFetch.wellknownLoaded).isFalse()
            val loaded = awaitItem()
            assertThat(loaded.wellknownLoaded).isTrue()
            assertThat(loaded.customRecoveryPassphraseRequirements).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - change + custom spec absent uses resetRecoveryKey path`() = runTest {
        val enableRecoveryLambda = lambdaRecorder<Boolean, String?, Result<String>> { _, _ -> Result.success(A_RECOVERY_KEY) }
        val encryptionService = FakeEncryptionService(enableRecoveryLambda = enableRecoveryLambda)
        val presenter = createSecureBackupSetupPresenter(
            isChangeRecoveryKeyUserStory = true,
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = null))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val loaded = awaitItem()
            assertThat(loaded.customRecoveryPassphraseRequirements).isNull()
            loaded.eventSink.invoke(SecureBackupSetupEvents.CreateRecoveryKey)
            awaitItem() // creating
            val createdState = awaitItem()
            assertThat(createdState.setupState).isEqualTo(SetupState.Created(FakeEncryptionService.FAKE_RECOVERY_KEY))
            enableRecoveryLambda.assertions().isNeverCalled()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - change + custom spec present surfaces validation errors and blocks continue`() = runTest {
        val enableRecoveryLambda = lambdaRecorder<Boolean, String?, Result<String>> { _, _ -> Result.success(A_RECOVERY_KEY) }
        val encryptionService = FakeEncryptionService(enableRecoveryLambda = enableRecoveryLambda)
        val presenter = createSecureBackupSetupPresenter(
            isChangeRecoveryKeyUserStory = true,
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // initial, pre-fetch
            val withSpec = awaitItem()
            assertThat(withSpec.customRecoveryPassphraseRequirements).isNotNull()
            assertThat(withSpec.canContinueFromEntry).isFalse()

            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase("abc"))
            val afterShortInput = awaitItem()
            assertThat(afterShortInput.customPassphrase).isEqualTo("abc")
            assertThat(afterShortInput.customPassphraseMeetsMinLength).isFalse()
            assertThat(afterShortInput.canContinueFromEntry).isFalse()

            afterShortInput.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
            expectNoEvents()
            enableRecoveryLambda.assertions().isNeverCalled()
        }
    }

    @Test
    fun `present - change + custom spec present submit with valid input forwards passphrase to enableRecovery`() = runTest {
        val enableRecoveryLambda = lambdaRecorder<Boolean, String?, Result<String>> { _, _ -> Result.success(A_RECOVERY_KEY) }
        val encryptionService = FakeEncryptionService(enableRecoveryLambda = enableRecoveryLambda)
        val presenter = createSecureBackupSetupPresenter(
            isChangeRecoveryKeyUserStory = true,
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // initial
            val withSpec = awaitItem()

            val valid = "Ab12!@cd"
            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(valid))
            val afterKey = awaitItem()
            afterKey.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
            val onConfirm = awaitItem()
            onConfirm.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphraseConfirm(valid))
            val ready = awaitItem()
            assertThat(ready.canSubmitCustomPassphrase).isTrue()

            ready.eventSink.invoke(SecureBackupSetupEvents.SubmitCustomPassphrase)
            awaitItem() // creating
            advanceUntilIdle()
            enableRecoveryLambda.assertions().isCalledOnce()
                .with(value(false), value(valid))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - change + well-known fetch failure falls back to resetRecoveryKey path`() = runTest {
        val enableRecoveryLambda = lambdaRecorder<Boolean, String?, Result<String>> { _, _ -> Result.success(A_RECOVERY_KEY) }
        val encryptionService = FakeEncryptionService(enableRecoveryLambda = enableRecoveryLambda)
        val presenter = createSecureBackupSetupPresenter(
            isChangeRecoveryKeyUserStory = true,
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Error(IllegalStateException("boom"))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val loaded = awaitItem()
            assertThat(loaded.customRecoveryPassphraseRequirements).isNull()
            loaded.eventSink.invoke(SecureBackupSetupEvents.CreateRecoveryKey)
            awaitItem() // creating
            val createdState = awaitItem()
            assertThat(createdState.setupState).isEqualTo(SetupState.Created(FakeEncryptionService.FAKE_RECOVERY_KEY))
            enableRecoveryLambda.assertions().isNeverCalled()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - custom spec Confirm mismatch clears once user edits to match`() = runTest {
        val presenter = createSecureBackupSetupPresenter(
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val withSpec = awaitItem()

            val valid = "Ab12!@cd"
            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(valid))
            awaitItem()
            withSpec.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
            val onConfirm = awaitItem()
            assertThat(onConfirm.customPassphraseMismatch).isFalse()

            // Typing a different value flips mismatch=true and blocks submit.
            onConfirm.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphraseConfirm("differen"))
            val mismatched = awaitItem()
            assertThat(mismatched.customPassphraseMismatch).isTrue()
            assertThat(mismatched.canSubmitCustomPassphrase).isFalse()

            // Editing the confirm field to match clears the mismatch and re-enables submit.
            mismatched.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphraseConfirm(valid))
            val matched = awaitItem()
            assertThat(matched.customPassphraseMismatch).isFalse()
            assertThat(matched.canSubmitCustomPassphrase).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - custom spec rapid double-submit invokes the SDK only once`() = runTest {
        val enableRecoveryLambda = lambdaRecorder<Boolean, String?, Result<String>> { _, _ -> Result.success(A_RECOVERY_KEY) }
        val encryptionService = FakeEncryptionService(enableRecoveryLambda = enableRecoveryLambda)
        val presenter = createSecureBackupSetupPresenter(
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val withSpec = awaitItem()

            val valid = "Ab12!@cd"
            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(valid))
            awaitItem()
            withSpec.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
            val onConfirm = awaitItem()
            onConfirm.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphraseConfirm(valid))
            val ready = awaitItem()
            assertThat(ready.canSubmitCustomPassphrase).isTrue()

            // Two synchronous submits dispatched from the same snapshot (e.g., button + IME-Done
            // racing). The presenter's in-flight guard must drop the second one before it
            // launches a second enableRecovery coroutine.
            ready.eventSink.invoke(SecureBackupSetupEvents.SubmitCustomPassphrase)
            ready.eventSink.invoke(SecureBackupSetupEvents.SubmitCustomPassphrase)
            advanceUntilIdle()
            enableRecoveryLambda.assertions().isCalledOnce()
                .with(value(false), value(valid))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - change + custom spec submit routes to enableRecovery and never resetRecoveryKey`() = runTest {
        // Guards the custom-passphrase Change path against silently dropping the passphrase:
        // the user's passphrase reaches the SDK only via enableRecovery(passphrase); the
        // passphrase-less resetRecoveryKey() path must never be taken here. A true end-to-end
        // check that the 4S key actually rotates needs the real SDK + homeserver and lives
        // outside this JVM suite.
        val enableRecoveryLambda = lambdaRecorder<Boolean, String?, Result<String>> { _, _ -> Result.success(A_RECOVERY_KEY) }
        val resetRecoveryKeyLambda = lambdaRecorder<Result<String>> { Result.success(FakeEncryptionService.FAKE_RECOVERY_KEY) }
        val encryptionService = FakeEncryptionService(
            enableRecoveryLambda = enableRecoveryLambda,
            resetRecoveryKeyLambda = resetRecoveryKeyLambda,
        )
        val presenter = createSecureBackupSetupPresenter(
            isChangeRecoveryKeyUserStory = true,
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val withSpec = awaitItem()

            val valid = "Ab12!@cd"
            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(valid))
            awaitItem()
            withSpec.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
            val onConfirm = awaitItem()
            onConfirm.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphraseConfirm(valid))
            val ready = awaitItem()
            assertThat(ready.canSubmitCustomPassphrase).isTrue()

            ready.eventSink.invoke(SecureBackupSetupEvents.SubmitCustomPassphrase)
            awaitItem() // creating
            advanceUntilIdle()
            enableRecoveryLambda.assertions().isCalledOnce()
                .with(value(false), value(valid))
            resetRecoveryKeyLambda.assertions().isNeverCalled()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - custom spec cancel while creating aborts the SDK call and returns to Confirm with input preserved`() = runTest {
        // simulateLongTask suspends on delay(1) before invoking the lambda, so cancelling before
        // advancing virtual time guarantees the SDK call never runs.
        val enableRecoveryLambda = lambdaRecorder<Boolean, String?, Result<String>> { _, _ -> Result.success(A_RECOVERY_KEY) }
        val encryptionService = FakeEncryptionService(enableRecoveryLambda = enableRecoveryLambda)
        val presenter = createSecureBackupSetupPresenter(
            encryptionService = encryptionService,
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val withSpec = awaitItem()

            val valid = "Ab12!@cd"
            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(valid))
            awaitItem()
            withSpec.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
            val onConfirm = awaitItem()
            onConfirm.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphraseConfirm(valid))
            val ready = awaitItem()
            assertThat(ready.canSubmitCustomPassphrase).isTrue()

            ready.eventSink.invoke(SecureBackupSetupEvents.SubmitCustomPassphrase)
            val creating = awaitItem()
            assertThat(creating.setupState).isEqualTo(SetupState.Creating)

            creating.eventSink.invoke(SecureBackupSetupEvents.CancelCustomPassphraseSubmit)
            val cancelled = awaitItem()
            assertThat(cancelled.setupState).isEqualTo(SetupState.Init)
            assertThat(cancelled.customEntryStep).isEqualTo(CustomEntryStep.Confirm)
            assertThat(cancelled.customPassphrase).isEqualTo(valid)
            assertThat(cancelled.customPassphraseConfirm).isEqualTo(valid)
            assertThat(cancelled.canSubmitCustomPassphrase).isTrue()

            // The cancelled coroutine never reached the SDK, and no zombie success/error followed.
            advanceUntilIdle()
            enableRecoveryLambda.assertions().isNeverCalled()
            expectNoEvents()
        }
    }

    @Test
    fun `present - custom spec strength indicator is suppressed on the Confirm step`() = runTest {
        val presenter = createSecureBackupSetupPresenter(
            sessionWellknownRetriever = FakeSessionWellknownRetriever {
                WellknownRetrieverResult.Success(anElementWellKnown(customRecoveryPassphraseRequirements = aCustomRecoveryPassphraseRequirements()))
            },
            enterpriseService = FakeEnterpriseService(
                isCustomRecoveryPassphraseEnabledResult = true,
                estimateCustomRecoveryPassphraseStrengthResult = {
                    CustomRecoveryPassphraseStrengthResult(CustomRecoveryPassphraseStrength.Strong, score = 0.9f)
                },
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem() // pre-fetch
            val withSpec = awaitItem()

            val valid = "Ab12!@cd"
            withSpec.eventSink.invoke(SecureBackupSetupEvents.UpdateCustomPassphrase(valid))
            val onEntry = awaitItem()
            // Entry step: the estimator result is surfaced.
            assertThat(onEntry.customPassphraseStrength?.strength).isEqualTo(CustomRecoveryPassphraseStrength.Strong)

            onEntry.eventSink.invoke(SecureBackupSetupEvents.ContinueCustomPassphrase)
            val onConfirm = awaitItem()
            // Confirm step: indicator is not rendered, so the presenter stops computing it.
            assertThat(onConfirm.customEntryStep).isEqualTo(CustomEntryStep.Confirm)
            assertThat(onConfirm.customPassphraseStrength).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createSecureBackupSetupPresenter(
        isChangeRecoveryKeyUserStory: Boolean = false,
        encryptionService: EncryptionService = FakeEncryptionService(
            enableRecoveryLambda = { _, _ -> Result.success(A_RECOVERY_KEY) },
        ),
        sessionWellknownRetriever: SessionWellknownRetriever = FakeSessionWellknownRetriever(),
        enterpriseService: EnterpriseService = FakeEnterpriseService(isCustomRecoveryPassphraseEnabledResult = true),
    ): SecureBackupSetupPresenter {
        return SecureBackupSetupPresenter(
            isChangeRecoveryKeyUserStory = isChangeRecoveryKeyUserStory,
            stateMachine = SecureBackupSetupStateMachine(),
            encryptionService = encryptionService,
            sessionWellknownRetriever = sessionWellknownRetriever,
            enterpriseService = enterpriseService,
        )
    }
}
