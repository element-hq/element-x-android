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

package io.element.android.libraries.sessionstorage.impl

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.session.SessionData
import io.element.android.libraries.sessionstorage.api.LoggedInState
import io.element.android.libraries.sessionstorage.api.LoginType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DatabaseSessionStoreTests {
    private lateinit var database: SessionDatabase
    private lateinit var databaseSessionStore: DatabaseSessionStore

    private val aSessionData = SessionData(
        userId = "userId",
        deviceId = "deviceId",
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        homeserverUrl = "homeserverUrl",
        slidingSyncProxy = null,
        loginTimestamp = null,
        oidcData = "aOidcData",
        isTokenValid = 1,
        loginType = LoginType.UNKNOWN.name,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        // Initialise in memory SQLite driver
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SessionDatabase.Schema.create(driver)

        database = SessionDatabase(driver)
        databaseSessionStore = DatabaseSessionStore(
            database = database,
            dispatchers = CoroutineDispatchers(
                io = UnconfinedTestDispatcher(),
                computation = UnconfinedTestDispatcher(),
                main = UnconfinedTestDispatcher(),
            )
        )
    }

    @Test
    fun `storeData persists the SessionData into the DB`() = runTest {
        assertThat(database.sessionDataQueries.selectFirst().executeAsOneOrNull()).isNull()

        databaseSessionStore.storeData(aSessionData.toApiModel())

        assertThat(database.sessionDataQueries.selectFirst().executeAsOneOrNull()).isEqualTo(aSessionData)
        assertThat(database.sessionDataQueries.selectAll().executeAsList().size).isEqualTo(1)
    }

    @Test
    fun `isLoggedIn emits true while there are sessions in the DB`() = runTest {
        databaseSessionStore.isLoggedIn().test {
            assertThat(awaitItem()).isEqualTo(LoggedInState.NotLoggedIn)
            database.sessionDataQueries.insertSessionData(aSessionData)
            assertThat(awaitItem()).isEqualTo(LoggedInState.LoggedIn(sessionId = aSessionData.userId, isTokenValid = true))
            database.sessionDataQueries.removeSession(aSessionData.userId)
            assertThat(awaitItem()).isEqualTo(LoggedInState.NotLoggedIn)
        }
    }

    @Test
    fun `getLatestSession gets the first session in the DB`() = runTest {
        database.sessionDataQueries.insertSessionData(aSessionData)
        database.sessionDataQueries.insertSessionData(aSessionData.copy(userId = "otherUserId"))

        val latestSession = databaseSessionStore.getLatestSession()?.toDbModel()

        assertThat(latestSession).isEqualTo(aSessionData)
    }

    @Test
    fun `getSession returns a matching session in DB if exists`() = runTest {
        database.sessionDataQueries.insertSessionData(aSessionData)
        database.sessionDataQueries.insertSessionData(aSessionData.copy(userId = "otherUserId"))

        val foundSession = databaseSessionStore.getSession(aSessionData.userId)?.toDbModel()

        assertThat(foundSession).isEqualTo(aSessionData)
        assertThat(database.sessionDataQueries.selectAll().executeAsList().size).isEqualTo(2)
    }

    @Test
    fun `getSession returns null if a no matching session exists in DB`() = runTest {
        database.sessionDataQueries.insertSessionData(aSessionData.copy(userId = "otherUserId"))

        val foundSession = databaseSessionStore.getSession(aSessionData.userId)

        assertThat(foundSession).isNull()
    }

    @Test
    fun `removeSession removes the associated session in DB`() = runTest {
        database.sessionDataQueries.insertSessionData(aSessionData)

        databaseSessionStore.removeSession(aSessionData.userId)

        assertThat(database.sessionDataQueries.selectByUserId(aSessionData.userId).executeAsOneOrNull()).isNull()
    }

    @Test
    fun `update session update all fields except loginTimestamp`() = runTest {
        val firstSessionData = SessionData(
            userId = "userId",
            deviceId = "deviceId",
            accessToken = "accessToken",
            refreshToken = "refreshToken",
            homeserverUrl = "homeserverUrl",
            slidingSyncProxy = "slidingSyncProxy",
            loginTimestamp = 1,
            oidcData = "aOidcData",
            isTokenValid = 1,
            loginType = null,
        )
        val secondSessionData = SessionData(
            userId = "userId",
            deviceId = "deviceIdAltered",
            accessToken = "accessTokenAltered",
            refreshToken = "refreshTokenAltered",
            homeserverUrl = "homeserverUrlAltered",
            slidingSyncProxy = "slidingSyncProxyAltered",
            loginTimestamp = 2,
            oidcData = "aOidcDataAltered",
            isTokenValid = 1,
            loginType = null,
        )
        assertThat(firstSessionData.userId).isEqualTo(secondSessionData.userId)
        assertThat(firstSessionData.loginTimestamp).isNotEqualTo(secondSessionData.loginTimestamp)

        database.sessionDataQueries.insertSessionData(firstSessionData)
        databaseSessionStore.updateData(secondSessionData.toApiModel())

        // Get the altered session
        val alteredSession = databaseSessionStore.getSession(firstSessionData.userId)!!.toDbModel()

        assertThat(alteredSession.userId).isEqualTo(secondSessionData.userId)
        assertThat(alteredSession.deviceId).isEqualTo(secondSessionData.deviceId)
        assertThat(alteredSession.accessToken).isEqualTo(secondSessionData.accessToken)
        assertThat(alteredSession.refreshToken).isEqualTo(secondSessionData.refreshToken)
        assertThat(alteredSession.homeserverUrl).isEqualTo(secondSessionData.homeserverUrl)
        assertThat(alteredSession.slidingSyncProxy).isEqualTo(secondSessionData.slidingSyncProxy)
        // Check that alteredSession.loginTimestamp is not altered, so equal to firstSessionData.loginTimestamp
        assertThat(alteredSession.loginTimestamp).isEqualTo(firstSessionData.loginTimestamp)
        assertThat(alteredSession.oidcData).isEqualTo(secondSessionData.oidcData)
    }
}
