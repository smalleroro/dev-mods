plugins {
    id("eclipse")
    id("idea")
    id("maven-publish")
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    kotlin("jvm") version "1.9.22"
}

fun prop(key: String): String = providers.gradleProperty(key).get()

val mcModId = prop("mod_id")
val mcModName = prop("mod_name")
val mcModVersion = prop("mod_version")
val mcModGroupId = prop("mod_group_id")
val mcModAuthors = prop("mod_authors")
val mcModDescription = prop("mod_description")
val mcVersion = prop("minecraft_version")
val mcVersionRange = prop("minecraft_version_range")
val mcForgeVersion = prop("forge_version")
val mcForgeVersionRange = prop("forge_version_range")
val mcLoaderVersionRange = prop("loader_version_range")
val mcMappingChannel = prop("mapping_channel")
val mcMappingVersion = prop("mapping_version")
val kffVersion = prop("kff_version")
val kffLoaderVersionRange = prop("kff_loader_version_range")

version = "$mcModVersion+mc$mcVersion"
group = mcModGroupId

base {
    archivesName = mcModId
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

minecraft {
    mappings(mcMappingChannel, mcMappingVersion)
    copyIdeResources = true

    runs {
        configureEach {
            workingDirectory = file("run").absolutePath
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods {
                create(mcModId) {
                    source(sourceSets.main.get())
                }
            }
        }
        create("client") {
            property("forge.enabledGameTestNamespaces", mcModId)
        }
    }
}

sourceSets.main.get().resources {
    srcDir("src/generated/resources")
}
sourceSets.main.get().kotlin.srcDir("src/main/kotlin")

repositories {
    mavenCentral()
    maven {
        name = "MinecraftForge"
        url = uri("https://maven.minecraftforge.net/")
    }
    maven {
        name = "KotlinForForge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
        content { includeGroup("thedarkcolour") }
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:$mcVersion-$mcForgeVersion")

    // Kotlin for Forge -- do NOT wrap in fg.deobf()
    implementation("thedarkcolour:kotlinforforge:$kffVersion")

    // Mixin annotation processor: processes @Mixin / @Inject at compile time.
    // See the note further down on why it does not produce a refmap here.
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

// The Mixin annotation processor only understands searge/notch obfuscation
// services, but this project compiles against official (Mojang) mappings — the
// same names Forge uses at runtime on 1.20.1. A refmap would therefore be
// meaningless, so @Inject is declared with remap = false and the processor emits
// no refmap for this build. If a searge-mapped build is ever needed, switch
// mapping_channel to "searge" and drop the remap = false.
tasks.named<JavaCompile>("compileJava").configure {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources>().configureEach {
    val replaceProperties = mapOf(
        "minecraft_version" to mcVersion,
        "minecraft_version_range" to mcVersionRange,
        "forge_version" to mcForgeVersion,
        "forge_version_range" to mcForgeVersionRange,
        "loader_version_range" to mcLoaderVersionRange,
        "kff_loader_version_range" to kffLoaderVersionRange,
        "mod_id" to mcModId,
        "mod_name" to mcModName,
        "mod_license" to "MIT",
        "mod_version" to mcModVersion,
        "mod_authors" to mcModAuthors,
        "mod_description" to mcModDescription
    )
    inputs.properties(replaceProperties)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(replaceProperties)
    }
}

tasks.named<Jar>("jar").configure {
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to mcModId,
                "Specification-Vendor" to mcModAuthors,
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version.toString(),
                "Implementation-Vendor" to mcModAuthors,
                "MixinConfigs" to "rotationspoof.mixins.json"
            )
        )
    }
    finalizedBy("reobfJar")
}
