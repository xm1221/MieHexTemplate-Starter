import miehexaddon.utils.configureHexCliServer
import miehexaddon.utils.extractNestedJar

plugins {
    id("miehexaddon.platform")
}

architectury {
    fabric()
}

fabricApi {
    configureDataGeneration {
        outputDirectory = file("src/generated/resources")
        modId = providers.gradleProperty("modId")
        strictValidation = true
        addToResources = false
    }
}

loom {
    runs {
        named("datagen") {
            property("miehexaddon.apply-datagen-mixin", "true")
        }

        // Headless dedicated server with HexCLI enabled (MieLib + hexParse). Started by `./gradlew runHexCli`.
        // The client endpoint is written to fabric/run/hexcli/hexcli/client.json.
        register("hexCli") {
            server()
            name("HexCLI Server")
            runDir("run/hexcli")
            property("mielib.hexcli", "true")
            programArgs("nogui")
            ideConfigGenerated(false)
        }
    }
}

miehexaddonModDependencies {
    // expand versions in fabric.mod.json
    filesMatching.add("fabric.mod.json")

    // transform Gradle version ranges into a Fabric-compatible format
    anyVersion = "*"
    mapVersions {
        replace(",", " ")
        replace(Regex("""\s+"""), " ")
        replace(Regex("""\[(\S+)"""), ">=$1")
        replace(Regex("""(\S+)\]"""), "<=$1")
        replace(Regex("""\](\S+)"""), ">$1")
        replace(Regex("""(\S+)\["""), "<$1")
    }

    // CurseForge/Modrinth mod dependency metadata
    requires("architectury-api")
    requires("cloth-config")
    requires(curseforge = "hexcasting", modrinth = "hex-casting")
    requires("fabric-api")
    requires("fabric-language-kotlin")
    optional("modmenu")
}

dependencies {
    modApi(libs.fabric.api)
    modImplementation(libs.fabric.loader)

    modImplementation(libs.kotlin.fabric)

    modApi(libs.architectury.fabric) {
        // Fix for the "two fabric loaders" loading crash
        exclude(group = "net.fabricmc", module = "fabric-loader")
    }

    modApi(libs.hexcasting.fabric) {
        // If not excluded here, calls a nonexistent method and crashes the dev client
        exclude(module = "phosphor")
    }
    // Hex Casting dependencies
    // we use modLocalRuntime to add these to the development runtime, but not at compile time or for consumers of this project
    modLocalRuntime(libs.paucal.fabric)
    modLocalRuntime(libs.patchouli.fabric)
    modLocalRuntime(libs.cardinalComponents)
    modLocalRuntime(libs.serializationHooks)
    modLocalRuntime(libs.trinkets)
    modLocalRuntime(libs.inline.fabric) { isTransitive = false }

    libs.mixinExtras.fabric.also {
        localRuntime(it)
        include(it)
        annotationProcessor(it)
    }

    modApi(libs.clothConfig.fabric) {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modImplementation(libs.modMenu)

    // MieLib (bundled via Jar-in-Jar)
    modImplementation(libs.mielib.fabric) { isTransitive = false }
    include(libs.mielib.fabric) { isTransitive = false }

    // hexParse: development runtime only (needed by HexCLI), never a dependency of the published mod
    modLocalRuntime(libs.hexparse.fabric) { isTransitive = false }
    // hexParse ships its yclib dependency jar-in-jar, which Loom does not load in dev; extract it separately.
    modLocalRuntime(files(extractNestedJar(
        libs.hexparse.fabric.get().toString(),
        Regex("""META-INF/jars/yclib-.*\.jar"""),
        "yclib-fabric.jar",
    )))
}

configureHexCliServer()
