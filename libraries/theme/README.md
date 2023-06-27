# Theme Module

This module contains the theme tokens for the application, including those auto-generated from [Compound](https://github.com/vector-im/compound-design-tokens) and its mappings.

## Usage

The module contains public tokens and color schemes that are later used in `MaterialTheme` and added to `ElementTheme` for use in the application.

## Adding new tokens

All new tokens **should** come from Compound and added to the `compound.generated` package. To map the literal tokens to the semantic ones, you'll have to update both `compoundColorsLight` and `compoundColorsDark` in `CompoundColors.kt`.

As we're still migrating to using Compound tokens, it's possible that you might need to add some tokens manually. In that case, you should add them to `LegacyColors.kt` and map them later in `ElementColors.kt` so they can be used in light and dark themes. However, keep in mind this is just a temporary step, as those tokens should either be added later to Compound or replaced by Compound tokens in the future.
