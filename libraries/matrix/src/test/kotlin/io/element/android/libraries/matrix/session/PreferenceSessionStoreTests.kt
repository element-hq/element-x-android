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

package io.element.android.libraries.matrix.session

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.element.android.libraries.matrix.Database
import io.element.android.libraries.matrix.core.UserId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.matrix.rustcomponents.sdk.Session

@OptIn(ExperimentalCoroutinesApi::class)
class PreferenceSessionStoreTests {

    private lateinit var database: Database
    private lateinit var preferencesSessionStore: PreferencesSessionStore

    private val session = Session("accessToken", "refreshToken", "userId", "deviceId", "homeserverUrl", false)
    private val sessionData = with(session) {
        SessionData(userId, deviceId, accessToken, refreshToken, homeserverUrl, isSoftLogout)
    }

    @Before
    fun setup() {
        // Initialise in memory SQLite driver
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        Database.Schema.create(driver)

        database = Database(driver)
        preferencesSessionStore = PreferencesSessionStore(database)
    }

    @Test
    fun `storeData persists the SessionData into the DB`() = runTest {
        assertThat(database.sessionDataQueries.selectFirst().executeAsOneOrNull()).isNull()

        preferencesSessionStore.storeData(session)

        assertThat(database.sessionDataQueries.selectFirst().executeAsOneOrNull()).isEqualTo(sessionData)
    }

    @Test
    fun `isLoggedIn emits true while there are sessions in the DB`() = runTest {
        preferencesSessionStore.isLoggedIn().test {
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

        val latestSession = preferencesSessionStore.getLatestSession()

        assertThat(latestSession).isEqualTo(session)
    }

    @Test
    fun `getSession returns a matching session in DB if exists`() = runTest {
        database.sessionDataQueries.insertSessionData(sessionData)
        database.sessionDataQueries.insertSessionData(sessionData.copy(userId = "otherUserId"))

        val foundSession = preferencesSessionStore.getSession(UserId(sessionData.userId))

        assertThat(foundSession).isEqualTo(session)
    }

    @Test
    fun `getSession returns null if a no matching session exists in DB`() = runTest {
        database.sessionDataQueries.insertSessionData(sessionData.copy(userId = "otherUserId"))

        val foundSession = preferencesSessionStore.getSession(UserId(sessionData.userId))

        assertThat(foundSession).isNull()
    }

    @Test
    fun `reset removes all sessions in DB`() = runTest {
        database.sessionDataQueries.insertSessionData(sessionData)
        database.sessionDataQueries.insertSessionData(sessionData.copy(userId = "otherUserId"))

        preferencesSessionStore.reset()

        assertThat(database.sessionDataQueries.selectFirst().executeAsOneOrNull()).isNull()
    }

}
