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

package io.element.android.libraries.matrix.impl.certificates

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import timber.log.Timber
import java.security.KeyStore
import java.security.KeyStoreException
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultUserCertificatesProvider @Inject constructor() : UserCertificatesProvider {
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
