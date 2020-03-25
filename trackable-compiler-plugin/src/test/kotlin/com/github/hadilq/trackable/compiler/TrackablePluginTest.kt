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

    private val trackablePackage = TRACKABLE_ANNOTATION.split(".").filter { it != "Trackable" }.joinToString(".")

    private val trackable = kotlin(
        "Trackable.kt",
        """|package $trackablePackage
           |
           |import kotlin.annotation.AnnotationRetention.BINARY
           |import kotlin.annotation.AnnotationTarget.CLASS
           |
           |@Retention(BINARY)
           |@Target(CLASS)
           |annotation class Trackable(val trackWith: String = "")
           |""".trimMargin("|")
    )

    @Test
    fun `generate the getTrack method`() {
        val result = compile(givenTrackableClass())
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `generate the getTrack method bytecode`() {
        val result = compile(givenTrackableClass())
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        val bytecode = fileBytecode(
            result.generatedFiles
                .first { it.exists() && it.isFile && it.name == "TrackableClass.class" }
        )
        assertThat(bytecode).isEqualTo(
            """|Compiled from "TrackableClass.kt"
               |public final class com.github.hadilq.trackable.compiler.test.TrackableClass {
               |  public com.github.hadilq.trackable.compiler.test.TrackableClass();
               |    Code:
               |       0: aload_0
               |       1: invokespecial #9                  // Method java/lang/Object."<init>":()V
               |       4: return
               |
               |  public final java.lang.String getTrack();
               |    Code:
               |       0: ldc           #15                 // String TrackableClass
               |       2: areturn
               |}
               |""".trimMargin("|")
        )
    }

    @Test
    fun `generate the getTrack method bytecode with annotated track`() {
        val result = compile(givenTrackableClassWithAnnotatedTrack())
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        val bytecode = fileBytecode(
            result.generatedFiles
                .first { it.exists() && it.isFile && it.name == "TrackableClass.class" }
        )
        assertThat(bytecode).isEqualTo(
            """|Compiled from "TrackableClass.kt"
               |public final class com.github.hadilq.trackable.compiler.test.TrackableClass {
               |  public com.github.hadilq.trackable.compiler.test.TrackableClass();
               |    Code:
               |       0: aload_0
               |       1: invokespecial #11                 // Method java/lang/Object."<init>":()V
               |       4: return
               |
               |  public final java.lang.String getTrack();
               |    Code:
               |       0: ldc           #16                 // String NotTrackableClass!
               |       2: areturn
               |}
               |""".trimMargin("|")
        )
    }

    @Test
    fun `generate the getTrack method bytecode for extended class`() {
        val result = compile(givenAnnotatedParentClass(), givenTrackableClassExtendedParent())
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        val bytecode = fileBytecode(
            result.generatedFiles
                .first { it.exists() && it.isFile && it.name == "TrackableClass.class" }
        )
        assertThat(bytecode).isEqualTo(
            """|Compiled from "TrackableClass.kt"
               |public final class com.github.hadilq.trackable.compiler.test.TrackableClass extends com.github.hadilq.trackable.compiler.test.Parent {
               |  public com.github.hadilq.trackable.compiler.test.TrackableClass();
               |    Code:
               |       0: aload_0
               |       1: invokespecial #8                  // Method com/github/hadilq/trackable/compiler/test/Parent."<init>":()V
               |       4: return
               |
               |  public final java.lang.String getTrack();
               |    Code:
               |       0: ldc           #14                 // String TrackableClass
               |       2: areturn
               |}
               |""".trimMargin("|")
        )
    }

    @Test
    fun `generate the getTrack method bytecode for extended interface`() {
        val result = compile(givenAnnotatedParentInterface(), givenTrackableClassExtendedParentInterface())
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        val bytecode = fileBytecode(
            result.generatedFiles
                .first { it.exists() && it.isFile && it.name == "TrackableClass.class" }
        )
        assertThat(bytecode).isEqualTo(
            """|Compiled from "TrackableClass.kt"
               |public final class com.github.hadilq.trackable.compiler.test.TrackableClass implements com.github.hadilq.trackable.compiler.test.Parent {
               |  public com.github.hadilq.trackable.compiler.test.TrackableClass();
               |    Code:
               |       0: aload_0
               |       1: invokespecial #10                 // Method java/lang/Object."<init>":()V
               |       4: return
               |
               |  public final java.lang.String getTrack();
               |    Code:
               |       0: ldc           #16                 // String TrackableClass
               |       2: areturn
               |}
               |""".trimMargin("|")
        )
    }

    @Test
    fun `generate and use the getTrack method`() {
        val result = compile(givenTrackableClass(), givenTrackableClassTestToRun())
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `generate and use the getTrack method bytecode`() {
        val result = compile(givenTrackableClass(), givenTrackableClassTestToRun())
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        val bytecode = fileBytecode(
            result.generatedFiles
                .first { it.exists() && it.isFile && it.name == "TrackableClassTestKt.class" }
        )
        println(bytecode)
        assertThat(bytecode).isEqualTo(
            """|Compiled from "TrackableClassTest.kt"
               |public final class com.github.hadilq.trackable.compiler.test.TrackableClassTestKt {
               |  public static final void main(java.lang.String[]);
               |    Code:
               |       0: getstatic     #13                 // Field java/lang/System.out:Ljava/io/PrintStream;
               |       3: new           #15                 // class com/github/hadilq/trackable/compiler/test/TrackableClass
               |       6: dup
               |       7: invokespecial #19                 // Method com/github/hadilq/trackable/compiler/test/TrackableClass."<init>":()V
               |      10: invokevirtual #23                 // Method com/github/hadilq/trackable/compiler/test/TrackableClass.getTrack:()Ljava/lang/String;
               |      13: invokevirtual #29                 // Method java/io/PrintStream.print:(Ljava/lang/String;)V
               |      16: return
               |}
               |""".trimMargin("|")
        )
    }

    @Test
    fun `generate and use the getTrack method then run`() {
        val result = compile(givenTrackableClass(), givenTrackableClassTestToRun())
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        val directory =
            result.generatedFiles.first { it.exists() && it.parentFile.name == "META-INF" }.parentFile.parentFile
        val output = runFiles(directory, "com.github.hadilq.trackable.compiler.test.TrackableClassTestKt")
        assertThat(output).isEqualTo("TrackableClass\n")
    }

    @Test
    fun `generate data class`() {
        val result = compile(
            kotlin(
                "TrackableDataClass.kt",
                """|package com.github.hadilq.trackable.compiler.test
                   |
                   |import ${trackablePackage}.Trackable
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
                   |import ${trackablePackage}.Trackable
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
              |import ${trackablePackage}.Trackable
              |
              |@Trackable
              |class TrackableClass 
              |""".trimMargin("|")
    )

    private fun givenTrackableClassExtendedParent(): SourceFile = kotlin(
        "TrackableClass.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |import ${trackablePackage}.Trackable
              |import com.github.hadilq.trackable.compiler.test.Parent
              |
              |class TrackableClass : Parent()
              |""".trimMargin("|")
    )

    private fun givenTrackableClassExtendedParentInterface(): SourceFile = kotlin(
        "TrackableClass.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |import ${trackablePackage}.Trackable
              |import com.github.hadilq.trackable.compiler.test.Parent
              |
              |class TrackableClass : Parent
              |""".trimMargin("|")
    )

    private fun givenAnnotatedParentClass(): SourceFile = kotlin(
        "Parent.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |import ${trackablePackage}.Trackable
              |
              |@Trackable
              |open class Parent
              |""".trimMargin("|")
    )

    private fun givenAnnotatedParentInterface(): SourceFile = kotlin(
        "Parent.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |import ${trackablePackage}.Trackable
              |
              |@Trackable
              |interface Parent
              |""".trimMargin("|")
    )

    private fun givenTrackableClassWithAnnotatedTrack(): SourceFile = kotlin(
        "TrackableClass.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |import ${trackablePackage}.Trackable
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
              |    System.out.print(TrackableClass().track)
              |}
              |""".trimMargin("|")
    )

    private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation {
        return KotlinCompilation()
            .apply {
                workingDir = temporaryFolder.root
                compilerPlugins = listOf<ComponentRegistrar>(
                    TrackableComponentRegistrar()
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
