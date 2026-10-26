You can put JAR files here for dependencies which aren't published to CurseForge/Modrinth or available on any Maven.

## Vendored: `mielib-forge-0.0.2.1+1.20.1-SNAPSHOT.jar`

MieLib does not publish a Forge jar yet (the `hexcli-v1.0.0` GitHub release only has the Fabric jar, which
Gradle downloads directly from the release). Until it does, the Forge jar lives here and is resolved as
`libs:mielib-forge` through the `flatDir` repository in `buildSrc`.

- Built from [xm1221/MieLib](https://github.com/xm1221/MieLib) `main` @ `c673420` (`./gradlew :forge:build`, file `forge/build/libs/`)
- SHA-256: `3913ff3c42a155fc76e0fd94d49f5bbebfb80c8c3061b9595a042c1d32d1ad41`
- MieLib is MIT-licensed.

When a Forge jar is published, delete this file and point `mielib-forge` in `gradle/libs.versions.toml` at it.
