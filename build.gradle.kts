import Global.jvmTarget

plugins {
    kotlin("jvm") version Deps.JetBrains.Kotlin.VERSION
    id("com.github.johnrengelman.shadow") version Deps.Plugins.Shadow.VERSION
    application
}

group = "com.hoffi"
version = "1.0.0"
val artifactName by extra { project.name.toLowerCase() }
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
                    println("Test Results: ${outcome} (total: ${result.testCount} tests, ${result.successfulTestCount} successes, $fails, ${result.skippedTestCount} skipped)\n")
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
tasks.register("printClasspath") {
    group = "misc"
    description = "print classpath"
    doLast {
        // filters only existing and non-empty dirs
        project.getConfigurations().getByName("runtimeClasspath").getFiles()
            .filter { (it.isDirectory() && it.listFiles().isNotEmpty()) || it.isFile }
            .forEach{ println(it) }
    }
}
