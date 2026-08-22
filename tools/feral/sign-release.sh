#!/usr/bin/env bash
# Feral Android release signer — run on the SIGNING machine (eheyu), never in CI.
#
# Input : the UNSIGNED release APKs built by the "Feral release (unsigned)" GitHub
#         workflow (artifact feral-<ver>-release-unsigned, unzipped:
#         app-fdroid-<abi|universal>-release-unsigned.apk + SHA256SUMS + BUILD-INFO.txt).
#         "fdroid" is only Element's name for the no-Google variant (UnifiedPush),
#         nothing is published on F-Droid; a gplay (Firebase) APK is refused.
# Output: Feral-<ver>[-<abi>].apk signed with the Feral release keystore, in --out.
#         Then: tools/feral/publish-release.sh --version <ver> --apk-dir <out> …
#
# Keystore: read from signing.properties at the repo root (gitignored) or --props.
#   FERAL_RELEASE_STORE_FILE, FERAL_RELEASE_STORE_PASSWORD,
#   FERAL_RELEASE_KEY_ALIAS,  FERAL_RELEASE_KEY_PASSWORD
#   Plain KEY=value lines only (LF or CRLF) — no Java \-escapes, no ':' separators:
#   keep the file to that subset so Gradle (Properties.load) and this script agree.
#
# Safety:
#  - refuses to emit an APK whose signing certificate differs from the fingerprint
#    pinned in appconfig/AppUpdateConfig.kt (SIGNING_CERT_SHA256). An APK signed with
#    any other key would be rejected by every installed Feral AND by Android
#    (same-signer rule) — members would have to uninstall/reinstall;
#  - checks each APK really is feral.app at --version (aapt2), so a mislabelled
#    artifact cannot be published under the wrong name;
#  - checks the artifact was built from the commit checked out here (BUILD-INFO.txt),
#    so what was reviewed is what gets signed (--allow-commit-mismatch to override);
#  - output is atomic: Feral-*.apk only appears in --out once signed AND verified.
set -euo pipefail

usage() {
    cat >&2 <<'USAGE'
Usage: sign-release.sh --version <versionName> --in <unsigned-apk-dir> [--out <dir>]
                       [--props <signing.properties>] [--allow-commit-mismatch]
Example:
  unzip -d ~/feral-rel/26.08.0 ~/Downloads/feral-26.08.0-release-unsigned.zip
  ./tools/feral/sign-release.sh --version 26.08.0 --in ~/feral-rel/26.08.0
  ./tools/feral/publish-release.sh --version 26.08.0 --apk-dir ~/feral-rel/26.08.0/signed --deploy loic_feral@172.232.45.124
USAGE
    exit "${1:-1}"
}
need() { [ $# -ge 2 ] && [ -n "$2" ] || { echo "ERROR: $1 needs a value" >&2; usage; }; }

VERSION="" IN="" OUT="" PROPS="" ALLOW_MISMATCH=0
while [ $# -gt 0 ]; do
    case "$1" in
        --version) need "$@"; VERSION="$2"; shift 2 ;;
        --in) need "$@"; IN="$2"; shift 2 ;;
        --out) need "$@"; OUT="$2"; shift 2 ;;
        --props) need "$@"; PROPS="$2"; shift 2 ;;
        --allow-commit-mismatch) ALLOW_MISMATCH=1; shift ;;
        -h|--help) usage 0 ;;
        *) echo "ERROR: unknown argument '$1'" >&2; usage ;;
    esac
done
[ -n "$VERSION" ] && [ -n "$IN" ] || usage
[[ "$VERSION" =~ ^[0-9]{2}\.[0-9]{2}\.[0-9]{1,2}$ ]] \
    || { echo "ERROR: --version must look like YY.MM.N as in Versions.kt (got '$VERSION')" >&2; exit 1; }
[ -d "$IN" ] || { echo "ERROR: $IN is not a directory" >&2; exit 1; }
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
PROPS="${PROPS:-$ROOT/signing.properties}"
OUT="${OUT:-$IN/signed}"
[ -f "$PROPS" ] || { echo "ERROR: $PROPS not found (keystore properties)" >&2; exit 1; }

shopt -s nullglob
INPUTS=("$IN"/app-fdroid-*-release-unsigned.apk)
[ ${#INPUTS[@]} -gt 0 ] \
    || { echo "ERROR: no app-fdroid-*-release-unsigned.apk directly in $IN (a gplay/Firebase build is refused on purpose) (point --in at the unzipped artifact directory)" >&2; exit 1; }

# --- provenance: the artifact must come from the commit checked out here ------------
if [ -f "$IN/BUILD-INFO.txt" ] && git -C "$ROOT" rev-parse --verify HEAD >/dev/null 2>&1; then
    CI_COMMIT="$(sed -n 's/^commit: //p' "$IN/BUILD-INFO.txt" | head -1)"
    HEAD_COMMIT="$(git -C "$ROOT" rev-parse HEAD)"
    if [ -n "$CI_COMMIT" ] && [ "$CI_COMMIT" != "$HEAD_COMMIT" ]; then
        if [ "$ALLOW_MISMATCH" = 1 ]; then
            echo "WARNING: artifact built from $CI_COMMIT, checked-out tree is $HEAD_COMMIT (override accepted)" >&2
        else
            echo "ERROR: artifact built from $CI_COMMIT but the checked-out tree is $HEAD_COMMIT." >&2
            echo "       Check out that commit (git fetch && git checkout $CI_COMMIT) or pass --allow-commit-mismatch." >&2
            exit 1
        fi
    fi
else
    echo "WARNING: no BUILD-INFO.txt (or not a git checkout) — cannot check the artifact's commit" >&2
fi

# --- keystore properties (same keys as app/build.gradle.kts) ------------------------
prop() {
    local v
    v="$(tr -d '\r' < "$PROPS" | sed -n "s/^[[:space:]]*$1[[:space:]]*=[[:space:]]*//p" | tail -1)"
    case "$v" in *\\*)
        echo "ERROR: $1 in $PROPS contains a backslash; this script reads values literally (no Java escapes)" >&2; exit 1 ;;
    esac
    printf '%s' "$v"
}
STORE_FILE="$(prop FERAL_RELEASE_STORE_FILE)"
KEY_ALIAS="$(prop FERAL_RELEASE_KEY_ALIAS)"
FERAL_KS_PASS="$(prop FERAL_RELEASE_STORE_PASSWORD)"
FERAL_KEY_PASS="$(prop FERAL_RELEASE_KEY_PASSWORD)"
[ -n "$STORE_FILE" ] && [ -n "$KEY_ALIAS" ] && [ -n "$FERAL_KS_PASS" ] && [ -n "$FERAL_KEY_PASS" ] \
    || { echo "ERROR: incomplete $PROPS (need STORE_FILE/STORE_PASSWORD/KEY_ALIAS/KEY_PASSWORD)" >&2; exit 1; }
# Gradle resolves the store file relative to the repo root: do the same.
case "$STORE_FILE" in /*) ;; *) STORE_FILE="$ROOT/$STORE_FILE" ;; esac
[ -f "$STORE_FILE" ] || { echo "ERROR: keystore $STORE_FILE not found" >&2; exit 1; }

# --- pinned certificate fingerprint (the app only installs updates from this key) ---
CONFIG="$ROOT/appconfig/src/main/kotlin/io/element/android/appconfig/AppUpdateConfig.kt"
EXPECTED="$(sed -n 's/^[[:space:]]*"\([0-9a-f]\{64\}\)"[[:space:]]*$/\1/p' "$CONFIG" | head -1)"
[ -n "$EXPECTED" ] || { echo "ERROR: SIGNING_CERT_SHA256 not found in $CONFIG" >&2; exit 1; }

# --- Android build-tools (zipalign + apksigner + aapt2) ------------------------------
find_build_tools() {
    local sdk
    for sdk in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}" "$HOME/Android/Sdk" "$HOME/Library/Android/sdk" /opt/android-sdk; do
        [ -n "$sdk" ] && [ -d "$sdk/build-tools" ] || continue
        ls -d "$sdk"/build-tools/*/ 2>/dev/null | sort -V | tail -1 && return
    done
    return 1
}
BT="$(find_build_tools || true)"
if [ -n "$BT" ]; then
    ZIPALIGN="${BT}zipalign"; APKSIGNER="${BT}apksigner"; AAPT2="${BT}aapt2"
else
    ZIPALIGN="$(command -v zipalign || true)"; APKSIGNER="$(command -v apksigner || true)"; AAPT2="$(command -v aapt2 || true)"
fi
[ -x "$ZIPALIGN" ] && [ -x "$APKSIGNER" ] && [ -x "$AAPT2" ] \
    || { echo "ERROR: zipalign/apksigner/aapt2 not found (install Android SDK build-tools or export ANDROID_HOME)" >&2; exit 1; }

badge() {
    "$AAPT2" dump badging "$1" \
        | sed -n "s/^package: name='\([^']*\)' versionCode='\([0-9]*\)' versionName='\([^']*\)'.*/\1 \2 \3/p" \
        | head -1
}
apk_abi() {
    case "$(basename "$1")" in
        *arm64-v8a*) echo "arm64-v8a" ;;
        *armeabi-v7a*) echo "armeabi-v7a" ;;
        *x86_64*) echo "x86_64" ;;
        *x86*) echo "x86" ;;
        *) echo "" ;;   # universal
    esac
}

mkdir -p "$OUT"
WORK="$(mktemp -d -p "$OUT" .sign.XXXXXX)"
trap 'rm -rf "$WORK"' EXIT
export FERAL_KS_PASS FERAL_KEY_PASS

echo "Signing ${#INPUTS[@]} APK(s) with $STORE_FILE (alias $KEY_ALIAS)"
echo "Expected certificate SHA-256: $EXPECTED"
for f in "${INPUTS[@]}"; do
    name="$(basename "$f")"
    read -r pkg vcode vname <<<"$(badge "$f")"
    [ "$pkg" = "feral.app" ] || { echo "ERROR: $name is package '$pkg', expected feral.app" >&2; exit 1; }
    [ "$vname" = "$VERSION" ] || { echo "ERROR: $name is versionName '$vname', not --version $VERSION" >&2; exit 1; }
    abi="$(apk_abi "$f")"
    dest="$OUT/Feral-$VERSION${abi:+-$abi}.apk"
    aligned="$WORK/aligned.apk"; signed="$WORK/signed.apk"
    "$ZIPALIGN" -p -f 4 "$f" "$aligned"
    "$APKSIGNER" sign \
        --ks "$STORE_FILE" --ks-key-alias "$KEY_ALIAS" \
        --ks-pass env:FERAL_KS_PASS --key-pass env:FERAL_KEY_PASS \
        --v4-signing-enabled false \
        --out "$signed" "$aligned"
    actual="$("$APKSIGNER" verify --print-certs "$signed" \
        | sed -n 's/^Signer #1 certificate SHA-256 digest: \([0-9a-f]*\)$/\1/p' | head -1)"
    if [ "$actual" != "$EXPECTED" ]; then
        echo "ERROR: $name signed with the WRONG key (cert $actual). Wrong keystore/alias? Nothing written." >&2
        exit 1
    fi
    "$ZIPALIGN" -c -p 4 "$signed" >/dev/null || { echo "ERROR: $name is not zip-aligned after signing" >&2; exit 1; }
    mv -f "$signed" "$dest"
    rm -f "$aligned"
    echo "  OK  $(basename "$dest")  ${abi:-universal}  versionCode=$vcode  cert=${actual:0:16}…"
done
unset FERAL_KS_PASS FERAL_KEY_PASS

echo
echo "Signed APKs in $OUT — next:"
echo "  ./tools/feral/publish-release.sh --version $VERSION --apk-dir $OUT --changelog-fr \"…\" --changelog-en \"…\" --deploy loic_feral@172.232.45.124"
