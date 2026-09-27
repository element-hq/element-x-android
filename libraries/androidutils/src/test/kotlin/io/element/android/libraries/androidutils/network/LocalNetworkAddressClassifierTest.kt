/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.network

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.net.InetAddress

class LocalNetworkAddressClassifierTest {
    private fun classifier(
        resolver: DnsResolver = FakeDnsResolver(),
    ) = DefaultLocalNetworkAddressClassifier(resolver)

    @Test
    fun `IPv4 loopback literal is LocalIp`() = runTest {
        assertThat(classifier().classify("https://127.0.0.1:8008")).isEqualTo(LocalNetworkClassification.LocalIp)
    }

    @Test
    fun `IPv4 RFC1918 10 literal is LocalIp`() = runTest {
        assertThat(classifier().classify("https://10.0.0.5")).isEqualTo(LocalNetworkClassification.LocalIp)
    }

    @Test
    fun `IPv4 RFC1918 172_16 literal is LocalIp`() = runTest {
        assertThat(classifier().classify("https://172.20.1.2")).isEqualTo(LocalNetworkClassification.LocalIp)
    }

    @Test
    fun `IPv4 RFC1918 192_168 literal is LocalIp`() = runTest {
        assertThat(classifier().classify("https://192.168.1.10")).isEqualTo(LocalNetworkClassification.LocalIp)
    }

    @Test
    fun `IPv4 link local literal is LocalIp`() = runTest {
        assertThat(classifier().classify("https://169.254.10.20")).isEqualTo(LocalNetworkClassification.LocalIp)
    }

    @Test
    fun `IPv4 CGNAT 100_64_0_0_10 literal is LocalIp`() = runTest {
        assertThat(classifier().classify("https://100.100.0.1")).isEqualTo(LocalNetworkClassification.LocalIp)
    }

    @Test
    fun `IPv4 public literal is PublicIp`() = runTest {
        assertThat(classifier().classify("https://8.8.8.8")).isEqualTo(LocalNetworkClassification.PublicIp)
    }

    @Test
    fun `IPv6 loopback literal is LocalIp`() = runTest {
        assertThat(classifier().classify("https://[::1]")).isEqualTo(LocalNetworkClassification.LocalIp)
    }

    @Test
    fun `IPv6 link local literal is LocalIp`() = runTest {
        assertThat(classifier().classify("https://[fe80::1]")).isEqualTo(LocalNetworkClassification.LocalIp)
    }

    @Test
    fun `IPv6 unique local literal is LocalIp`() = runTest {
        assertThat(classifier().classify("https://[fc00::1]")).isEqualTo(LocalNetworkClassification.LocalIp)
    }

    @Test
    fun `IPv6 public literal is PublicIp`() = runTest {
        assertThat(classifier().classify("https://[2606:4700:4700::1111]")).isEqualTo(LocalNetworkClassification.PublicIp)
    }

    @Test
    fun `dot local mDNS name is LocalIp without DNS`() = runTest {
        assertThat(classifier().classify("https://matrix.local"))
            .isEqualTo(LocalNetworkClassification.LocalIp)
    }

    @Test
    fun `hostname resolving to public IP is PublicIp`() = runTest {
        val resolver = FakeDnsResolver(
            results = mapOf("matrix.org" to listOf(InetAddress.getByName("8.8.8.8")))
        )
        assertThat(classifier(resolver).classify("https://matrix.org"))
            .isEqualTo(LocalNetworkClassification.PublicIp)
    }

    @Test
    fun `hostname resolving to private IP is LocalIp`() = runTest {
        val resolver = FakeDnsResolver(
            results = mapOf("matrix.corp.internal" to listOf(InetAddress.getByName("10.0.0.5")))
        )
        assertThat(classifier(resolver).classify("https://matrix.corp.internal"))
            .isEqualTo(LocalNetworkClassification.LocalIp)
    }

    @Test
    fun `hostname with mixed public and private addresses is LocalIp`() = runTest {
        val resolver = FakeDnsResolver(
            results = mapOf(
                "matrix.example" to listOf(
                    InetAddress.getByName("8.8.8.8"),
                    InetAddress.getByName("10.0.0.5"),
                )
            )
        )
        assertThat(classifier(resolver).classify("https://matrix.example"))
            .isEqualTo(LocalNetworkClassification.LocalIp)
    }

    @Test
    fun `unresolvable hostname is Unresolvable`() = runTest {
        val resolver = FakeDnsResolver(throwOnUnknown = true)
        assertThat(classifier(resolver).classify("https://nonexistent.example"))
            .isEqualTo(LocalNetworkClassification.Unresolvable)
    }

    @Test
    fun `malformed URL is Unresolvable`() = runTest {
        assertThat(classifier().classify("not a url")).isEqualTo(LocalNetworkClassification.Unresolvable)
    }
}
