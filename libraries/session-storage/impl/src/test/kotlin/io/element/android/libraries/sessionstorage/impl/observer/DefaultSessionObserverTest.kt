/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.sessionstorage.impl.observer

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.sessionstorage.impl.DatabaseSessionStore
import io.element.android.libraries.sessionstorage.impl.SessionDatabase
import io.element.android.libraries.sessionstorage.impl.aSessionData
import io.element.android.libraries.sessionstorage.impl.toApiModel
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class) class DefaultSessionObserverTest {
    private lateinit var database: SessionDatabase
    private lateinit var databaseSessionStore: DatabaseSessionStore

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
    fun `adding data invokes onSessionCreated`() = runTest {
        val sessionData = aSessionData()
        val sut = createDefaultSessionObserver()
        runCurrent()
        val listener = TestSessionListener()
        sut.addListener(listener)
        databaseSessionStore.storeData(sessionData.toApiModel())
        listener.assertEvents(TestSessionListener.Event.Created(sessionData.userId))
        sut.removeListener(listener)
        coroutineContext.cancelChildren()
    }

    @Test
    fun `adding and deleting data invokes onSessionCreated and onSessionDeleted`() = runTest {
        val sessionData = aSessionData()
        val sut = createDefaultSessionObserver()
        runCurrent()
        val listener = TestSessionListener()
        sut.addListener(listener)
        databaseSessionStore.storeData(sessionData.toApiModel())
        listener.assertEvents(TestSessionListener.Event.Created(sessionData.userId))
        databaseSessionStore.removeSession(sessionData.userId)
        listener.assertEvents(
            TestSessionListener.Event.Created(sessionData.userId),
            TestSessionListener.Event.Deleted(sessionData.userId),
        )
        coroutineContext.cancelChildren()
    }

    private fun TestScope.createDefaultSessionObserver(): DefaultSessionObserver {
        return DefaultSessionObserver(
            sessionStore = databaseSessionStore,
            coroutineScope = this,
            dispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true),
        )
    }
}
