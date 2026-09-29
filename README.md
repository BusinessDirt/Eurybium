# Eurybium

Kotlin Hypixel SkyBlock client mod for **Fabric / Minecraft 26.1.2**, using Java 25 and Gradle Kotlin DSL.

## Modules

- `core`: Minecraft-independent application and feature logic. KSP generates `BuildInfo` here.
- `compat-api`: plain Kotlin interfaces and data contracts shared with the Minecraft integration.
- `minecraft`: Fabric entrypoint, UniversalCraft adapters, commands, and Java mixins. Stonecutter creates a build target per supported Minecraft version from these shared sources.
- `processors`: build-time KSP code generation. Never shipped in the mod.

Dependencies: `core -> compat-api`; `minecraft -> core + compat-api + UniversalCraft`. Keep Minecraft, Fabric, and UniversalCraft imports out of `core` and `compat-api`. Add services to `compat-api` as features need them; do not mirror the entire Minecraft API.

KSP generates source files; Stonecutter handles version-specific preprocessing. No custom version preprocessor is necessary. The initial processor generates the mod version from Gradle so the setup exercises real code generation without introducing an annotation framework prematurely.

## Build and run

Install **JDK 25** and import this directory as a Gradle project in IntelliJ IDEA. Use JDK 25 for Gradle as well.

```sh
./gradlew build
./gradlew :core:test
./gradlew :minecraft:26.1.2:runClient
```

Distributable jars are written to `build/libs/`. Install Fabric Loader, Fabric API, and Fabric Language Kotlin with the mod. UniversalCraft is bundled as a nested mod; our core and API classes are embedded in the jar. DevAuth and the KSP processor are not shipped.

In a world, run `/eurybium`. It prints local build/version information and a client tick counter, exercising generated metadata, the mixin hook, shared core, and UniversalCraft chat output. It sends nothing to the server. Java is used for the small mixin; the application code is Kotlin. Prefer Fabric callbacks for future hooks when suitable APIs exist.

## Development authentication

DevAuth is present only on the development runtime classpath and disabled by default:

```sh
./gradlew :minecraft:26.1.2:runClient -Pdevauth=true
```

Follow DevAuth's sign-in instructions in the client log. Its configuration and account cache live in the ignored `.devauth/` directory. To select an account, configure DevAuth's local config file; never commit account data. Each Minecraft target has its own ignored `run/` directory.

## Version maintenance

1. Add a version to `minecraft_targets` in the root `gradle.properties`.
2. Add `minecraft/versions/<version>/gradle.properties` with Minecraft, Fabric API, and UniversalCraft coordinates and an explicit supported Minecraft range.
3. Update Minecraft integration code with Stonecutter conditions or target-specific implementations as needed. Keep these differences out of the core.
4. Run `./gradlew build` for every registered target, then launch each client and check mixins, commands, and rendering. Compilation alone does not prove runtime compatibility.

The first target is the VCS baseline; keep `minecraft/stonecutter.gradle.kts` set to that version when committing. Use Stonecutter's IDE tasks to switch the active version. To retire a target, remove it from the list, remove its target directory, and simplify conditions that no remaining target needs. Historical releases remain available in Git.

Common library/plugin versions are pinned in `gradle/libs.versions.toml`; Stonecutter is pinned in `settings.gradle.kts`. Shared Kotlin/Java conventions are in the root build script. This project targets unobfuscated Minecraft 26.1+, so Loom does not need a separate mappings dependency.

## Verification

CI builds all registered targets and runs the core tests. The lifecycle test uses a fake platform and requires no running Minecraft client. Before releasing, also launch the built mod on the supported client and verify `/eurybium` and mixin application.
