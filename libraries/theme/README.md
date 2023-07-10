# Theme Module

This module contains the theme tokens for the application, including those auto-generated from [Compound](https://github.com/vector-im/compound-design-tokens) and its mappings.

## Usage

The module contains public tokens and color schemes that are later used in `MaterialTheme` and added to `ElementTheme` for use in the application.

All tokens can be accessed through the `ElementTheme` object, which contains the following properties:

* `ElementTheme.materialColors`: contains all Material color tokens. In Figma, they're prefixed with `M3/`. It's an alias to `MaterialTheme.colorScheme`.
* `ElementTheme.colors`: contains all Compound semantic color tokens. In Figma, they're prefixed with either `Light/` or `Dark/`.
* `ElementTheme.materialTypography`: contains the Material `Typography` values. In Figma, they're prefixed with `M3/`. It's an alias to `MaterialTheme.typography`.
* `ElementTheme.typography`: contains the Compound `TypographyTokens` values. In Figma, they're prefixed with `Android/font/`.

## Adding new tokens

All new tokens **should** come from Compound and added to the `compound.generated` package. To map the literal tokens to the semantic ones, you'll have to update both `compoundColorsLight` and `compoundColorsDark` in `CompoundColors.kt`.
