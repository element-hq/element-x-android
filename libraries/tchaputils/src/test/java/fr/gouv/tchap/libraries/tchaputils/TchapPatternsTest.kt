/*
 * MIT License
 *
 * Copyright (c) 2024. DINUM
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fr.gouv.tchap.libraries.tchaputils

import fr.gouv.tchap.libraries.tchaputils.TchapPatterns.getUserDomain
import fr.gouv.tchap.libraries.tchaputils.TchapPatterns.getUserName
import fr.gouv.tchap.libraries.tchaputils.TchapPatterns.toHomeserverDisplayName
import fr.gouv.tchap.libraries.tchaputils.TchapPatterns.toUserDisplayName
import org.junit.Assert.assertEquals
import org.junit.Test

class TchapPatternsTest {
    /**
     * Test getting full name without domain.
     */

    @Test
    fun `given a displayName if it contains brackets then return its first element after split`() {
        assertEquals("Nom Prenom", "Nom Prenom [Modernisation]".getUserName())
    }

    @Test
    fun `given a displayName if it doesn't contain brackets then return the original display name`() {
        assertEquals("Nom Prenom", "Nom Prenom ".getUserName())
    }

    /**
     * Test getting domain only.
     */

    @Test
    fun `given a displayName if it contains brackets then return domain name inside`() {
        assertEquals("Modernisation", "Nom Prenom [Modernisation]".getUserDomain())
    }

    @Test
    fun `given a displayName if it doesn't contain brackets then return empty string`() {
        assertEquals("", "Nom Prenom ".getUserDomain())
    }

    @Test
    fun `given a display name of a homeserver mentioned in a matrix identifier`() {
        assertEquals("A", "@jean-philippe.martin-modernisation.fr:a.tchap.gouv.fr".toHomeserverDisplayName())
    }

    @Test
    fun computeDisplayNameFromUserId_simple() {
        assertEquals("Jean Martin", "@jean.martin-modernisation.fr:matrix.org".toUserDisplayName())
    }

    @Test
    fun computeDisplayNameFromUserId_dash() {
        assertEquals("Jean-Philippe Martin", "@jean-philippe.martin-modernisation.fr:matrix.org".toUserDisplayName())
    }

    @Test
    fun computeDisplayNameFromUserId_dashes() {
        assertEquals("Jean Martin De-La-Rampe", "@jean.martin.de-la-rampe-modernisation.gouv.fr:a.tchap.gouv.fr".toUserDisplayName())
    }

    @Test
    fun computeDisplayNameFromUserId_emptydashes() {
        assertEquals("Jean Martin De-La-Rampe", "@jean..martin..de--la--rampe-modernisation.gouv.fr:a.tchap.gouv.fr".toUserDisplayName())
    }

    @Test
    fun computeDisplayNameFromUserId_dash_in_domain() {
        assertEquals("Jerome Ploquin4-Developpement", "@jerome.ploquin4-developpement-durable.gouv.fr:a.tchap.gouv.fr".toUserDisplayName())
    }

    @Test
    fun computeDisplayNameFromUserId_external_user() {
        assertEquals("jerome.ploquin@otherdomain.fr", "@jerome.ploquin-otherdomain.fr:agent.externe.gouv.fr".toUserDisplayName())
    }

    @Test
    fun computeDisplayNameFromUserId_external_user_dashes() {
        assertEquals("jean-philippe.martin-other-domain.fr", "@jean-philippe.martin-other-domain.fr:agent.externe.gouv.fr".toUserDisplayName())
    }
}
