plugins {
    kotlin("jvm") version Deps.JetBrains.Kotlin.VERSION
    id("com.github.johnrengelman.shadow") version Deps.Plugins.Shadow.VERSION
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
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${Deps.Misc.DATETIME.VERSION}")
    implementation("com.github.ajalt.clikt:clikt:${Deps.Misc.CLIKT.VERSION}")
    //implementation(kotlin("reflect"))

    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223:${Deps.JetBrains.Kotlin.VERSION}")
    //runtimeOnly("net.java.dev.jna:jna:5.8.0")

    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
}

application {
    mainClass.set(theMainClass)
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
            jvmTarget = JavaVersion.VERSION_11.toString()
            //Will retain parameter names for Java reflection
            javaParameters = true
        }
    }

    withType<Test> {
        // classpath += developmentOnly

        useJUnitPlatform {
            //includeEngines("junit-jupiter", "spek2")
            // includeTags "fast"
            // excludeTags "app", "integration", "messaging", "slow", "trivial"
        }
        failFast = false
        ignoreFailures = false
        // reports.html.isEnabled = true

        testLogging {
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            events = setOf(
                org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
            ) //, STARTED //, standardOut, standardError)
        }

        addTestListener(object : TestListener {
            override fun beforeTest(descriptor: TestDescriptor?) {
                logger.lifecycle("Running $descriptor")
            }

            override fun beforeSuite(p0: TestDescriptor?) = Unit
            override fun afterTest(desc: TestDescriptor, result: TestResult) = Unit
            override fun afterSuite(desc: TestDescriptor, result: TestResult) {
                if (desc.parent == null) { // will match the outermost suite
                    println("\nTotal Test Results:")
                    println("===================")
                    val failsDefault = "${result.failedTestCount} failures"
                    val fails =
                        if (result.failedTestCount > 0) colorString(ConsoleColor.RED, failsDefault) else failsDefault
                    val outcome = if (result.resultType.name == "FAILURE") colorString(
                        ConsoleColor.RED,
                        result.resultType.name
                    ) else colorString(ConsoleColor.GREEN, result.resultType.name)
                    println("Test Results: $outcome (total: ${result.testCount} tests, ${result.successfulTestCount} successes, $fails, ${result.skippedTestCount} skipped)\n")
                }
            }
        })

        // listen to standard out and standard error of the test JVM(s)
        // onOutput { descriptor, event -> logger.lifecycle("Test: " + descriptor + " produced standard out/err: " + event.message ) }
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
        val foreground = ConsoleColor.YELLOW
        val background = ConsoleColor.DEFAULT
        val shadowJar by tasks.getting(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class)
        printlnColor(foreground, "  fat/uber jar: ${shadowJar.archiveFileName.get()}", background)
        printlnColor(foreground, "Gradle version: " + project.gradle.gradleVersion, background)
        printColor(foreground, "Kotlin version: " + kotlin.coreLibrariesVersion) ; if (kotlin.coreLibrariesVersion != Deps.JetBrains.Kotlin.VERSION) printColor(ConsoleColor.RED, " ( != ${Deps.JetBrains.Kotlin.VERSION} )")
        println()
        printlnColor(foreground, "javac  version: " + org.gradle.internal.jvm.Jvm.current(), background) // + " with compiler args: " + options.compilerArgs, backgroundColor = ConsoleColor.DARK_GRAY)
        printlnColor(foreground, "       srcComp: " + java.sourceCompatibility, background)
        printlnColor(foreground, "       tgtComp: " + java.targetCompatibility, background)
        printlnColor(foreground, "versions of core dependencies:", background)
        val regex = Regex(pattern = "^(spring-cloud-starter|spring-boot-starter|micronaut-core|kotlin-stdlib-jdk[0-9-]+|foundation-desktop)-[0-9].*$")
        if (subprojects.size > 0) {
            configurations.compileClasspath.get().map { it.nameWithoutExtension }.filter { it.matches(regex) }
                .forEach { printlnColor(foreground, String.format("%-25s: %s", project.name, it), background) }
        } else {
            configurations.compileClasspath.get().map { it.nameWithoutExtension }.filter { it.matches(regex) }
                .forEach { printlnColor(foreground, "  $it", background) }
        }
    }
}
val build by tasks.existing {
    val versionsPrint by tasks.existing
    finalizedBy(versionsPrint)
}
