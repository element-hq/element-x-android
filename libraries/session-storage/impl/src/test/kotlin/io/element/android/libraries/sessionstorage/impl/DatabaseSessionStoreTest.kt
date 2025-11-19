/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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

class DatabaseSessionStoreTest {
    private lateinit var database: SessionDatabase
    private lateinit var databaseSessionStore: DatabaseSessionStore

    private val aSessionData = aDbSessionData()

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
    fun `addSession persists the SessionData into the DB`() = runTest {
        assertThat(database.sessionDataQueries.selectLatest().executeAsOneOrNull()).isNull()

        databaseSessionStore.addSession(aSessionData.toApiModel())

        assertThat(database.sessionDataQueries.selectLatest().executeAsOneOrNull()).isEqualTo(aSessionData)
        assertThat(database.sessionDataQueries.selectAll().executeAsList().size).isEqualTo(1)
        assertThat(database.sessionDataQueries.count().executeAsOneOrNull()).isEqualTo(1)
    }

    @Test
    fun `loggedInStateFlow emits LoggedIn while there are sessions in the DB`() = runTest {
        databaseSessionStore.loggedInStateFlow().test {
            assertThat(awaitItem()).isEqualTo(LoggedInState.NotLoggedIn)
            databaseSessionStore.addSession(aSessionData.toApiModel())
            assertThat(awaitItem()).isEqualTo(LoggedInState.LoggedIn(sessionId = aSessionData.userId, isTokenValid = true))
            // Add a second session
            databaseSessionStore.addSession(aSessionData.copy(userId = "otherUserId").toApiModel())
            assertThat(awaitItem()).isEqualTo(LoggedInState.LoggedIn(sessionId = "otherUserId", isTokenValid = true))
            // Remove the second session
            databaseSessionStore.removeSession("otherUserId")
            assertThat(awaitItem()).isEqualTo(LoggedInState.LoggedIn(sessionId = aSessionData.userId, isTokenValid = true))
            // Remove the first session
            databaseSessionStore.removeSession(aSessionData.userId)
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
        assertThat(database.sessionDataQueries.count().executeAsOneOrNull()).isEqualTo(2)
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
    fun `updateUserProfile does nothing if the session is not found`() = runTest {
        databaseSessionStore.updateUserProfile(aSessionData.userId, "userDisplayName", "userAvatarUrl")
        assertThat(database.sessionDataQueries.selectByUserId(aSessionData.userId).executeAsOneOrNull()).isNull()
    }

    @Test
    fun `updateUserProfile update the data`() = runTest {
        database.sessionDataQueries.insertSessionData(aSessionData)
        databaseSessionStore.updateUserProfile(aSessionData.userId, "userDisplayName", "userAvatarUrl")
        val updatedSession = database.sessionDataQueries.selectByUserId(aSessionData.userId).executeAsOne()
        assertThat(updatedSession.userDisplayName).isEqualTo("userDisplayName")
        assertThat(updatedSession.userAvatarUrl).isEqualTo("userAvatarUrl")
    }

    @Test
    fun `setLatestSession is no op when the session is already the latest session`() = runTest {
        database.sessionDataQueries.insertSessionData(aSessionData)
        val session = database.sessionDataQueries.selectByUserId(aSessionData.userId).executeAsOne()
        assertThat(session.lastUsageIndex).isEqualTo(0)
        assertThat(session.position).isEqualTo(0)
        databaseSessionStore.setLatestSession(aSessionData.userId)
        assertThat(database.sessionDataQueries.selectByUserId(aSessionData.userId).executeAsOne().lastUsageIndex).isEqualTo(0)
    }

    @Test
    fun `setLatestSession is no op when the session is not found`() = runTest {
        databaseSessionStore.setLatestSession(aSessionData.userId)
    }

    @Test
    fun `multi session test`() = runTest {
        databaseSessionStore.addSession(aSessionData.toApiModel())
        val session = databaseSessionStore.getSession(aSessionData.userId)!!
        assertThat(session.lastUsageIndex).isEqualTo(0)
        assertThat(session.position).isEqualTo(0)
        val secondSessionData = aSessionData.copy(
            userId = "otherUserId",
            position = 1,
            lastUsageIndex = 1,
        )
        databaseSessionStore.addSession(secondSessionData.toApiModel())
        val secondSession = database.sessionDataQueries.selectByUserId(secondSessionData.userId).executeAsOne()
        assertThat(secondSession.lastUsageIndex).isEqualTo(1)
        assertThat(secondSession.position).isEqualTo(1)
        // Set the first session as the latest
        databaseSessionStore.setLatestSession(aSessionData.userId)
        val firstSession = database.sessionDataQueries.selectByUserId(aSessionData.userId).executeAsOne()
        assertThat(firstSession.lastUsageIndex).isEqualTo(2)
        assertThat(firstSession.position).isEqualTo(0)
        // Check that the second session has not been altered
        val secondSession2 = database.sessionDataQueries.selectByUserId(secondSessionData.userId).executeAsOne()
        assertThat(secondSession2.lastUsageIndex).isEqualTo(1)
        assertThat(secondSession2.position).isEqualTo(1)
    }

    @Test
    fun `test sessionsFlow()`() = runTest {
        databaseSessionStore.sessionsFlow().test {
            assertThat(awaitItem()).isEmpty()
            databaseSessionStore.addSession(aSessionData.toApiModel())
            assertThat(awaitItem().size).isEqualTo(1)
            val secondSessionData = aSessionData.copy(
                userId = "otherUserId",
                position = 1,
                lastUsageIndex = 1,
            )
            assertThat(database.sessionDataQueries.count().executeAsOneOrNull()).isEqualTo(1)
            databaseSessionStore.addSession(secondSessionData.toApiModel())
            assertThat(awaitItem().size).isEqualTo(2)
            assertThat(database.sessionDataQueries.count().executeAsOneOrNull()).isEqualTo(2)
            databaseSessionStore.removeSession(aSessionData.userId)
            assertThat(awaitItem().size).isEqualTo(1)
            assertThat(database.sessionDataQueries.count().executeAsOneOrNull()).isEqualTo(1)
            databaseSessionStore.removeSession(secondSessionData.userId)
            assertThat(awaitItem()).isEmpty()
            assertThat(database.sessionDataQueries.count().executeAsOneOrNull()).isEqualTo(0)
        }
    }

    @Test
    fun `update session update all fields except info used by the application`() = runTest {
        val firstSessionData = SessionData(
            userId = "userId",
            deviceId = "deviceId",
            accessToken = "accessToken",
            refreshToken = "refreshToken",
            homeserverUrl = "homeserverUrl",
            loginTimestamp = 1,
            oidcData = "aOidcData",
            isTokenValid = 1,
            loginType = null,
            passphrase = "aPassphrase",
            sessionPath = "sessionPath",
            cachePath = "cachePath",
            position = 0,
            lastUsageIndex = 0,
            userDisplayName = "userDisplayName",
            userAvatarUrl = "userAvatarUrl",
        )
        val secondSessionData = SessionData(
            userId = "userId",
            deviceId = "deviceIdAltered",
            accessToken = "accessTokenAltered",
            refreshToken = "refreshTokenAltered",
            homeserverUrl = "homeserverUrlAltered",
            loginTimestamp = 2,
            oidcData = "aOidcDataAltered",
            isTokenValid = 1,
            loginType = null,
            passphrase = "aPassphraseAltered",
            sessionPath = "sessionPathAltered",
            cachePath = "cachePathAltered",
            position = 1,
            lastUsageIndex = 1,
            userDisplayName = "userDisplayNameAltered",
            userAvatarUrl = "userAvatarUrlAltered",
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
        // Check that alteredSession.loginTimestamp is not altered, so equal to firstSessionData.loginTimestamp
        assertThat(alteredSession.loginTimestamp).isEqualTo(firstSessionData.loginTimestamp)
        assertThat(alteredSession.oidcData).isEqualTo(secondSessionData.oidcData)
        assertThat(alteredSession.passphrase).isEqualTo(secondSessionData.passphrase)
        // Check that application data have not been altered
        assertThat(alteredSession.position).isEqualTo(firstSessionData.position)
        assertThat(alteredSession.lastUsageIndex).isEqualTo(firstSessionData.lastUsageIndex)
        assertThat(alteredSession.userDisplayName).isEqualTo(firstSessionData.userDisplayName)
        assertThat(alteredSession.userAvatarUrl).isEqualTo(firstSessionData.userAvatarUrl)
    }

    @Test
    fun `update data, session not found`() = runTest {
        val firstSessionData = SessionData(
            userId = "userId",
            deviceId = "deviceId",
            accessToken = "accessToken",
            refreshToken = "refreshToken",
            homeserverUrl = "homeserverUrl",
            loginTimestamp = 1,
            oidcData = "aOidcData",
            isTokenValid = 1,
            loginType = LoginType.PASSWORD.name,
            passphrase = "aPassphrase",
            sessionPath = "sessionPath",
            cachePath = "cachePath",
            position = 0,
            lastUsageIndex = 0,
            userDisplayName = "userDisplayName",
            userAvatarUrl = "userAvatarUrl",
        )
        val secondSessionData = SessionData(
            userId = "userIdUnknown",
            deviceId = "deviceIdAltered",
            accessToken = "accessTokenAltered",
            refreshToken = "refreshTokenAltered",
            homeserverUrl = "homeserverUrlAltered",
            loginTimestamp = 2,
            oidcData = "aOidcDataAltered",
            isTokenValid = 1,
            loginType = LoginType.PASSWORD.name,
            passphrase = "aPassphraseAltered",
            sessionPath = "sessionPathAltered",
            cachePath = "cachePathAltered",
            position = 1,
            lastUsageIndex = 1,
            userDisplayName = "userDisplayNameAltered",
            userAvatarUrl = "userAvatarUrlAltered",
        )
        assertThat(firstSessionData.userId).isNotEqualTo(secondSessionData.userId)

        database.sessionDataQueries.insertSessionData(firstSessionData)
        databaseSessionStore.updateData(secondSessionData.toApiModel())

        // Get the session and check that it has not been altered
        val notAlteredSession = databaseSessionStore.getSession(firstSessionData.userId)!!.toDbModel()

        assertThat(notAlteredSession).isEqualTo(firstSessionData)
    }
}
