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
import io.element.android.libraries.matrix.api.accountprovider.AccountProvider
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER_2
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER_URL
import io.element.android.libraries.matrix.test.accountprovider.anAccountProviderManaged
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test

class DefaultAccountProviderAccessControlTest {
    /**
     * The account provider the assertions are run against: its server name is the title reported by the exceptions,
     * its base url is what the enterprise service is queried with.
     */
    private fun anAccountProviderUnderTest() = anAccountProviderManaged(
        serverName = AN_ACCOUNT_PROVIDER,
        baseUrl = AN_ACCOUNT_PROVIDER_URL,
    )

    @Test
    fun `foss build should not allow using account provider that enforce enterprise build`() {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = false,
            isAllowedToConnectToAccountProvider = true,
            enterpriseService = FakeEnterpriseService(isElementProEnforcedResult = { true }),
        )
        accessControl.expectNeedElementProException()
    }

    @Test
    fun `foss build should not allow using account provider that enforce enterprise build taking precedence over authorization`() {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = false,
            // false here.
            isAllowedToConnectToAccountProvider = false,
            enterpriseService = FakeEnterpriseService(isElementProEnforcedResult = { true }),
        )
        accessControl.expectNeedElementProException()
    }

    @Test
    fun `foss build should allow using account provider that does not enforce enterprise build`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = false,
            isAllowedToConnectToAccountProvider = true,
            enterpriseService = FakeEnterpriseService(
                isAllowedToConnectToAccountProviderResult = { true },
                isElementProEnforcedResult = { false }
            ),
        )
        accessControl.expectAllowed()
    }

    @Test
    fun `foss build should allow using account provider twith missing key in wellknown`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = false,
            isAllowedToConnectToAccountProvider = true,
        )
        accessControl.expectAllowed()
    }

    @Test
    fun `foss build should allow using account provider with missing wellknown`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = false,
            isAllowedToConnectToAccountProvider = true,
        )
        accessControl.expectAllowed()
    }

    @Test
    fun `foss build should not allow using account provider that do not enforce enterprise build but is not allowed`() {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = false,
            isAllowedToConnectToAccountProvider = false,
            allowedAccountProviders = listOf(anAccountProviderManaged(serverName = AN_ACCOUNT_PROVIDER_2)),
            enterpriseService = FakeEnterpriseService(
                isAllowedToConnectToAccountProviderResult = { false },
                isElementProEnforcedResult = { false },
                accountProviderAllowListResult = { listOf(anAccountProviderManaged(serverName = AN_ACCOUNT_PROVIDER_2)) },
            ),
        )
        accessControl.expectUnauthorizedAccountProviderException()
    }

    @Test
    fun `enterprise build should allow using account provider that enforce enterprise build`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = true,
            isAllowedToConnectToAccountProvider = true,
            enterpriseService = FakeEnterpriseService(
                isAllowedToConnectToAccountProviderResult = { true },
                isElementProEnforcedResult = { true },
            ),
        )
        accessControl.expectAllowed()
    }

    @Test
    fun `enterprise build should allow using account provider that do not enforce enterprise build`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = true,
            isAllowedToConnectToAccountProvider = true,
            enterpriseService = FakeEnterpriseService(
                isAllowedToConnectToAccountProviderResult = { true },
                isElementProEnforcedResult = { false },
            ),
        )
        accessControl.expectAllowed()
    }

    @Test
    fun `enterprise build should not allow using account provider that enforce enterprise build but is not allowed`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = true,
            isAllowedToConnectToAccountProvider = false,
            allowedAccountProviders = listOf(anAccountProviderManaged(serverName = AN_ACCOUNT_PROVIDER_2)),
            enterpriseService = FakeEnterpriseService(
                isAllowedToConnectToAccountProviderResult = { false },
                isElementProEnforcedResult = { true },
                accountProviderAllowListResult = { listOf(anAccountProviderManaged(serverName = AN_ACCOUNT_PROVIDER_2)) },
            ),
        )
        accessControl.expectUnauthorizedAccountProviderException()
    }

    @Test
    fun `enterprise build should not allow using account provider that do not enforce enterprise build but is not allowed`() = runTest {
        val accessControl = createDefaultAccountProviderAccessControl(
            isEnterpriseBuild = true,
            isAllowedToConnectToAccountProvider = false,
            allowedAccountProviders = listOf(anAccountProviderManaged(serverName = AN_ACCOUNT_PROVIDER_2)),
            enterpriseService = FakeEnterpriseService(
                isAllowedToConnectToAccountProviderResult = { false },
                isElementProEnforcedResult = { false },
                accountProviderAllowListResult = { listOf(anAccountProviderManaged(serverName = AN_ACCOUNT_PROVIDER_2)) },
            ),
        )
        accessControl.expectUnauthorizedAccountProviderException()
    }

    private fun createDefaultAccountProviderAccessControl(
        isEnterpriseBuild: Boolean = false,
        isAllowedToConnectToAccountProvider: Boolean = false,
        allowedAccountProviders: List<AccountProvider> = emptyList(),
        enterpriseService: FakeEnterpriseService = FakeEnterpriseService(
            isAllowedToConnectToAccountProviderResult = { isAllowedToConnectToAccountProvider },
            accountProviderAllowListResult = { allowedAccountProviders },
            isElementProEnforcedResult = { false },
        )
    ) = DefaultAccountProviderAccessControl(
        isEnterpriseBuild = { isEnterpriseBuild },
        enterpriseService = enterpriseService,
    )

    private fun DefaultAccountProviderAccessControl.expectNeedElementProException() {
        val exception = assertThrows(AccountProviderAccessException.NeedElementProException::class.java) {
            runTest {
                assertIsAllowedToConnectToAccountProvider(anAccountProviderUnderTest())
            }
        }
        assertThat(exception.unauthorisedAccountProviderTitle).isEqualTo(AN_ACCOUNT_PROVIDER)
        assertThat(exception.applicationId).isEqualTo("io.element.enterprise")
        runTest {
            assertThat(
                isAllowedToConnectToAccountProvider(anAccountProviderUnderTest())
            ).isFalse()
        }
    }

    private fun DefaultAccountProviderAccessControl.expectUnauthorizedAccountProviderException() {
        val exception = assertThrows(AccountProviderAccessException.UnauthorizedAccountProviderException::class.java) {
            runTest {
                assertIsAllowedToConnectToAccountProvider(anAccountProviderUnderTest())
            }
        }
        assertThat(exception.unauthorisedAccountProviderTitle).isEqualTo(AN_ACCOUNT_PROVIDER)
        assertThat(exception.authorisedAccountProviderTitles).containsExactly(AN_ACCOUNT_PROVIDER_2)
        runTest {
            assertThat(
                isAllowedToConnectToAccountProvider(anAccountProviderUnderTest())
            ).isFalse()
        }
    }

    private suspend fun DefaultAccountProviderAccessControl.expectAllowed() {
        // If no exception is thrown, the test passes
        assertIsAllowedToConnectToAccountProvider(anAccountProviderUnderTest())
        runTest {
            assertThat(
                isAllowedToConnectToAccountProvider(anAccountProviderUnderTest())
            ).isTrue()
        }
    }
}
