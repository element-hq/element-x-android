# Migration to Metro

The dependency injection library is now [Metro](https://zacsweers.github.io/metro/latest/). It replaces both Dagger and Anvil.

Migration of the current Element X code has been performed in https://github.com/element-hq/element-x-android/pull/5253.

To migrate other existing code you will need to:

- replace `setupAnvil()` with `setupDependencyInjection()` in your `build.gradle.kts` files
- replace the Dagger and Anvil imports with Metro ones
- move the `@Inject` apply to the constructor to the class itself (only applicable if there is only one primary constructor
- replace `@AssistedInject` with `@Inject`
- replace `@Module` with `@BindingContainer`

This should help to migrate your existing code.
