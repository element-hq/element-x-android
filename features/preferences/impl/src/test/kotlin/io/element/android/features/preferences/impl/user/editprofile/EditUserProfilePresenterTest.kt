/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.user.editprofile

import android.net.Uri
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.mediapickers.test.FakePickerProvider
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.consumeItemsUntilTimeout
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@ExperimentalCoroutinesApi
class EditUserProfilePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private lateinit var fakePickerProvider: FakePickerProvider
    private lateinit var fakeMediaPreProcessor: FakeMediaPreProcessor

    private val userAvatarUri: Uri = mockk()
    private val anotherAvatarUri: Uri = mockk()

    private val fakeFileContents = ByteArray(2)

    @Before
    fun setup() {
        fakePickerProvider = FakePickerProvider()
        fakeMediaPreProcessor = FakeMediaPreProcessor()
        mockkStatic(Uri::class)

        every { Uri.parse(AN_AVATAR_URL) } returns userAvatarUri
        every { Uri.parse(ANOTHER_AVATAR_URL) } returns anotherAvatarUri
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun createEditUserProfilePresenter(
        matrixClient: MatrixClient = FakeMatrixClient(),
        buildMeta: BuildMeta = aBuildMeta(),
        matrixUser: MatrixUser = aMatrixUser(),
        permissionsPresenter: PermissionsPresenter = FakePermissionsPresenter(),
    ): EditUserProfilePresenter {
        return EditUserProfilePresenter(
            matrixClient = matrixClient,
            matrixUser = matrixUser,
            buildMeta = buildMeta,
            mediaPickerProvider = fakePickerProvider,
            mediaPreProcessor = fakeMediaPreProcessor,
            permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter),
        )
    }

    @Test
    fun `present - initial state is created from user info`() = runTest {
        val user = aMatrixUser(avatarUrl = AN_AVATAR_URL)
        val presenter = createEditUserProfilePresenter(matrixUser = user)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userId).isEqualTo(user.userId)
            assertThat(initialState.displayName).isEqualTo(user.displayName)
            assertThat(initialState.userAvatarUrl).isEqualTo(userAvatarUri)
            assertThat(initialState.avatarActions).containsExactly(
                AvatarAction.ChoosePhoto,
                AvatarAction.TakePhoto,
                AvatarAction.Remove
            )
            assertThat(initialState.saveButtonEnabled).isFalse()
            assertThat(initialState.saveAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
        }
    }

    @Test
    fun `present - updates state in response to changes`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val presenter = createEditUserProfilePresenter(matrixUser = user)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.displayName).isEqualTo("Name")
            assertThat(initialState.userAvatarUrl).isEqualTo(userAvatarUri)
            initialState.eventSink(EditUserProfileEvents.UpdateDisplayName("Name II"))
            awaitItem().apply {
                assertThat(displayName).isEqualTo("Name II")
                assertThat(userAvatarUrl).isEqualTo(userAvatarUri)
            }
            initialState.eventSink(EditUserProfileEvents.UpdateDisplayName("Name III"))
            awaitItem().apply {
                assertThat(displayName).isEqualTo("Name III")
                assertThat(userAvatarUrl).isEqualTo(userAvatarUri)
            }
            initialState.eventSink(EditUserProfileEvents.HandleAvatarAction(AvatarAction.Remove))
            awaitItem().apply {
                assertThat(displayName).isEqualTo("Name III")
                assertThat(userAvatarUrl).isNull()
            }
        }
    }

    @Test
    fun `present - obtains avatar uris from gallery`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        fakePickerProvider.givenResult(anotherAvatarUri)
        val presenter = createEditUserProfilePresenter(matrixUser = user)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userAvatarUrl).isEqualTo(userAvatarUri)
            initialState.eventSink(EditUserProfileEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            awaitItem().apply {
                assertThat(userAvatarUrl).isEqualTo(anotherAvatarUri)
            }
        }
    }

    @Test
    fun `present - obtains avatar uris from camera`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        fakePickerProvider.givenResult(anotherAvatarUri)
        val fakePermissionsPresenter = FakePermissionsPresenter()
        val presenter = createEditUserProfilePresenter(
            matrixUser = user,
            permissionsPresenter = fakePermissionsPresenter,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userAvatarUrl).isEqualTo(userAvatarUri)
            assertThat(initialState.cameraPermissionState.permissionGranted).isFalse()
            initialState.eventSink(EditUserProfileEvents.HandleAvatarAction(AvatarAction.TakePhoto))
            val stateWithAskingPermission = awaitItem()
            assertThat(stateWithAskingPermission.cameraPermissionState.showDialog).isTrue()
            fakePermissionsPresenter.setPermissionGranted()
            val stateWithPermission = awaitItem()
            assertThat(stateWithPermission.cameraPermissionState.permissionGranted).isTrue()
            val stateWithNewAvatar = awaitItem()
            assertThat(stateWithNewAvatar.userAvatarUrl).isEqualTo(anotherAvatarUri)
            // Do it again, no permission is requested
            fakePickerProvider.givenResult(userAvatarUri)
            stateWithNewAvatar.eventSink(EditUserProfileEvents.HandleAvatarAction(AvatarAction.TakePhoto))
            val stateWithNewAvatar2 = awaitItem()
            assertThat(stateWithNewAvatar2.userAvatarUrl).isEqualTo(userAvatarUri)
        }
    }

    @Test
    fun `present - updates save button state`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        fakePickerProvider.givenResult(userAvatarUri)
        val presenter = createEditUserProfilePresenter(matrixUser = user)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.saveButtonEnabled).isFalse()
            // Once a change is made, the save button is enabled
            initialState.eventSink(EditUserProfileEvents.UpdateDisplayName("Name II"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }
            // If it's reverted then the save disables again
            initialState.eventSink(EditUserProfileEvents.UpdateDisplayName("Name"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }
            // Make a change...
            initialState.eventSink(EditUserProfileEvents.HandleAvatarAction(AvatarAction.Remove))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }
            // Revert it...
            initialState.eventSink(EditUserProfileEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }
        }
    }

    @Test
    fun `present - updates save button state when initial values are null`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = null)
        fakePickerProvider.givenResult(userAvatarUri)
        val presenter = createEditUserProfilePresenter(matrixUser = user)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.saveButtonEnabled).isFalse()
            // Once a change is made, the save button is enabled
            initialState.eventSink(EditUserProfileEvents.UpdateDisplayName("Name II"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }
            // If it's reverted then the save disables again
            initialState.eventSink(EditUserProfileEvents.UpdateDisplayName("Name"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }
            // Make a change...
            initialState.eventSink(EditUserProfileEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }
            // Revert it...
            initialState.eventSink(EditUserProfileEvents.HandleAvatarAction(AvatarAction.Remove))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }
        }
    }

    @Test
    fun `present - save changes room details if different`() = runTest {
        val matrixClient = FakeMatrixClient()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val presenter = createEditUserProfilePresenter(
            matrixClient = matrixClient,
            matrixUser = user
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(EditUserProfileEvents.UpdateDisplayName("New name"))
            initialState.eventSink(EditUserProfileEvents.HandleAvatarAction(AvatarAction.Remove))
            initialState.eventSink(EditUserProfileEvents.Save)
            consumeItemsUntilPredicate { matrixClient.setDisplayNameCalled && matrixClient.removeAvatarCalled && !matrixClient.uploadAvatarCalled }
            assertThat(matrixClient.setDisplayNameCalled).isTrue()
            assertThat(matrixClient.removeAvatarCalled).isTrue()
            assertThat(matrixClient.uploadAvatarCalled).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - save does not change room details if they're the same trimmed`() = runTest {
        val matrixClient = FakeMatrixClient()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val presenter = createEditUserProfilePresenter(
            matrixClient = matrixClient,
            matrixUser = user
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(EditUserProfileEvents.UpdateDisplayName("   Name   "))
            initialState.eventSink(EditUserProfileEvents.Save)
            consumeItemsUntilTimeout()
            assertThat(matrixClient.setDisplayNameCalled).isFalse()
            assertThat(matrixClient.uploadAvatarCalled).isFalse()
            assertThat(matrixClient.removeAvatarCalled).isFalse()
        }
    }

    @Test
    fun `present - save does not change name if it's now empty`() = runTest {
        val matrixClient = FakeMatrixClient()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val presenter = createEditUserProfilePresenter(
            matrixClient = matrixClient,
            matrixUser = user
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(EditUserProfileEvents.UpdateDisplayName(""))
            initialState.eventSink(EditUserProfileEvents.Save)
            assertThat(matrixClient.setDisplayNameCalled).isFalse()
            assertThat(matrixClient.uploadAvatarCalled).isFalse()
            assertThat(matrixClient.removeAvatarCalled).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - save processes and sets avatar when processor returns successfully`() = runTest {
        val matrixClient = FakeMatrixClient()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        givenPickerReturnsFile()
        val presenter = createEditUserProfilePresenter(
            matrixClient = matrixClient,
            matrixUser = user
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(EditUserProfileEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            initialState.eventSink(EditUserProfileEvents.Save)
            consumeItemsUntilPredicate { matrixClient.uploadAvatarCalled }
            assertThat(matrixClient.uploadAvatarCalled).isTrue()
        }
    }

    @Test
    fun `present - save does not set avatar data if processor fails`() = runTest {
        val matrixClient = FakeMatrixClient()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val presenter = createEditUserProfilePresenter(
            matrixClient = matrixClient,
            matrixUser = user
        )
        fakePickerProvider.givenResult(anotherAvatarUri)
        fakeMediaPreProcessor.givenResult(Result.failure(Throwable("Oh no")))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(EditUserProfileEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            initialState.eventSink(EditUserProfileEvents.Save)
            skipItems(2)
            assertThat(matrixClient.uploadAvatarCalled).isFalse()
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)
        }
    }

    @Test
    fun `present - sets save action to failure if name update fails`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val matrixClient = FakeMatrixClient().apply {
            givenSetDisplayNameResult(Result.failure(Throwable("!")))
        }
        saveAndAssertFailure(user, matrixClient, EditUserProfileEvents.UpdateDisplayName("New name"))
    }

    @Test
    fun `present - sets save action to failure if removing avatar fails`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val matrixClient = FakeMatrixClient().apply {
            givenRemoveAvatarResult(Result.failure(Throwable("!")))
        }
        saveAndAssertFailure(user, matrixClient, EditUserProfileEvents.HandleAvatarAction(AvatarAction.Remove))
    }

    @Test
    fun `present - sets save action to failure if setting avatar fails`() = runTest {
        givenPickerReturnsFile()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val matrixClient = FakeMatrixClient().apply {
            givenUploadAvatarResult(Result.failure(Throwable("!")))
        }
        saveAndAssertFailure(user, matrixClient, EditUserProfileEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
    }

    @Test
    fun `present - CancelSaveChanges resets save action state`() = runTest {
        givenPickerReturnsFile()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val matrixClient = FakeMatrixClient().apply {
            givenSetDisplayNameResult(Result.failure(Throwable("!")))
        }
        val presenter = createEditUserProfilePresenter(matrixUser = user, matrixClient = matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(EditUserProfileEvents.UpdateDisplayName("foo"))
            initialState.eventSink(EditUserProfileEvents.Save)
            skipItems(2)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)
            initialState.eventSink(EditUserProfileEvents.CancelSaveChanges)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
        }
    }

    private suspend fun saveAndAssertFailure(matrixUser: MatrixUser, matrixClient: MatrixClient, event: EditUserProfileEvents) {
        val presenter = createEditUserProfilePresenter(matrixUser = matrixUser, matrixClient = matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(event)
            initialState.eventSink(EditUserProfileEvents.Save)
            skipItems(1)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)
        }
    }

    private fun givenPickerReturnsFile() {
        mockkStatic(File::readBytes)
        val processedFile: File = mockk {
            every { readBytes() } returns fakeFileContents
        }
        fakePickerProvider.givenResult(anotherAvatarUri)
        fakeMediaPreProcessor.givenResult(
            Result.success(
                MediaUploadInfo.AnyFile(
                    file = processedFile,
                    fileInfo = mockk(),
                )
            )
        )
    }

    companion object {
        private const val ANOTHER_AVATAR_URL = "example://camera/foo.jpg"
    }
}
