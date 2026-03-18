# Ravel - Release Signing Setup

To enable signed release builds via CI, generate a keystore and add these GitHub repository secrets.

## Generate a Keystore (one-time)

```bash
keytool -genkey -v \
  -keystore ravel.keystore \
  -alias ravel \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Keep ravel.keystore safe — back it up somewhere secure. Do NOT commit it to the repo.
Add ravel.keystore to .gitignore.

## Encode Keystore for GitHub Secrets

```bash
base64 -i ravel.keystore | pbcopy
```

## GitHub Repository Secrets

Add at Settings -> Secrets and variables -> Actions:

| Secret | Value |
|---|---|
| RAVEL_KEYSTORE_BASE64 | Base64-encoded keystore file |
| RAVEL_KEY_ALIAS | Key alias (e.g. ravel) |
| RAVEL_KEY_PASSWORD | Key password |
| RAVEL_STORE_PASSWORD | Keystore store password |

## Obtainium Setup

Add https://github.com/zachatrocity/ravel to Obtainium.
Obtainium will watch GitHub Releases and auto-update when new APKs are attached.
