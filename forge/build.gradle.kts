import miehexaddon.utils.configureHexCliServer
import miehexaddon.utils.extractNestedJar

plugins {
    id("miehexaddon.platform")
}

val modId: String by project

architectury {
    forge()
}

loom {
    forge {
        convertAccessWideners = true
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)

        mixinConfig(
            "miehexaddon-common.mixins.json",
            "miehexaddon-forge.mixins.json",
        )
    }

    runs {
        for ((platform, outputProject) in arrayOf(
            // we're using forge to do the common datagen because fabric's datagen kind of sucks
            "common" to project(":common"),
            "forge" to project,
        )) {
            register("${platform}Datagen") {
                data()
                programArgs(
                    "--mod", modId,
                    "--all",
                    "--output", outputProject.file("src/generated/resources").absolutePath,
                    "--existing", file("src/main/resources").absolutePath,
                    "--existing", project(":common").file("src/main/resources").absolutePath,
                    "--existing-mod", "hexcasting",
                )
                property("miehexaddon.apply-datagen-mixin", "true")
                property("miehexaddon.$platform-datagen", "true")
            }
        }

        // Headless Forge dedicated server with HexCLI enabled (MieLib + hexParse). Started by `./gradlew :forge:runHexCli`.
        // The client endpoint is written to forge/run/hexcli/hexcli/client.json.
        register("hexCli") {
            server()
            name("HexCLI Server (Forge)")
            runDir("run/hexcli")
            property("mielib.hexcli", "true")
            // Forge runs with -Djava.net.preferIPv6Addresses=system, so MieLib's HexCLI endpoint would bind to [::1]
            // while hexcli/client.json says 127.0.0.1 ("Connection refused"). Force IPv4 for this run.
            property("java.net.preferIPv4Stack", "true")
            programArgs("nogui")
            ideConfigGenerated(false)
        }
    }
}

miehexaddonModDependencies {
    // expand versions in mods.toml
    filesMatching.add("META-INF/mods.toml")

    // transform Gradle version ranges into a Forge-compatible format
    anyVersion = ""
    mapVersions {
        replace(Regex("""\](\S+)"""), "($1")
        replace(Regex("""(\S+)\["""), "$1)")
    }

    // CurseForge/Modrinth mod dependency metadata
    requires("architectury-api")
    requires("cloth-config")
    requires(curseforge = "hexcasting", modrinth = "hex-casting")
    requires("kotlin-for-forge")
}

dependencies {
    forge(libs.forge)
    modApi(libs.architectury.forge)

    implementation(libs.kotlin.forge)

    modApi(libs.hexcasting.forge) { isTransitive = false }
    // Hex Casting dependencies
    // we use modLocalRuntime to add these to the development runtime, but not at compile time or for consumers of this project
    // but we use PAUCAL for datagen, so that's part of the actual implementation
    modImplementation(libs.paucal.forge)
    modLocalRuntime(libs.patchouli.forge)
    modLocalRuntime(libs.caelus)
    modLocalRuntime(libs.inline.forge) { isTransitive = false }

    modApi(libs.clothConfig.forge)

    libs.mixinExtras.common.also {
        compileOnly(it)
        annotationProcessor(it)
    }

    libs.mixinExtras.forge.also {
        localRuntime(it)
        include(it)
    }

    // MieLib (bundled via Jar-in-Jar). No public Forge jar yet: vendored in libs/, see libs/README.md
    modImplementation(libs.mielib.forge) { isTransitive = false }
    include(libs.mielib.forge) { isTransitive = false }

    // hexParse: development runtime only (needed by HexCLI)
    modLocalRuntime(libs.hexparse.forge) { isTransitive = false }
    // hexParse ships its yclib dependency jar-in-jar, which Loom does not load in dev; extract it separately.
    modLocalRuntime(files(extractNestedJar(
        libs.hexparse.forge.get().toString(),
        Regex("""META-INF/jars/yclib-.*\.jar"""),
        "yclib-forge.jar",
    )))
}

tasks {
    shadowJar {
        exclude("fabric.mod.json")
    }
}

configureHexCliServer()
