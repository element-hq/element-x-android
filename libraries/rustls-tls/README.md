This module is a wrapper for the Android code distributed in the rustls-platform-verifier-android crate.

To avoid the distribution mess that this library has (download a Rust crate, then search for it using Gradle and use it as local maven repo),
we previously just manually updated the AAR file instead using a script. This won't work for F-Droid because the AAR library is a black box with
no sources attached to it, so we can't use it like that.

Instead, for the time being, we're adding the single `CertificateVerifier.kt` class this AAR had in it as part of our sources.

When this file is updated, the [UPDATED.md](./UPDATED.md) file should be updated too with the commit SHA of the new version.
