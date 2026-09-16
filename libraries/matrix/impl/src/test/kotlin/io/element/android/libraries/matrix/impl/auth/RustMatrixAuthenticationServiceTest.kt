/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.paths.SessionPaths
import io.element.android.libraries.matrix.impl.ClientBuilderProvider
import io.element.android.libraries.matrix.impl.FakeClientBuilderProvider
import io.element.android.libraries.matrix.impl.auth.qrlogin.SdkQrCodeLoginData
import io.element.android.libraries.matrix.impl.createRustMatrixClientFactory
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClientBuilder
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiHomeserverLoginDetails
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiLoginWithQrCodeHandler
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiQrCodeData
import io.element.android.libraries.matrix.impl.paths.SessionPathsFactory
import io.element.android.libraries.matrix.impl.storage.FakeSqliteStoreBuilder
import io.element.android.libraries.matrix.impl.storage.FakeSqliteStoreBuilderProvider
import io.element.android.libraries.matrix.impl.storage.SqliteStoreBuilder
import io.element.android.libraries.matrix.impl.storage.SqliteStoreBuilderProvider
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.auth.FakeOAuthRedirectUrlProvider
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.workmanager.test.FakeWorkManagerScheduler
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.HumanQrLoginException
import java.io.File

class RustMatrixAuthenticationServiceTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `setHomeserver is successful`() = runTest {
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeClientBuilderProvider(
                provideResult = {
                    FakeFfiClientBuilder(
                        buildResult = {
                            FakeFfiClient(
                                homeserverLoginDetailsResult = {
                                    FakeFfiHomeserverLoginDetails()
                                }
                            )
                        }
                    )
                }
            ),
        )
        assertThat(sut.setHomeserver("matrix.org").isSuccess).isTrue()
    }

    @Test
    fun `setHomeserver can fail gracefully and clean up the temporary client`() = runTest {
        val closeResult = lambdaRecorder<Unit> {}
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeClientBuilderProvider(
                provideResult = {
                    FakeFfiClientBuilder(
                        buildResult = {
                            FakeFfiClient(
                                homeserverLoginDetailsResult = {
                                    throw IllegalStateException("Failed to get homeserver login details")
                                },
                                closeResult = closeResult,
                            )
                        },
                    )
                },
            ),
        )
        assertThat(sut.setHomeserver("matrix.org").isFailure).isTrue()
        closeResult.assertions().isCalledOnce()
    }

    @Test
    fun `login closes the client used to log in before building the client of the session`() = runTest {
        val events = mutableListOf<String>()
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeSequentialClientBuilderProvider(
                {
                    events.add("build login client")
                    FakeFfiClient(
                        homeserverLoginDetailsResult = { FakeFfiHomeserverLoginDetails() },
                        loginResult = { _, _ -> },
                        closeResult = { events.add("close login client") },
                    )
                },
                {
                    events.add("build session client")
                    FakeFfiClient(withUtdHook = {})
                },
            ),
        )

        assertThat(sut.setHomeserver("matrix.org").isSuccess).isTrue()
        assertThat(sut.login("alice", "password").getOrNull()).isEqualTo(A_USER_ID)

        // The two clients share the same session paths, so the login one must be closed first.
        assertThat(events).containsExactly("build login client", "close login client", "build session client").inOrder()
    }

    @Test
    fun `loginWithQrCode closes the client used to log in before building the client of the session`() = runTest {
        val events = mutableListOf<String>()
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeSequentialClientBuilderProvider(
                {
                    events.add("build login client")
                    FakeFfiClient(
                        newLoginWithQrCodeHandlerResult = { FakeFfiLoginWithQrCodeHandler() },
                        closeResult = { events.add("close login client") },
                    )
                },
                {
                    events.add("build session client")
                    FakeFfiClient(withUtdHook = {})
                },
            ),
        )

        val result = sut.loginWithQrCode(aSdkQrCodeLoginData()) {}

        assertThat(result.getOrNull()).isEqualTo(A_USER_ID)
        assertThat(events).containsExactly("build login client", "close login client", "build session client").inOrder()
    }

    @Test
    fun `loginWithQrCode closes the client it created when the login fails`() = runTest {
        val closeResult = lambdaRecorder<Unit> {}
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeClientBuilderProvider(
                provideResult = {
                    FakeFfiClientBuilder(
                        buildResult = {
                            FakeFfiClient(
                                newLoginWithQrCodeHandlerResult = {
                                    FakeFfiLoginWithQrCodeHandler(
                                        scanResult = { throw HumanQrLoginException.Unknown() },
                                    )
                                },
                                closeResult = closeResult,
                            )
                        },
                    )
                },
            ),
        )

        assertThat(sut.loginWithQrCode(aSdkQrCodeLoginData()) {}.isFailure).isTrue()
        closeResult.assertions().isCalledOnce()
    }

    private fun aSdkQrCodeLoginData() = SdkQrCodeLoginData(
        FakeFfiQrCodeData(
            baseUrlResult = { A_HOMESERVER_URL },
        )
    )

    /**
     * A [ClientBuilderProvider] handing out one [Client] per call, in order.
     */
    private class FakeSequentialClientBuilderProvider(
        private vararg val clients: () -> Client,
    ) : ClientBuilderProvider {
        private var index = 0
        override fun provide(): ClientBuilder = FakeFfiClientBuilder(buildResult = clients[index++])
    }

    @Test
    fun `a new login does not delete the session data of the account which has just been logged in`() = runTest {
        val storeBuilderProvider = SessionDirectoryCreatingSqliteStoreBuilderProvider()
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeSequentialClientBuilderProvider(
                { aLoginFakeFfiClient() },
                { FakeFfiClient(withUtdHook = {}) },
                { aLoginFakeFfiClient() },
            ),
            sessionPathsFactory = SessionPathsFactory(temporaryFolder.newFolder("base"), temporaryFolder.newFolder("cache")),
            sqliteStoreBuilderProvider = storeBuilderProvider,
        )

        assertThat(sut.setHomeserver("matrix.org").isSuccess).isTrue()
        assertThat(sut.login("alice", "password").getOrNull()).isEqualTo(A_USER_ID)
        val loggedInSessionPaths = storeBuilderProvider.providedSessionPaths.first()

        // Adding another account rotates the session paths, which must not touch the previous account.
        assertThat(sut.setHomeserver("matrix.org").isSuccess).isTrue()

        assertThat(loggedInSessionPaths.fileDirectory.exists()).isTrue()
        assertThat(loggedInSessionPaths.cacheDirectory.exists()).isTrue()
    }

    @Test
    fun `a new login deletes the session data of a previous failed login attempt`() = runTest {
        val storeBuilderProvider = SessionDirectoryCreatingSqliteStoreBuilderProvider()
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeSequentialClientBuilderProvider(
                { aLoginFakeFfiClient() },
                { throw IllegalStateException("Failed to build the client of the session") },
                { aLoginFakeFfiClient() },
            ),
            sessionPathsFactory = SessionPathsFactory(temporaryFolder.newFolder("base"), temporaryFolder.newFolder("cache")),
            sqliteStoreBuilderProvider = storeBuilderProvider,
        )

        assertThat(sut.setHomeserver("matrix.org").isSuccess).isTrue()
        assertThat(sut.login("alice", "password").isFailure).isTrue()
        val abandonedSessionPaths = storeBuilderProvider.providedSessionPaths.first()

        assertThat(sut.setHomeserver("matrix.org").isSuccess).isTrue()

        assertThat(abandonedSessionPaths.fileDirectory.exists()).isFalse()
        assertThat(abandonedSessionPaths.cacheDirectory.exists()).isFalse()
    }

    private fun aLoginFakeFfiClient() = FakeFfiClient(
        homeserverLoginDetailsResult = { FakeFfiHomeserverLoginDetails() },
        loginResult = { _, _ -> },
    )

    /**
     * A [SqliteStoreBuilderProvider] which creates the session directories, like the SDK does when it
     * opens its stores, and records them so that a test can assert on their lifecycle.
     */
    private class SessionDirectoryCreatingSqliteStoreBuilderProvider : SqliteStoreBuilderProvider {
        val providedSessionPaths = mutableListOf<SessionPaths>()

        override fun provide(sessionPaths: SessionPaths): SqliteStoreBuilder {
            sessionPaths.fileDirectory.mkdirs()
            sessionPaths.cacheDirectory.mkdirs()
            providedSessionPaths.add(sessionPaths)
            return FakeSqliteStoreBuilder()
        }
    }

    private fun TestScope.createRustMatrixAuthenticationService(
        sessionStore: SessionStore = InMemorySessionStore(updateUserProfileResult = { _, _, _ -> }),
        clientBuilderProvider: ClientBuilderProvider = FakeClientBuilderProvider(),
        enterpriseService: EnterpriseService = FakeEnterpriseService(),
        sessionPathsFactory: SessionPathsFactory = SessionPathsFactory(File("/base"), File("/cache")),
        sqliteStoreBuilderProvider: SqliteStoreBuilderProvider = FakeSqliteStoreBuilderProvider(),
    ): RustMatrixAuthenticationService {
        val rustMatrixClientFactory = createRustMatrixClientFactory(
            cacheDirectory = File("/cache"),
            sessionStore = sessionStore,
            clientBuilderProvider = clientBuilderProvider,
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}),
            sqliteStoreBuilderProvider = sqliteStoreBuilderProvider,
        )
        return RustMatrixAuthenticationService(
            sessionPathsFactory = sessionPathsFactory,
            coroutineDispatchers = testCoroutineDispatchers(),
            sessionStore = sessionStore,
            rustMatrixClientFactory = rustMatrixClientFactory,
            secretGenerator = FakeSecretGenerator(),
            oAuthConfigurationProvider = OAuthConfigurationProvider(
                buildMeta = aBuildMeta(),
                oAuthRedirectUrlProvider = FakeOAuthRedirectUrlProvider(),
            ),
            enterpriseService = enterpriseService,
            featureFlagService = FakeFeatureFlagService(),
            clientEnterpriseHook = {},
        )
    }
}
