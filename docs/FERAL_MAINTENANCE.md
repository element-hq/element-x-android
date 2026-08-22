# Maintenance du fork Feral (Android)

Référentiel de maintenance de `jeheja/feral-android`, fork de
`element-hq/element-x-android`. But : **rester synchronisé avec l'upstream en
quasi-temps réel, sans perdre les customisations Feral, sans sacrifier la
sécurité.** Les builds manuels (signature) sont assumés.

> Ce document fait autorité. Il **remplace** le système `patches/` +
> `scripts/apply-feral-patches.sh` (obsolète et contradictoire avec le workflow
> par commits directs) — voir §7.

---

## 1. Ce qui n'allait pas (constat)

- **L'override members-only était du code mort, jamais compilé.** Il vivait dans
  `features/enterprise/impl/FeralEnterpriseService.kt`, un dossier **sans
  `build.gradle.kts`**. Or `settings.gradle.kts` n'inclut un projet que s'il a un
  fichier de build → ce dossier n'était **pas** un module Gradle et n'était donc
  **jamais compilé**. Il traînait en plus un import Anvil périmé (upstream a migré
  **Anvil → Metro**). Upstream n'a pas ce dossier (seulement `api`, `impl-foss`,
  `test`). _(À noter : cela signifie que `develop` compilait — le fichier cassé
  n'était pas dans la chaîne de build ; le vrai problème est le verrou inactif
  ci-dessous.)_
- **Le verrou members-only était inactif dans l'APK public.** Le build FOSS
  compile `features/enterprise/impl-foss` (car
  `isEnterpriseBuild = File("enterprise/README.md").exists()` = `false`), dont le
  `DefaultEnterpriseService` renvoyait `defaultHomeserverList() = emptyList()` +
  `isAllowedToConnectToHomeserver() = true`. L'override Feral vivait dans l'autre
  module, jamais compilé → **l'app acceptait n'importe quel homeserver**.
- **Un sync upstream passé (merge de ~2916 commits) avait silencieusement
  reverté** l'override. C'est le mode d'échec classique du merge géant : il noie
  une régression *sémantique* dans un diff illisible.

## 2. Ce que corrige cette branche

- Suppression du dossier mort `features/enterprise/impl` (jamais compilé).
- Nouvel `features/enterprise/impl-foss/…/FeralEnterpriseService.kt` :
  `@ContributesBinding(AppScope::class, replaces = [DefaultEnterpriseService::class])`
  (Metro), qui restaure la liste d'homeservers Feral **dans le module réellement
  compilé**. Upstream n'édite jamais ce fichier → un sync ne peut plus le reverter
  en silence.
- **Test garde-fou** `FeralEnterpriseServiceTest` : `canConnectToAnyHomeserver()`
  doit rester `false`, la liste doit contenir `https://feralisme.fr` et rejeter
  les serveurs tiers. Il tourne en CI (§5) et casse le build **avant** qu'un APK
  « n'importe quel homeserver » puisse être signé.
- ✅ Vérifié (2026-08-21) : `feralism.net` ne sert **pas** Matrix (404 sur
  `/_matrix/client/versions`) → liste réduite à `feralisme.fr` seul,
  `feralism.net` laissé en commentaire pour une éventuelle activation future.

## 3. Modèle de branches

Trois remotes, une pile de customisation **mince** :

- `origin` = `jeheja/feral-android` (le fork).
- `upstream` = `github.com/element-hq/element-x-android` (lecture seule) :
  `git remote add upstream https://github.com/element-hq/element-x-android.git`.
- `element-call-embedded` vient de **Maven Central**, pas d'un remote source
  (aucun fork EC — voir §6).

Branches :
- **`main`** = la branche de release : **dernier tag upstream stable + pile Feral**,
  historique **linéaire** (depuis le sync v26.08.2 ; `git describe --tags --match 'v[0-9]*'`
  donne le tag de base). Elle n'est jamais *mergée* : on la **re-pointe** après chaque
  sync (`git push --force-with-lease origin feral/sync/<tag>:main`).
- `develop` = ancien historique (fork + merge géant de 2026-03), conservé pour
  référence (tag `archive/develop-before-v26.08.2-sync`), plus jamais buildé.
- `feral/sync/vYY.MM.N` : branche jetable ouverte par l'automatisation (ou à la main)
  à chaque sync ; CI Feral dessus ; devient `main` après revue.

## 4. Suivre l'upstream : rebase-onto-tag, depuis les TAGS

**Pourquoi rebase et pas merge :** le rebase force à re-présenter *chaque* commit
Feral contre le nouvel upstream → un revert silencieux d'une customisation
devient visible. Le merge géant, lui, le cache.

**Pourquoi les tags stables (`vYY.MM.N`) et pas `develop` :** pour un mainteneur
unique, un tag testé vaut mieux que `develop` mouvant. Cadence upstream ≈ un tag
toutes les ~2 semaines (CalVer, `vYY.MM.N` ; les `-rc.N` sont ignorés).

Boucle de sync :
```
git fetch upstream --tags
git checkout -b feral/sync/vYY.MM.N main
BASE=$(git describe --tags --abbrev=0 --match 'v[0-9]*' --exclude '*-*')   # = registre §12
git rebase --onto vYY.MM.N "$BASE"
# résoudre les conflits (surtout features/login onboarding) ; `git rerere` rejoue les anciennes résolutions
git diff "$BASE" vYY.MM.N -- features/enterprise/api   # nouveau membre EnterpriseService qui conditionne l'accès ? -> surcharger + test
./gradlew :features:enterprise:impl-foss:testDebugUnitTest :features:appupdate:impl:testDebugUnitTest   # garde-fous
./gradlew :app:assembleFdroidDebug                            # compile + graphe Metro
git push origin feral/sync/vYY.MM.N                          # CI Feral ; revue
git push --force-with-lease origin feral/sync/vYY.MM.N:main  # re-pointer main (jamais le bouton merge)
# puis : mettre à jour §12, tag feral-v<ver> -> feral-release.yml (§11)
```
L'automatisation (`feral-upstream-sync.yml`, §5) fait exactement ce rebase deux fois
par semaine et ouvre une PR (rebase propre) ou une issue (conflits).
Activer **`git rerere`** (`git config rerere.enabled true`) pour rejouer
automatiquement les résolutions récurrentes.

### Amincir le fork (priorité)

La divergence se concentre aujourd'hui sur les fichiers upstream les plus
mouvants — `OnBoardingView.kt` (touché par 7 commits Feral), les 5 presenters
`account-provider`, `SuperButton.kt`. **Chaque rebase retombe dessus.** Objectif :
sortir les customisations du code cœur vers des points d'extension conçus pour ça.

| Customisation | Aujourd'hui (fragile) | Cible (résistant au rebase) |
|---|---|---|
| App id / nom | édition de `BuildTimeConfig` + `appconfig` | idem (déjà central), OK à garder |
| Serveur / members-only | ✅ `FeralEnterpriseService` (impl-foss, Metro `replaces`) | conserver ce pattern |
| Branding visuel (logo, couleurs, thème) | patchs dans `OnBoardingView.kt`, `SuperButton.kt` | un `productFlavor` Feral + overrides `res/` |
| Notice members-only i18n | 1 clé ajoutée dans les `translations.xml` Localazy régénérés | ressource **Feral-owned** hors des fichiers Localazy |

Cible : ~4 commits atomiques, sur des fichiers que **Feral possède**, hors des
fichiers churny upstream.

## 5. Automatisation (« toujours à jour »)

« Quasi-temps réel » réaliste = **détection + PR + build auto** en quelques heures
d'un tag upstream ; **revue + signature = manuelles** (ralentissement délibéré,
assumé). Pas de ship automatique.

- **`.github/workflows/feral-ci.yml`** — sur chaque push/PR `feral/**` : build
  d'un **APK FOSS debug NON signé** (`assembleFdroidDebug`, valide aussi le graphe
  Metro) + le **test garde-fou**. Zéro secret, zéro keystore. C'est le vérificateur
  de compilation.
- **`.github/workflows/feral-release.yml`** — manuel (`Run workflow`) ou tag
  `feral-v<ver>` (= versionName de `Versions.kt`, vérifié) : garde-fou members-only
  puis build R8 **release `fdroid` (sans Google) NON signé** (4 ABI + universel),
  artefact `feral-<ver>-release-unsigned` avec `SHA256SUMS` + `BUILD-INFO.txt` (commit,
  versionCode par APK). Refuse de tourner si `signing.properties` existe. Signature
  sur eheyu — voir §11.
- **`.github/workflows/feral-upstream-sync.yml`** — planifié : détecte le dernier
  tag stable upstream et ouvre une **PR de sync** (rebase propre) ou une **issue**
  (conflits → rebase manuel). Ne ship jamais rien. Le lancer d'abord à la main
  (`Run workflow`) avant de compter sur le cron.
- **Renovate** — `.github/renovate.json5` (fichier upstream, converti en JSON5 par
  upstream — la règle Feral `element-call-embedded` y est reportée en dernière
  `packageRule` ; Dependabot est
  neutralisé via `open-pull-requests-limit: 0`, ne pas le réactiver). Il suffit
  d'**activer l'app Renovate** sur `jeheja/feral-android`. `element-call-embedded`
  est marqué **review-required** (pas d'auto-merge) — cf. §6.
- Actions GitHub **épinglées par SHA** depuis le sync v26.08.2 (mêmes SHA qu'upstream) ;
  `quality.yml` upstream fait tourner **zizmor** sur chaque PR, les `feral-*.yml`
  respectent `permissions: {}` / `persist-credentials: false` (sauf le job de sync,
  qui doit pousser).

## 6. Element Call : jamais un merge de source

**Aucun fork d'Element Call.** Les 3 clients consomment EC en boîte noire :
- **Android** : artefact Maven Central `io.element.android:element-call-embedded`
  (unique conso : `features/call/impl`). MaJ = **bump d'une ligne** dans
  `gradle/libs.versions.toml` + rebuild. **Ne jamais bumper en standalone** :
  laisser le merge du tag upstream porter la version EC qu'il embarque et teste.
  Un bump Renovate isolé reste review-required (risque d'incompat widget/SDK).
- **Web déployé** (`/opt/feral-source`) : widget distant `call.element.io`. À
  terme, **self-héberger la SPA EC** (Feral a déjà LiveKit + lk-jwt) pour ne pas
  fuiter les métadonnées d'appel vers Element.
- **Desktop** (`~/feral-build`) : bundle npm `@element-hq/element-call-embedded`.

⚠️ **Dérive de versions à aligner** : Android `0.23.0` (= upstream v26.08.2 ; le fork
était à `0.24.0`, voir §12), desktop `0.21.0`, web =
ce que sert `call.element.io`. Vérifier aussi que le widget d'appel Android pointe
sur **le LiveKit de Feral**, pas sur `call.element.io` par défaut.

Supply-chain : activer la **dependency verification Gradle**
(`verification-metadata.xml`, sha256 + PGP) pour cet AAR et les autres deps.

## 7. Consolidation

Deux systèmes de customisation coexistaient et se contredisaient :
- `patches/` (10 `.patch`) + `scripts/apply-feral-patches.sh`,
- les commits directs (que `FERAL_CUSTOMIZATION.md` déclarait canoniques).

**Décision : un seul système = commits directs (branche Feral).** Le dossier
`patches/` est à retirer (il n'est pas appliqué par le build et induit en erreur).
Non supprimé par cette branche pour ne pas détruire d'historique sans validation :
```
git rm -r patches scripts/apply-feral-patches.sh
```
Fait au sync v26.08.2 : `patches/` et `apply-feral-patches.sh` n'existaient déjà
plus ; `scripts/remove-feral-patches.sh` (miroir du système de patches, liste de
fichiers périmée) a été abandonné. Revenir à Element stock = `git checkout <tag-de-base>`.

## 8. Signature & sécurité

La **clé de signature est l'ancre de confiance de tout le système** : pas de Play
Store (donc pas de re-signature Google en filet), et le futur updater intégré fait
confiance à cette signature pour accepter une MaJ. La compromettre = pousser une
MaJ malveillante signée à tous les membres.

- **Signer sur eheyu, manuellement.** Le keystore ne quitte jamais eheyu
  (`signing.properties` gitignoré, `FERAL_RELEASE_*`). **Rejeté** : keystore dans
  les secrets CI (exposé à toute la supply-chain), signature sur le VPS de prod
  (exposé). La CI produit le **non signé** ; eheyu signe.
- Durcir : passphrase forte **hors des notes en clair**, sauvegarde offline
  chiffrée, idéalement token matériel (PKCS#11 / YubiKey). APK Signing Scheme
  v2+v3+v4.
- **Build reproductible** (à valider) : eheyu rebuild et *diffe* l'artefact CI
  avant de signer → une CI compromise ne peut pas faire signer du code injecté.

### Updater intégré (façon Telegram) — invariants de sécurité

- **Flux public signé** : manifeste + APK sur URL publique (l'app est publique ;
  seuls les membres peuvent s'y *connecter*). Intégrité par HTTPS **+ sha256 + et
  surtout la signature de l'APK** (le sha256 seul prouve le transport, pas
  l'authenticité).
- **Vérifier l'empreinte du certificat de signature Feral (pinnée) AVANT
  l'installation**, pas seulement le sha256.
- **Manifeste signé** (signature détachée, clé Feral vérifiée in-app) — sinon un
  MITM/compromission serveur peut le pointer vers un APK attaquant.
- **`versionCode` monotone** (refuser ≤ installé) → anti-downgrade.
- **TLS/public-key pinning** vers `feralisme.fr` en défense en profondeur.

## 9. Licence & marque

- **AGPL-3.0-only** (les en-têtes SPDX machine font foi). Garder les en-têtes SPDX
  et les copyrights Element/New Vector intacts ; marquer les fichiers modifiés
  (§5(a)). Distribuer l'APK **oblige** à fournir la source correspondante (§6/§13)
  → ajouter un lien **« code source »** in-app / sur `feralisme.fr` pointant vers
  le repo + tag exact.
- **Marque** (séparée de l'AGPL) : ne pas utiliser « Element »/« Element X », le
  logo ni le branding d'Element ; ne pas suggérer une affiliation. Feral est déjà
  conforme (nom/logo/identité distincts, services Element mis à `null` dans
  `BuildTimeConfig`). Autorisé, en texte : « compatible avec Element »,
  « construit sur la technologie open-source d'Element, sans affiliation ».

## 10. Registre de risques (synthèse)

| Risque | Mitigation |
|---|---|
| Revert silencieux d'une customisation sécu au sync (**déjà arrivé**) | test garde-fou bloquant + rebase-onto-tag + pile mince |
| Verrou members-only inactif (module non compilé) | override dans `impl-foss` via Metro `replaces` (fait) |
| Updater : install d'un APK malveillant / MITM | vérif empreinte certif + manifeste signé + `versionCode` monotone + pinning |
| Downgrade (ancien APK vulnérable, sha+signature valides) | `versionCode` monotone, version-plancher dans le manifeste signé |
| Exposition du keystore | eheyu offline uniquement, token matériel, sauvegarde chiffrée |
| Supply-chain `element-call-embedded` / transitives | version épinglée + dependency verification + bump review-required |
| Conflits récurrents sur les `translations.xml` Localazy | déplacer la notice dans une ressource Feral-owned |

---

_Analyse et mise en place initiales : 2026-08-21._

---

## 11. Updater intégré — implémentation (2026-08-21)

**Statut : implémenté** (branche `feral/fix-members-only-and-maintenance`), à valider
par le build CI puis un build signé eheyu.

### Côté app
- Nouveau module **`features/appupdate/{api,impl}`** (auto-inclus par
  `settings.gradle` + `allFeaturesImpl`) :
  - `AppUpdateChecker` — GET public `…/media/downloads/android/update.json`
    (OkHttp + kotlinx `ignoreUnknownKeys`), au plus une requête / 6 h
    (`AppUpdateConfig.CHECK_INTERVAL_MS`), résultat mis en cache (DataStore
    `feral_appupdate`), fail-quiet. Anti-downgrade (`versionCode` strictement
    supérieur) + version ignorée après « fermer ».
  - `ApkDownloader` — télécharge dans `cacheDir/updates/` (FileProvider `cache-path`),
    vérifie **sha256 manifeste** + **empreinte du certificat de signature épinglée**
    (`AppUpdateConfig.SIGNING_CERT_SHA256`, extraite de l'APK 25.05.4 servi) +
    **versionCode de l'archive** (> installé et == manifeste) + `packageName`,
    puis lance l'installation (`ACTION_VIEW` + FileProvider ; permission
    `REQUEST_INSTALL_PACKAGES` déjà dans le manifeste).
  - `AppUpdateBannerPresenter` — check au premier affichage de la liste, bannière
    via le composant design-system `Announcement`.
- **Édits cœur minimaux** (5 fichiers home) : champ `appUpdateBannerState` dans
  `RoomListContentState.Rooms`, presenter injecté, `AppUpdateBanner` en tête de la
  LazyColumn, fixtures/tests mis à jour. Strings dans
  `strings_feral_appupdate.xml` (en+fr, hors Localazy).
- `appconfig/AppUpdateConfig.kt` : URL du canal, empreinte certif, intervalle,
  interrupteur `ENABLED`.
- ⚠️ Le versionCode par ABI = base×10+chiffre ABI (`app/build.gradle.kts`) → le
  manifeste porte un versionCode **par APK** (géré par le script de release).

### Côté serveur (fait, sans sudo)
- `/var/www/html/feralism/media/downloads/android/` créé — servi publiquement par le
  bloc nginx `location /media/` existant (vérifié 200/206 en HTTPS). Les APK 25.05.4
  + .sha256 y sont copiés. `update.json` n'y sera déposé qu'à la **prochaine**
  release (une app sans updater ne le lit pas ; 404 = silencieux côté app).

### Variante buildée : `fdroid` = « sans Google » (décision 2026-08-22)
`gplay` / `fdroid` sont les noms **internes à Element** de deux variantes du même
APK, baptisées d'après les stores où Element publie. **Feral ne publie sur aucun
store** : dans les deux cas c'est un fichier `.apk` hébergé sur feralisme.fr. Seule
différence : le canal de notifications quand l'app est fermée.
- `gplay` : Firebase (Google) + passerelle push de matrix.org codée en dur
  (`FirebaseConfig.PUSHER_HTTP_URL`). C'était la variante de l'APK 25.05.4.
- `fdroid` : **aucun code Google** ; UnifiedPush (appli distributeur, ex. ntfy) avec
  repli interne « background sync » (`KeepInternalDistributor`) ; passerelle par
  défaut `matrix.gateway.unifiedpush.org` (`UnifiedPushConfig`).
Depuis 26.08.0 on build **`fdroid`** (`assembleFdroidRelease`) ; `sign-release.sh`
refuse un APK gplay. Même `applicationId`/clé ⇒ s'installe par-dessus 25.05.4.
**À faire ensuite** : héberger **ntfy** sur le VPS (il implémente la passerelle
`/_matrix/push/v1/notify` ; l'app la découvre via l'endpoint du distributeur) ⇒
notifications 100 % privées ; les membres installent l'appli ntfy pointée sur le VPS.

### Runbook de release (build CI non signé → signature sur eheyu)
1. Bump `plugins/src/main/kotlin/Versions.kt` (`versionYear`/`versionMonth`/
   `versionReleaseNumber` → versionName `YY.MM.N`, versionCode `20YYMM0N`×10+abi ;
   doit rester **strictement supérieur** à celui de l'APK installé chez les membres),
   pousser la branche.
2. GitHub → Actions → **« Feral release (unsigned) »** → *Run workflow* sur la branche
   (ou pousser un tag `feral-v<ver>` — seule voie tant que le workflow n'est pas sur
   la branche par défaut : GitHub ne liste dans Actions que les workflows de `develop`
   ou ayant déjà tourné). ~30–40 min. Artefact
   `feral-<ver>-release-unsigned` : 4 APK par ABI + universel (`*-unsigned.apk`),
   `SHA256SUMS`, `BUILD-INFO.txt` (versionCode lus via aapt2). Aucun secret en CI.
3. Sur **eheyu** (seule machine avec le keystore + `signing.properties`) :
```
git fetch origin && git checkout <branche> && git pull
unzip -d ~/feral-rel/<ver> ~/Téléchargements/feral-<ver>-release-unsigned.zip
./tools/feral/sign-release.sh --version <ver> --in ~/feral-rel/<ver>
./tools/feral/publish-release.sh --version <ver> --apk-dir ~/feral-rel/<ver>/signed \
    --changelog-fr "…" --changelog-en "…" --deploy loic_feral@172.232.45.124
```
`sign-release.sh` : zipalign + apksigner (mots de passe passés par env, jamais en
argv), renomme en `Feral-<ver>[-abi].apk`, et **refuse** : tout APK dont le certificat
≠ `AppUpdateConfig.SIGNING_CERT_SHA256` (mauvais keystore/alias = abort, rien n'est
produit), un APK qui n'est pas `feral.app` à la `--version` donnée, et un artefact
bâti depuis un autre commit que celui extrait (`BUILD-INFO.txt` ;
`--allow-commit-mismatch` pour passer outre). Sortie atomique (rien dans `signed/`
tant que ce n'est pas signé ET vérifié). Si `Permission denied` après `git pull` :
`chmod +x tools/feral/*.sh`. Pour re-signer, vider `signed/` et `publish/` d'abord
(le script remplace les APK, pas les fichiers d'une autre version). Voie alternative toujours valable : `./gradlew assembleFdroidRelease` sur
eheyu (`./gradlew assembleFdroidRelease`, signé directement via `signing.properties`)
puis `publish-release.sh`.
4. Vérifier : `curl -s https://feralisme.fr/media/downloads/android/update.json`,
   la page membre, puis installer **par-dessus** l'ancienne version sur un téléphone
   (`adb install -r Feral-<ver>-arm64-v8a.apk`) — doit passer sans désinstallation.
   La première release embarquant l'updater est forcément téléchargée à la main
   (l'APK 25.05.4 ne sait pas se mettre à jour) ; les suivantes arrivent via la
   bannière in-app.

Le script : renomme selon la convention `Feral-<ver>[-abi].apk`, génère
`.sha256`/`update.json`/`version.json`/`latest.json` (versionCode lus via aapt),
déploie **les APK d'abord, les manifestes en dernier** (atomicité), vers le
répertoire public ET `protected_downloads/` (page membre).

### Invariants de sécurité implémentés
sha256 (transport) + certificat épinglé (authenticité — sans le keystore eheyu,
aucun APK accepté) + versionCode monotone (anti-downgrade) + `packageName` vérifié +
HTTPS. Non implémenté (amélioration future) : signature détachée du manifeste,
TLS pinning.

---

## 12. Registre des syncs upstream

**Tag de base courant : `v26.08.2`** (`3ea7541628`). Mettre cette section à jour à
chaque sync ; c'est ici que `git rebase --onto <nouveau-tag> <tag-de-base-courant>`
(§4) lit son second argument.

### Sync v26.08.2 (2026-08-22)

| | |
|---|---|
| Base précédente | `1c5f185d6a` (upstream `develop` du 2026-02-27, versionName 26.03.0 — pas un tag) |
| Nouvelle base | tag **`v26.08.2`** = `3ea7541628` |
| Branche | `feral/sync/v26.08.2` |
| Méthode | pile mince **ré-appliquée** commit par commit sur le tag, à partir du diff net `1c5f185d6a..14d480b6d4` (159 fichiers) — pas un rebase de l'ancienne pile ; un `git merge v26.08.2` d'essai donnait 31 conflits dont 21 `translations.xml` Localazy |
| Toolchain | AGP **9.3.1** (Kotlin intégré : les plugins de convention n'appliquent plus `kotlin-android`), Kotlin **2.4.10**, Gradle **9.7.0**, compileSdk/targetSdk **37**, build-tools **37.0.0**, JDK 21, DI Metro. `gradle.properties` upstream est passé à `-Xmx8g` (+ `org.gradle.tooling.parallel`) : notre patch local 4g→8g n'a plus lieu d'être, fichier pris verbatim. `feral-ci.yml`/`feral-release.yml` installent `platforms;android-37` + `build-tools;37.0.0` via `sdkmanager` (idempotent) |
| Versions | upstream `Versions.kt` = 26.08.2 → versionCode `20260802`×10+abi, strictement supérieur au 26.08.0 livré (`20260800`×10+abi) : la prochaine release Feral est `feral-v26.08.2` sans bump manuel |

**Changements upstream qui touchent Feral :**
- **`EnterpriseService`** (`features/enterprise/api/…/EnterpriseService.kt`) :
  `defaultHomeserverList()` → **`homeserverAllowList()`** (l.43) ;
  `isAllowedToConnectToHomeserver(url)` conservée (l.50) ; `isEnterpriseBuild` n'est
  plus un membre du service (binding séparé `DefaultIsEnterpriseBuild`, impl-foss) ;
  nouveaux membres `isElementProEnforced`, `tweakMasUrl`, `overrideBrandColor`,
  `brandColorsFlow`, `semanticColorsFlow`, `firebasePushGateway`,
  `unifiedPushDefaultPushGateway`, `bugReportUrlFlow`, `getNoisyNotificationChannelId`.
  `FeralEnterpriseService` + `FeralEnterpriseServiceTest` adaptés ; l'invariant
  `canConnectToAnyHomeserver() == false` est inchangé. **À chaque sync** : differ ce
  fichier entre les deux tags — tout nouveau membre qui conditionne l'accès à un
  compte hérite en silence du défaut permissif de `DefaultEnterpriseService` par
  délégation et doit être surchargé + gardé par le test.
- **Point d'extension pour le plan ntfy (§11)** :
  `EnterpriseService.unifiedPushDefaultPushGateway(): String?` (l.84), consommé par
  `libraries/pushproviders/unifiedpush/…/DefaultPushGatewayHttpUrlProvider.kt:24`
  (`enterpriseService.unifiedPushDefaultPushGateway() ?: UnifiedPushConfig.DEFAULT_PUSH_GATEWAY_HTTP_URL`).
  Quand ntfy sera hébergé sur le VPS, retourner
  `"https://<ntfy-feral>/_matrix/push/v1/notify"` depuis `FeralEnterpriseService`
  suffit — **sans toucher `UnifiedPushConfig.kt`** (fichier upstream).
  `firebasePushGateway()` est sans objet (variante `fdroid`).
- **Renovate** : `.github/renovate.json` → **`.github/renovate.json5`** (+
  `minimumReleaseAge: 7 days`, sauf `io.element.android*` et le SDK Rust). Règle Feral
  `element-call-embedded` (no automerge) reportée.
- **CI upstream** : actions épinglées par SHA, `permissions: {}`,
  `persist-credentials: false`, lint **zizmor** sur les PR ; `tests.yml` a abandonné
  `--no-daemon`, `release.yml` le garde (comme `feral-release.yml`). Les tâches
  utilisées par `feral-ci.yml`/`feral-release.yml` sont inchangées. `feral-ci.yml` ne
  tire plus LFS (inutile pour compiler). Les workflows upstream (`build.yml`,
  `tests.yml`, `quality.yml`, `gradle-wrapper-update.yml`…) tournent aussi sur le
  fork et y sont rouges par construction (manifeste `gplay` diffé, Sonar…) : les
  désactiver dans l'UI Actions plutôt que de les patcher.
- **Element Call** : upstream v26.08.2 embarque `element-call-embedded` **0.23.0** ;
  le fork avait bumpé seul à **0.24.0** (`923c06d26b`, « fix cross-client E2EE
  calls »). Conformément à §6 la pile garde la version du tag (0.23.0) — **à vérifier
  sur un appel cross-client** ; si la régression E2EE revient, re-bumper en commit
  Feral distinct et le noter ici.
- `scripts/remove-feral-patches.sh` abandonné (§7).
