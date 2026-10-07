# Eurybium

Kotlin Hypixel SkyBlock client mod for **Fabric / Minecraft 26.1.2**, using Java 25 and Gradle Kotlin DSL.

## Modules

- `minecraft`: all mod code, including application logic, platform services, the Fabric entrypoint, UniversalCraft adapters, commands, and Java mixins. Stonecutter creates a build target per supported Minecraft version from these shared sources. KSP generates `BuildInfo` for each target.
- `processors`: build-time KSP code generation. Never shipped in the mod.

The mod is a single module. Packages organize the code without extra Gradle boundaries; `ClientPlatform` remains a small injectable interface for lifecycle tests. The only separate module is the build-time processor.

KSP generates source files; Stonecutter handles version-specific preprocessing. No custom version preprocessor is necessary. The initial processor generates the mod version from Gradle so the setup exercises real code generation without introducing an annotation framework prematurely.

## Build and run

Install **JDK 25** and import this directory as a Gradle project in IntelliJ IDEA. Use JDK 25 for Gradle as well.

```sh
./gradlew build
./gradlew :minecraft:26.1.2:test
./gradlew :minecraft:26.1.2:runClient
```

Distributable jars are written to `build/libs/`. Install Fabric Loader, Fabric API, and Fabric Language Kotlin with the mod. UniversalCraft is bundled as a nested mod; all application classes compile directly into the mod jar. DevAuth and the KSP processor are not shipped.

In a world, run `/eurybium`. It prints local build/version information and a client tick counter, exercising generated metadata, the mixin hook, application lifecycle, and UniversalCraft chat output. It sends nothing to the server. Java is used for the small mixin; the application code is Kotlin. Prefer Fabric callbacks for future hooks when suitable APIs exist.

## Development authentication

DevAuth is present only on the development runtime classpath and enabled by default:

```sh
./gradlew :minecraft:26.1.2:runClient -Pdevauth=true
```

Follow DevAuth's sign-in instructions in the client log. Its configuration and account cache live in the ignored `.devauth/` directory. To select an account, configure DevAuth's local config file; never commit account data. Each Minecraft target has its own ignored `run/` directory.

## Version maintenance

1. Add a version to `minecraft_targets` in the root `gradle.properties`.
2. Add `minecraft/versions/<version>/gradle.properties` with Minecraft, Fabric API, and UniversalCraft coordinates and an explicit supported Minecraft range.
3. Update Minecraft integration code with Stonecutter conditions or target-specific implementations as needed. Keep version differences localized to the Minecraft integration code.
4. Run `./gradlew build` for every registered target, then launch each client and check mixins, commands, and rendering. Compilation alone does not prove runtime compatibility.

The first target is the VCS baseline; keep `minecraft/stonecutter.gradle.kts` set to that version when committing. Use Stonecutter's IDE tasks to switch the active version. To retire a target, remove it from the list, remove its target directory, and simplify conditions that no remaining target needs. Historical releases remain available in Git.

Common library/plugin versions are pinned in `gradle/libs.versions.toml`; Stonecutter is pinned in `settings.gradle.kts`. Shared Kotlin/Java conventions are in the root build script. This project targets unobfuscated Minecraft 26.1+, so Loom does not need a separate mappings dependency.

## Verification

The `Build` GitHub Actions workflow runs on pull requests targeting `master` and can also be
started manually from the Actions tab. It uses Java 25 and the root `build` task to build and
run tests for every version in `minecraft_targets`; new targets are included automatically.
The workflow continues independent targets after a failure, while still failing the overall
check if any build or test fails.

The lifecycle test uses a fake platform and requires no running Minecraft client. Before releasing, also launch the built mod on the supported client and verify `/eurybium` and mixin application.

## Development worlds

World ZIPs are published as release attachments in
[Eurybium-Data](https://github.com/BusinessDirt/Eurybium-Data). Its `dev/worlds.json` index
contains the download metadata. List the available worlds, then install one by its ID:

```sh
./gradlew :minecraft:26.1.2:listDevWorlds
./gradlew :minecraft:26.1.2:installDevWorld -Pworld=mineshaft-example
```

Replace `26.1.2` with your target. The installer verifies the SHA-256 checksum, then extracts
the ZIP into that target's configured client `run/saves/<id>/` directory. It does not restrict
worlds by Minecraft version. Existing saves are never replaced: move or remove an old copy yourself before
installing a fresh fixture. ZIPs may contain `level.dat` at their root or inside one top-level
world folder, such as `Amber Mineshaft/level.dat`. The installer removes that wrapper folder
so the installed save always has `level.dat` directly beneath `run/saves/<id>/`. Archives with
multiple top-level world folders are rejected.

The default index comes from the data repository's `master` branch. Use `-PdevWorldRef=<tag-or-commit>`
to select a published revision, or read an unpublished index from a local data checkout:

```sh
./gradlew :minecraft:26.1.2:listDevWorlds -PdevWorldIndex=../Eurybium-Data/dev/worlds.json
```

`devWorldIndex` accepts a path relative to the mod repository, an absolute path, or an HTTPS URL.
Local indexes may use absolute `file:` archive URLs for testing before publishing a release.
Downloads are cached under `.gradle/dev-worlds/`; `--offline` uses the cached index and verified ZIPs.
Ordinary builds do not download or install worlds.
