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

package io.element.android.libraries.sessionstorage

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.element.android.libraries.matrix.session.SessionData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DatabaseSessionStoreTests {

    private lateinit var database: SessionDatabase
    private lateinit var databaseSessionStore: DatabaseSessionStore

    private val sessionData = SessionData("userId", "deviceId", "accessToken", "refreshToken", "homeserverUrl", false)

    @Before
    fun setup() {
        // Initialise in memory SQLite driver
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SessionDatabase.Schema.create(driver)

        database = SessionDatabase(driver)
        databaseSessionStore = DatabaseSessionStore(database)
    }

    @Test
    fun `storeData persists the SessionData into the DB`() = runTest {
        assertThat(database.sessionDataQueries.selectFirst().executeAsOneOrNull()).isNull()

        databaseSessionStore.storeData(sessionData)

        assertThat(database.sessionDataQueries.selectFirst().executeAsOneOrNull()).isEqualTo(sessionData)
    }

    @Test
    fun `isLoggedIn emits true while there are sessions in the DB`() = runTest {
        databaseSessionStore.isLoggedIn().test {
            assertThat(awaitItem()).isFalse()
            database.sessionDataQueries.insertSessionData(sessionData)
            assertThat(awaitItem()).isTrue()
            database.sessionDataQueries.removeAll()
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `getLatestSession gets the first session in the DB`() = runTest {
        database.sessionDataQueries.insertSessionData(sessionData)
        database.sessionDataQueries.insertSessionData(sessionData.copy(userId = "otherUserId"))

        val latestSession = databaseSessionStore.getLatestSession()

        assertThat(latestSession).isEqualTo(sessionData)
    }

    @Test
    fun `getSession returns a matching session in DB if exists`() = runTest {
        database.sessionDataQueries.insertSessionData(sessionData)
        database.sessionDataQueries.insertSessionData(sessionData.copy(userId = "otherUserId"))

        val foundSession = databaseSessionStore.getSession(SessionId(sessionData.userId))

        assertThat(foundSession).isEqualTo(sessionData)
    }

    @Test
    fun `getSession returns null if a no matching session exists in DB`() = runTest {
        database.sessionDataQueries.insertSessionData(sessionData.copy(userId = "otherUserId"))

        val foundSession = databaseSessionStore.getSession(SessionId(sessionData.userId))

        assertThat(foundSession).isNull()
    }

    @Test
    fun `reset removes all sessions in DB`() = runTest {
        database.sessionDataQueries.insertSessionData(sessionData)
        database.sessionDataQueries.insertSessionData(sessionData.copy(userId = "otherUserId"))

        databaseSessionStore.reset()

        assertThat(database.sessionDataQueries.selectFirst().executeAsOneOrNull()).isNull()
    }

}
