package com.github.hadilq.trackable.compiler

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.JvmTarget
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class TrackablePluginTest {

    @Rule
    @JvmField
    var temporaryFolder: TemporaryFolder = TemporaryFolder()

    private val trackable = kotlin(
        "Trackable.kt",
        """|package com.github.hadilq.trackable.compiler.test
           |
           |import kotlin.annotation.AnnotationRetention.BINARY
           |import kotlin.annotation.AnnotationTarget.CLASS
           |
           |@Retention(BINARY)
           |@Target(CLASS)
           |annotation class Trackable(val trackWith: String = "")
           |""".trimMargin("|")
    )
    private val trackablePackage = "com.github.hadilq.trackable.compiler.test"

    @Test
    fun `generate the getTrack method`() {
        val result = compile(givenTrackableClass())
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `generate the getTrack method bytecode then run`() {
        val result = compile(
            givenTrackableClass(),
            givenTrackableClassTestToRun()
        )
        assertExecutionResult(result)
    }

    @Test
    fun `generate the getTrack method bytecode with annotated track then run`() {
        val result = compile(
            givenTrackableClassWithAnnotatedTrack(),
            givenTrackableClassTestToRun()
        )
        assertExecutionResult(result, "NotTrackableClass!")
    }

    @Test
    fun `generate the getTrack method bytecode for extended class then run`() {
        val result = compile(
            givenTrackableClassExtendedParent(),
            givenTrackableClassTestToRun()
        )
        assertExecutionResult(result)
    }

    @Test
    fun `generate the getTrack method bytecode for double extended class then run`() {
        val result = compile(
            givenTrackableClassDoubleExtendedParent(),
            givenTrackableClassTestToRun()
        )
        assertExecutionResult(result)
    }

    @Test
    fun `generate the getTrack method bytecode for extended interface then run`() {
        val result = compile(
            givenTrackableClassExtendedParentInterface(),
            givenTrackableClassTestToRun()
        )
        assertExecutionResult(result)
    }

    @Test
    fun `generate and use the getTrack method then run`() {
        val result = compile(givenTrackableClass(), givenTrackableClassTestToRun())
        assertExecutionResult(result)
    }

    @Test
    fun `generate data class`() {
        val result = compile(
            kotlin(
                "TrackableDataClass.kt",
                """|package com.github.hadilq.trackable.compiler.test
                   |
                   |import com.github.hadilq.trackable.compiler.test.Trackable
                   |
                   |@Trackable
                   |data class TrackableDataClass(private val firstParam: String = "")
                   |""".trimMargin("|")
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("TrackableDataClass.kt: (5, 1): $DATA_INLINE_CLASS_ERROR_MESSAGE")
    }

    @Test
    fun `generate inline class`() {
        val result = compile(
            kotlin(
                "TrackableDataClass.kt",
                """|package com.github.hadilq.trackable.compiler.test
                   |
                   |import com.github.hadilq.trackable.compiler.test.Trackable
                   |
                   |@Trackable
                   |inline class TrackableDataClass(private val firstParam: String = "")
                   |""".trimMargin("|")
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("TrackableDataClass.kt: (5, 1): $DATA_INLINE_CLASS_ERROR_MESSAGE")
    }

    private fun givenTrackableClass(): SourceFile = kotlin(
        "TrackableClass.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |import com.github.hadilq.trackable.compiler.test.Trackable
              |
              |@Trackable
              |class TrackableClass 
              |""".trimMargin("|")
    )

    private fun givenTrackableClassExtendedParent(): SourceFile = kotlin(
        "TrackableClass.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |import com.github.hadilq.trackable.compiler.test.Trackable
              |import com.github.hadilq.trackable.compiler.test.Parent
              |
              |@Trackable
              |open class Parent
              |
              |class TrackableClass : Parent()
              |""".trimMargin("|")
    )

    private fun givenTrackableClassDoubleExtendedParent(): SourceFile = kotlin(
        "TrackableClass.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |import com.github.hadilq.trackable.compiler.test.Trackable
              |import com.github.hadilq.trackable.compiler.test.Parent
              |
              |@Trackable
              |open class Parent
              |
              |open class ParentTrackableClass : Parent()
              |
              |class TrackableClass : ParentTrackableClass()
              |""".trimMargin("|")
    )

    private fun givenTrackableClassExtendedParentInterface(): SourceFile = kotlin(
        "TrackableClass.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |import com.github.hadilq.trackable.compiler.test.Trackable
              |import com.github.hadilq.trackable.compiler.test.Parent
              |
              |@Trackable
              |interface Parent
              |
              |class TrackableClass : Parent
              |""".trimMargin("|")
    )

    private fun givenTrackableClassWithAnnotatedTrack(): SourceFile = kotlin(
        "TrackableClass.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |import com.github.hadilq.trackable.compiler.test.Trackable
              |
              |@Trackable(trackWith = "NotTrackableClass!")
              |class TrackableClass 
              |""".trimMargin("|")
    )

    private fun givenTrackableClassTestToRun(): SourceFile = kotlin(
        "TrackableClassTest.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |fun main(args: Array<String>?) {
              |    System.out.print(TrackableClass().track())
              |}
              |""".trimMargin("|")
    )

    private fun assertExecutionResult(result: KotlinCompilation.Result, outputString: String = "TrackableClass") {
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        val directory =
            result.generatedFiles.first { it.exists() && it.parentFile.name == "META-INF" }.parentFile.parentFile
        val output = runFiles(directory, "$trackablePackage.TrackableClassTestKt")
        assertThat(output).isEqualTo("$outputString\n")
    }

    private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation {
        return KotlinCompilation()
            .apply {
                workingDir = temporaryFolder.root
                compilerPlugins = listOf<ComponentRegistrar>(
                    TrackableComponentRegistrar("${trackablePackage}.Trackable")
                )
                inheritClassPath = true
                sources = sourceFiles.asList() + trackable
                verbose = false
                jvmTarget = JvmTarget.fromString(System.getenv()["ci_java_version"] ?: "1.8")!!.description
                includeRuntime = true
            }
    }

    private fun compile(vararg sourceFiles: SourceFile): KotlinCompilation.Result {
        return prepareCompilation(*sourceFiles).compile()
    }

    private fun runFiles(directory: File, mainClass: String) =
        runCommand("java -cp ${directory.absolutePath} $mainClass")

    private fun fileBytecode(file: File) = runCommand("javap -c X $file")

    private fun runCommand(command: String): String {
        val p: Process = Runtime.getRuntime().exec(command)
        val input = BufferedReader(InputStreamReader(p.inputStream))
        var line: String?
        val sb = StringBuilder()
        while (run { line = input.readLine();line } != null) {
            sb.appendln(line)
        }
        input.close()
        val error = BufferedReader(InputStreamReader(p.errorStream))
        while (run { line = error.readLine();line } != null) {
            println("Error of running: $command -> $line")
        }
        error.close()
        return sb.toString()
    }
}
