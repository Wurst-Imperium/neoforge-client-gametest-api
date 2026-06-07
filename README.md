# NeoForge Client GameTest API (unofficial)

An unofficial NeoForge port of the Fabric Client GameTest API plus a tiny Gradle plugin that builds `src/gametest` as a separate mod jar.

This is a personal project, made because I wanted client gametests for my own NeoForge mods. No affiliation with Fabric or NeoForge maintainers, though they are welcome to use this code if they want to make it official one day.

## Usage

```groovy
plugins {
    id "net.neoforged.gradle.userdev" version "7.1.36"
    id "net.wimods.neoforge-client-gametest-api" version "0.1.0+26.1.1"
}

repositories {
    mavenLocal()
    maven { url = "https://maven.wimods.net/releases" }
}

runs {
    clientGameTest {
        arguments("--username", "ExampleFixedName")
    }
}
```

The fixed username is optional. It makes the test player always have the same name, and therefore the same skin, which is useful for consistent screenshots.

Put tests in `src/gametest/java` and gametest-only resources in `src/gametest/resources`.
Add a normal NeoForge mod metadata file at `src/gametest/resources/META-INF/neoforge.mods.toml`:

```toml
modLoader="javafml"
loaderVersion="${loader_version_range}"
license="${mod_license}"

[[mods]]
modId="${mod_id}_gametest"
version="1.0.0"
displayName="${mod_name} Client GameTests"
description="""Client gametests for ${mod_name}."""

[[mixins]]
config="example_gametest.mixins.json"

[modproperties.${mod_id}_gametest]
fabric-client-gametest = ["com.example.gametest.MyClientGameTest"]

[[dependencies.${mod_id}_gametest]]
modId="${mod_id}"
type="required"
versionRange="[0,)"
ordering="AFTER"
side="CLIENT"

[[dependencies.${mod_id}_gametest]]
modId="fabric_client_gametest_api_v1"
type="required"
versionRange="[0,)"
ordering="AFTER"
side="CLIENT"
```

The plugin only creates:

- `gametest` source set
- matching `gametestImplementation` dependency on the API mod
- `clientGameTestJar`
- `runClientGameTest` NeoGradle run wiring
