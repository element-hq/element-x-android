# Use of maps

<!--- TOC -->

* [Overview](#overview)
* [Local development with MapTiler](#local-development-with-maptiler)
* [Making releasable builds with MapTiler](#making-releasable-builds-with-maptiler)
* [Using other map sources or MapTiler styles](#using-other-map-sources-or-maptiler-styles)

<!--- END -->

## Overview

Element Android uses [MapTiler](https://www.maptiler.com/) to provide map
imagery where required. MapTiler requires an API key, which we bake in to
the app at release time.

## Local development with MapTiler

If you're developing the application and want maps to render properly you can
sign up for the [MapTiler free tier](https://www.maptiler.com/cloud/pricing/).

Place your API key in `local.properties` with the key
`services.maptiler.apikey`, e.g.:

```properties
services.maptiler.apikey=abCd3fGhijK1mN0pQr5t
```

Optionally you can also place your custom MapTyler style ids for light and dark maps
in the `local.properties` with the keys `services.maptiler.lightMapId` and
`services.maptiler.darkMapId`. If you don't specify these, the default MapTiler "basic-v2"
styles will be used.

## Making releasable builds with MapTiler

To insert the MapTiler API key when building an APK, set the
`ELEMENT_ANDROID_MAPTILER_API_KEY` environment variable in your build
environment.
If you've added custom styles also set the `ELEMENT_ANDROID_MAPTILER_LIGHT_MAP_ID`
and `ELEMENT_ANDROID_MAPTILER_DARK_MAP_ID` environment variables accordingly.

## Using other map sources or MapTiler styles

If you wish to use an alternative map provider, you can provide your own implementations of
`TileServerStyleUriBuilder` and `StaticMapUrlBuilder` in
`features/location/api/src/main/kotlin/io/element/android/features/location/api/internal/`.
