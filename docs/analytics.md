# Analytics in Element

<!--- TOC -->

* [Sentry](#sentry)

<!--- END -->

## Sentry

To make Sentry analytics and bug reporting work, you need to provide a Sentry DSN in the `local.properties` file, or set the `ELEMENT_ANDROID_SENTRY_DSN` environment variable.

The format used to add the DSN to your `local.properties` file is the following:

```properties
services.analyticsproviders.sentry.dsn=https://your-sentry-dsn/project-id
```
