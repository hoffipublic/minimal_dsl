plugins {
    kotlin("jvm") version BuildSrcGlobal.VersionKotlin
    id("com.github.johnrengelman.shadow") version "shadow".pluginVersion()
    application
}

group = "com.hoffi"
version = "1.0.0"
val artifactName by extra { project.name.toLowerCase().replace('_', '-') }
val theMainClass by extra { "com.hoffi.dsl.AppKt" }

repositories {
    mavenCentral()
}

dependencies {
    //implementation("io.github.microutils:kotlin-logging:2.0.6")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime".depAndVersion())
    implementation("com.github.ajalt.clikt:clikt".depAndVersion())
    //implementation(kotlin("reflect"))

    runtimeOnly("org.jetbrains.kotlin:kotlin-main-kts:${BuildSrcGlobal.VersionKotlin}")
    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223:${BuildSrcGlobal.VersionKotlin}")
    //runtimeOnly("net.java.dev.jna:jna:5.8.0")

    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
}

application {
    mainClass.set(theMainClass)
}

kotlin {
    jvmToolchain(BuildSrcGlobal.jdkVersion)
}

tasks {
    withType<Jar> {
        archiveBaseName.set(artifactName)
    }
    shadowJar {
        manifest { attributes["Main-Class"] = theMainClass }
        archiveClassifier.set("fat")
        mergeServiceFiles()
        minimize()
    }
//    val build by existing {
//        finalizedBy(shadowCreate)
//    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions{
            //Will retain parameter names for Java reflection
            javaParameters = true
            kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
        }
    }

    withType<Test> {
        buildSrcJvmTestConfig()
    }
}

// Helper tasks to speed up things and don't waste time
//=====================================================
// 'c'ompile 'c'ommon
val cc by tasks.registering {
    dependsOn(
        ":compileKotlin",
        ":compileTestKotlin")
}

// ################################################################################################
// #####    pure informational stuff on stdout    #################################################
// ################################################################################################
tasks.register<CheckVersionsTask>("checkVersions") // implemented in buildSrc/src/main/kotlin/Deps.kt
tasks.register("printClasspath") {
    group = "misc"
    description = "print classpath"
    doLast {
        // filters only existing and non-empty dirs
        project.configurations.getByName("runtimeClasspath").files
            .filter { it.isDirectory && (it?.listFiles()?.isNotEmpty() ?: false) || it.isFile }
            .forEach{ println(it) }
    }
}
tasks.register("versionsPrint") {
    group = "misc"
    description = "extract spring boot versions from dependency jars"
    doLast {
        val foreground = BuildSrcGlobal.ConsoleColor.YELLOW
        val background = BuildSrcGlobal.ConsoleColor.DEFAULT
        val shadowJar by tasks.getting(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class)
        BuildSrcGlobal.printlnColor(foreground, "  fat/uber jar: ${shadowJar.archiveFileName.get()}", background)
        BuildSrcGlobal.printlnColor(foreground, "Gradle version: " + project.gradle.gradleVersion, background)
        BuildSrcGlobal.printColor(foreground, "Kotlin version: " + kotlin.coreLibrariesVersion) ; if (kotlin.coreLibrariesVersion != BuildSrcGlobal.VersionKotlin) BuildSrcGlobal.printColor(
        BuildSrcGlobal.ConsoleColor.RED, " ( != ${BuildSrcGlobal.VersionKotlin} )")
        println()
        BuildSrcGlobal.printlnColor(foreground, "javac  version: " + org.gradle.internal.jvm.Jvm.current(), background) // + " with compiler args: " + options.compilerArgs, backgroundColor = ConsoleColor.DARK_GRAY)
        BuildSrcGlobal.printlnColor(foreground, "       srcComp: " + java.sourceCompatibility, background)
        BuildSrcGlobal.printlnColor(foreground, "       tgtComp: " + java.targetCompatibility, background)
        BuildSrcGlobal.printlnColor(foreground, "versions of core dependencies:", background)
        val regex = Regex(pattern = "^(spring-cloud-starter|spring-boot-starter|micronaut-core|kotlin-stdlib-jdk[0-9-]+|foundation-desktop)-[0-9].*$")
        if (subprojects.size > 0) {
            configurations.compileClasspath.get().map { it.nameWithoutExtension }.filter { it.matches(regex) }
                .forEach { BuildSrcGlobal.printlnColor(foreground, String.format("%-25s: %s", project.name, it), background) }
        } else {
            configurations.compileClasspath.get().map { it.nameWithoutExtension }.filter { it.matches(regex) }
                .forEach { BuildSrcGlobal.printlnColor(foreground, "  $it", background) }
        }
    }
}
val build by tasks.existing {
    val versionsPrint by tasks.existing
    finalizedBy(versionsPrint)
}
