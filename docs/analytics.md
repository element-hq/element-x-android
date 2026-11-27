# Analytics in Element

<!--- TOC -->

* [Sentry](#sentry)
  * [Upload of the R8 mapping file](#upload-of-the-r8-mapping-file)

<!--- END -->

## Sentry

To make Sentry analytics and bug reporting work, you need to provide a Sentry DSN in the `local.properties` file, or set the `ELEMENT_ANDROID_SENTRY_DSN` environment variable.

The format used to add the DSN to your `local.properties` file is the following:

```properties
services.analyticsproviders.sentry.dsn=https://your-sentry-dsn/project-id
```

### Upload of the R8 mapping file

The GPlay release and nightly builds are obfuscated by R8. To be able to retrace the stack traces received by Sentry, the R8 mapping file of the build is uploaded to the Sentry project `element-x-android` by the [Sentry Gradle plugin](https://docs.sentry.io/platforms/android/configuration/gradle/). The plugin is only applied when a Sentry auth token is provided, so by default local builds do not upload anything.

The token is read from the `SENTRY_AUTH_TOKEN` environment variable (this is what the release and nightly workflows of the CI do), or from the Gradle property `services.analyticsproviders.sentry.authToken`. To set the Gradle property, add this line to your user-level `~/.gradle/gradle.properties` file:

```properties
services.analyticsproviders.sentry.authToken=your-sentry-auth-token
```

Notes:
- Never add the token to the `gradle.properties` file of the project, which is committed.
- When the token is set, every GPlay release and nightly build that you run locally uploads its mapping file to Sentry.
