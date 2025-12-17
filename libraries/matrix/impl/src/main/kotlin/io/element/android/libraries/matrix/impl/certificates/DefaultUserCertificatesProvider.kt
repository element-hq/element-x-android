/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.certificates

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import timber.log.Timber
import java.security.KeyStore
import java.security.KeyStoreException

@ContributesBinding(AppScope::class)
class DefaultUserCertificatesProvider : UserCertificatesProvider {
    /**
     * Get additional user-installed certificates from the `AndroidCAStore` `Keystore`.
     *
     * The Rust HTTP client doesn't include user-installed certificates in its internal certificate
     * store. This means that whatever the user installs will be ignored.
     *
     * While most users don't need user-installed certificates some special deployments or debugging
     * setups using a proxy might want to use them.
     *
     * @return A list of byte arrays where each byte array is a single user-installed certificate
     *         in encoded form.
     */
    override fun provides(): List<ByteArray> {
        // At least for API 34 the `AndroidCAStore` `Keystore` type contained user certificates as well.
        // I have not found this to be documented anywhere.
        val keyStore: KeyStore = try {
            KeyStore.getInstance("AndroidCAStore")
        } catch (e: KeyStoreException) {
            Timber.w(e, "Failed to get AndroidCAStore keystore")
            return emptyList()
        }
        val aliases = try {
            keyStore.load(null)
            keyStore.aliases()
        } catch (e: Exception) {
            Timber.w(e, "Failed to load and get aliases AndroidCAStore keystore")
            return emptyList()
        }
        return aliases.toList()
            .filter { alias ->
                // The certificate alias always contains the prefix `system` or
                // `user` and the MD5 subject hash separated by a colon.
                //
                // The subject hash can be calculated using openssl as such:
                //     openssl x509 -subject_hash_old -noout -in mycert.cer
                //
                // Again, I have not found this to be documented somewhere.
                alias.startsWith("user")
            }
            .mapNotNull { alias ->
                try {
                    keyStore.getEntry(alias, null)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to get entry for alias $alias")
                    null
                }
            }
            .filterIsInstance<KeyStore.TrustedCertificateEntry>()
            .map { trustedCertificateEntry ->
                trustedCertificateEntry.trustedCertificate.encoded
            }
            .also {
                // Let's at least log the number of user-installed certificates we found,
                // since the alias isn't particularly useful nor does the issuer seem to
                // be easily available.
                Timber.i("Found ${it.size} additional user-provided certificates.")
            }
    }
}
