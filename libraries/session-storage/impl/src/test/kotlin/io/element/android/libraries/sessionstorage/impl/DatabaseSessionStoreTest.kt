/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.impl

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.session.SessionData
import io.element.android.libraries.sessionstorage.api.LoggedInState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DatabaseSessionStoreTest {
    private lateinit var database: SessionDatabase
    private lateinit var databaseSessionStore: DatabaseSessionStore

    private val aSessionData = aSessionData()

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
    fun `getAllSessions should return all the sessions`() = runTest {
        val noSessions = databaseSessionStore.getAllSessions()
        assertThat(noSessions).isEmpty()
        database.sessionDataQueries.insertSessionData(aSessionData)
        val otherSessionData = aSessionData.copy(userId = "otherUserId")
        database.sessionDataQueries.insertSessionData(otherSessionData)
        val allSessions = databaseSessionStore.getAllSessions().map {
            it.toDbModel()
        }
        assertThat(allSessions).isEqualTo(
            listOf(
                aSessionData,
                otherSessionData,
            )
        )
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
            passphrase = "aPassphrase",
            sessionPath = "sessionPath",
            cachePath = "cachePath",
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
            passphrase = "aPassphraseAltered",
            sessionPath = "sessionPath",
            cachePath = "cachePath",
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
        assertThat(alteredSession.passphrase).isEqualTo(secondSessionData.passphrase)
    }

    @Test
    fun `update data, session not found`() = runTest {
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
            passphrase = "aPassphrase",
            sessionPath = "sessionPath",
            cachePath = "cachePath",
        )
        val secondSessionData = SessionData(
            userId = "userIdUnknown",
            deviceId = "deviceIdAltered",
            accessToken = "accessTokenAltered",
            refreshToken = "refreshTokenAltered",
            homeserverUrl = "homeserverUrlAltered",
            slidingSyncProxy = "slidingSyncProxyAltered",
            loginTimestamp = 2,
            oidcData = "aOidcDataAltered",
            isTokenValid = 1,
            loginType = null,
            passphrase = "aPassphraseAltered",
            sessionPath = "sessionPath",
            cachePath = "cachePath",
        )
        assertThat(firstSessionData.userId).isNotEqualTo(secondSessionData.userId)

        database.sessionDataQueries.insertSessionData(firstSessionData)
        databaseSessionStore.updateData(secondSessionData.toApiModel())

        // Get the session and check that it has not been altered
        val notAlteredSession = databaseSessionStore.getSession(firstSessionData.userId)!!.toDbModel()

        assertThat(notAlteredSession.userId).isEqualTo(firstSessionData.userId)
        assertThat(notAlteredSession.deviceId).isEqualTo(firstSessionData.deviceId)
        assertThat(notAlteredSession.accessToken).isEqualTo(firstSessionData.accessToken)
        assertThat(notAlteredSession.refreshToken).isEqualTo(firstSessionData.refreshToken)
        assertThat(notAlteredSession.homeserverUrl).isEqualTo(firstSessionData.homeserverUrl)
        assertThat(notAlteredSession.slidingSyncProxy).isEqualTo(firstSessionData.slidingSyncProxy)
        assertThat(notAlteredSession.loginTimestamp).isEqualTo(firstSessionData.loginTimestamp)
        assertThat(notAlteredSession.oidcData).isEqualTo(firstSessionData.oidcData)
        assertThat(notAlteredSession.passphrase).isEqualTo(firstSessionData.passphrase)
    }
}
