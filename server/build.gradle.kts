import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.internal.os.OperatingSystem
import java.io.ByteArrayOutputStream

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.task.NodeTask
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.utils.extendsFrom
import kotlin.apply

plugins {
    id("base")
    id("com.github.node-gradle.node")
}

node {
    version = "22.14.0"
    npmVersion = "11.2.0"
    distBaseUrl = "https://nodejs.org/dist"
    download = true
}

// Register NpmTask that will do what "npm run build" command does.
val npmRunUpdateTask = tasks.named("npm_update", NpmTask::class.java) {
    // npmCommand.apply { listOf<String>("update") }
    args.apply { listOf<String>("--omit", "dev", "--loglevel", "warn") }
}

// Register NpmTask that will do what "npm run build" command does.
val npmRunBuildTask = tasks.named("npm_run_build", NpmTask::class.java) {
    // npmCommand.apply { listOf<String>("build") }
    outputs.upToDateWhen { file("${projectDir}/build").exists() }
    inputs.files(fileTree("public"))
    inputs.file("package.json")
    inputs.file("package-lock.json")
    outputs.dir("build")
}

// pack output of the build into JAR file
val packageNpmApp = tasks.register("packageNpmApp", Zip::class.java) {
    dependsOn(npmRunBuildTask)
    destinationDirectory = file("${projectDir}/build")
    from(".")
    include("index.js")
    include("package.json")
    include("README.md")
    include("public/*")
}

// declare a dedicated scope for publishing the packaged JAR
val npmResources: Configuration? by configurations.creating
configurations.default.get().extendsFrom(npmResources)

// expose the artifact created by the packaging task
artifacts {
    npmResources.apply {
        add("archives", packageNpmApp) {
            builtBy(packageNpmApp)
            type = "jar"
        }
    }
}

tasks.named("assemble").dependsOn(packageNpmApp)

tasks.named("clean") {
    delete(packageNpmApp.get().path)
}

tasks.register("startServer", Exec::class.java) {
    isIgnoreExitValue = true
    val stdOut = ByteArrayOutputStream()
    val stdErr = ByteArrayOutputStream()
    standardOutput = stdOut
    errorOutput = stdErr

    val os: OperatingSystem = OperatingSystem.current()
    commandLine("scripts/start_server" +
            if (os.isUnix || os.isLinux || os.isMacOsX) {".sh"}
            else {".bat"}
    )

    doLast {
        if (executionResult.get().exitValue == 0) {
            println(standardOutput.toString())
        } else {
            println(stdErr.toString())
        }
    }
}

tasks.register("stopServer", Exec::class.java) {
    isIgnoreExitValue = true
    val stdOut = ByteArrayOutputStream()
    val stdErr = ByteArrayOutputStream()
    standardOutput = stdOut
    errorOutput = stdErr

    val os: OperatingSystem = OperatingSystem.current()
    commandLine("scripts/stop_server" +
            if (os.isUnix || os.isLinux || os.isMacOsX) {".sh"}
            else {".bat"}
    )

    doLast {
        if (executionResult.get().exitValue == 0) {
            println(standardOutput.toString())
        } else {
            println(stdErr.toString())
        }
    }
}
