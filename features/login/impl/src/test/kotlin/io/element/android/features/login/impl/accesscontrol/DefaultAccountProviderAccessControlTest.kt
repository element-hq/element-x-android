/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accesscontrol

import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.changeserver.AccountProviderAccessException
import io.element.android.features.wellknown.test.FakeWellknownRetriever
import io.element.android.features.wellknown.test.anElementWellKnown
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER_2
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER_URL
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test

class DefaultAccountProviderAccessControlTest {
    @Test
    fun `foss build should not allow using account provider that enforce enterprise build`() {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = false,
            isAllowedToConnectToHomeserver = true,
            elementWellKnown = anElementWellKnown(
                enforceElementPro = true,
            ),
        )
        accessControl.expectNeedElementProException()
    }

    @Test
    fun `foss build should not allow using account provider that enforce enterprise build taking precedence over authorization`() {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = false,
            // false here.
            isAllowedToConnectToHomeserver = false,
            elementWellKnown = anElementWellKnown(
                enforceElementPro = true,
            ),
        )
        accessControl.expectNeedElementProException()
    }

    @Test
    fun `foss build should allow using account provider that does not enforce enterprise build`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = false,
            isAllowedToConnectToHomeserver = true,
            elementWellKnown = anElementWellKnown(
                enforceElementPro = false,
            ),
        )
        accessControl.expectAllowed()
    }

    @Test
    fun `foss build should allow using account provider twith missing key in wellknown`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = false,
            isAllowedToConnectToHomeserver = true,
            elementWellKnown = anElementWellKnown(
                enforceElementPro = null,
            ),
        )
        accessControl.expectAllowed()
    }

    @Test
    fun `foss build should allow using account provider twith missing wellknown`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = false,
            isAllowedToConnectToHomeserver = true,
            elementWellKnown = null,
        )
        accessControl.expectAllowed()
    }

    @Test
    fun `foss build should not allow using account provider that do not enforce enterprise build but is not allowed`() {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = false,
            isAllowedToConnectToHomeserver = false,
            allowedAccountProviders = listOf(AN_ACCOUNT_PROVIDER_2),
            elementWellKnown = anElementWellKnown(
                enforceElementPro = false,
            ),
        )
        accessControl.expectUnauthorizedAccountProviderException()
    }

    @Test
    fun `enterprise build should allow using account provider that enforce enterprise build`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = true,
            isAllowedToConnectToHomeserver = true,
            elementWellKnown = anElementWellKnown(
                enforceElementPro = true,
            ),
        )
        accessControl.expectAllowed()
    }

    @Test
    fun `enterprise build should allow using account provider that do not enforce enterprise build`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = true,
            isAllowedToConnectToHomeserver = true,
            elementWellKnown = anElementWellKnown(
                enforceElementPro = false,
            ),
        )
        accessControl.expectAllowed()
    }

    @Test
    fun `enterprise build should not allow using account provider that enforce enterprise build but is not allowed`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = true,
            isAllowedToConnectToHomeserver = false,
            allowedAccountProviders = listOf(AN_ACCOUNT_PROVIDER_2),
            elementWellKnown = anElementWellKnown(
                enforceElementPro = true,
            ),
        )
        accessControl.expectUnauthorizedAccountProviderException()
    }

    @Test
    fun `enterprise build should not allow using account provider that do not enforce enterprise build but is not allowed`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = true,
            isAllowedToConnectToHomeserver = false,
            allowedAccountProviders = listOf(AN_ACCOUNT_PROVIDER_2),
            elementWellKnown = anElementWellKnown(
                enforceElementPro = false,
            ),
        )
        accessControl.expectUnauthorizedAccountProviderException()
    }

    private fun createDefaultAccountProviderAccessControl(
        isEnterpriseBuild: Boolean = false,
        isAllowedToConnectToHomeserver: Boolean = false,
        allowedAccountProviders: List<String> = emptyList(),
        elementWellKnown: ElementWellKnown? = null,
    ) = DefaultAccountProviderAccessControl(
        enterpriseService = FakeEnterpriseService(
            isEnterpriseBuild = isEnterpriseBuild,
            isAllowedToConnectToHomeserverResult = { isAllowedToConnectToHomeserver },
            defaultHomeserverListResult = { allowedAccountProviders },
        ),
        wellknownRetriever = FakeWellknownRetriever(
            getElementWellKnownResult = {
                if (elementWellKnown == null) {
                    WellknownRetrieverResult.NotFound
                } else {
                    WellknownRetrieverResult.Success(elementWellKnown)
                }
            },
        ),
    )

    private fun DefaultAccountProviderAccessControl.expectNeedElementProException() {
        val exception = assertThrows(AccountProviderAccessException.NeedElementProException::class.java) {
            runTest {
                assertIsAllowedToConnectToAccountProvider(
                    title = AN_ACCOUNT_PROVIDER,
                    accountProviderUrl = AN_ACCOUNT_PROVIDER_URL,
                )
            }
        }
        assertThat(exception.unauthorisedAccountProviderTitle).isEqualTo(AN_ACCOUNT_PROVIDER)
        assertThat(exception.applicationId).isEqualTo("io.element.enterprise")
        runTest {
            assertThat(
                isAllowedToConnectToAccountProvider(
                    accountProviderUrl = AN_ACCOUNT_PROVIDER_URL,
                )
            ).isFalse()
        }
    }

    private fun DefaultAccountProviderAccessControl.expectUnauthorizedAccountProviderException() {
        val exception = assertThrows(AccountProviderAccessException.UnauthorizedAccountProviderException::class.java) {
            runTest {
                assertIsAllowedToConnectToAccountProvider(
                    title = AN_ACCOUNT_PROVIDER,
                    accountProviderUrl = AN_ACCOUNT_PROVIDER_URL,
                )
            }
        }
        assertThat(exception.unauthorisedAccountProviderTitle).isEqualTo(AN_ACCOUNT_PROVIDER)
        assertThat(exception.authorisedAccountProviderTitles).containsExactly(AN_ACCOUNT_PROVIDER_2)
        runTest {
            assertThat(
                isAllowedToConnectToAccountProvider(
                    accountProviderUrl = AN_ACCOUNT_PROVIDER_URL,
                )
            ).isFalse()
        }
    }

    private suspend fun DefaultAccountProviderAccessControl.expectAllowed() {
        // If no exception is thrown, the test passes
        assertIsAllowedToConnectToAccountProvider(
            title = AN_ACCOUNT_PROVIDER,
            accountProviderUrl = AN_ACCOUNT_PROVIDER_URL,
        )
        runTest {
            assertThat(
                isAllowedToConnectToAccountProvider(
                    accountProviderUrl = AN_ACCOUNT_PROVIDER_URL,
                )
            ).isTrue()
        }
    }
}
