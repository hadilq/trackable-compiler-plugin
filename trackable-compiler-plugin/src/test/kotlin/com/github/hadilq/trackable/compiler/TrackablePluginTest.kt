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
        val result = compile(givenTrackableClass(), givenTrackableClassTest())
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `generate and use the getTrack method bytecode`() {
        val result = compile(givenTrackableClass(), givenTrackableClassTest())
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        val bytecode = fileBytecode(
            result.generatedFiles
                .first { it.exists() && it.isFile && it.name == "TrackableClassTest.class" }
        )
        assertThat(bytecode).isEqualTo(
            """|Compiled from "TrackableClassTest.kt"
               |public final class com.github.hadilq.trackable.compiler.test.TrackableClassTest {
               |  public static final com.github.hadilq.trackable.compiler.test.TrackableClassTest INSTANCE;
               |
               |  static {};
               |    Code:
               |       0: new           #2                  // class com/github/hadilq/trackable/compiler/test/TrackableClassTest
               |       3: dup
               |       4: invokespecial #25                 // Method "<init>":()V
               |       7: astore_0
               |       8: aload_0
               |       9: putstatic     #27                 // Field INSTANCE:Lcom/github/hadilq/trackable/compiler/test/TrackableClassTest;
               |      12: new           #29                 // class com/github/hadilq/trackable/compiler/test/TrackableClass
               |      15: dup
               |      16: invokespecial #30                 // Method com/github/hadilq/trackable/compiler/test/TrackableClass."<init>":()V
               |      19: invokevirtual #34                 // Method com/github/hadilq/trackable/compiler/test/TrackableClass.getTrack:()Ljava/lang/String;
               |      22: astore_1
               |      23: iconst_0
               |      24: istore_2
               |      25: getstatic     #40                 // Field java/lang/System.out:Ljava/io/PrintStream;
               |      28: aload_1
               |      29: invokevirtual #46                 // Method java/io/PrintStream.println:(Ljava/lang/Object;)V
               |      32: return
               |}
               |""".trimMargin("|")
        )
    }

    @Test
    fun `generate and use the getTrack method run`() {
        val result = compile(givenTrackableClass(), givenTrackableClassTestToRun())
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        val directory =
            result.generatedFiles.first { it.exists() && it.parentFile.name == "META-INF" }.parentFile.parentFile
        val output = runFiles(directory, "$trackablePackage.TrackableClassTestKt")
        assertThat(output).isEqualTo("TrackableClass\n")
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
              |class TrackableClass : Parent()
              |""".trimMargin("|")
    )

    private fun givenTrackableClassExtendedParentInterface(): SourceFile = kotlin(
        "TrackableClass.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |import com.github.hadilq.trackable.compiler.test.Trackable
              |import com.github.hadilq.trackable.compiler.test.Parent
              |
              |class TrackableClass : Parent
              |""".trimMargin("|")
    )

    private fun givenAnnotatedParentClass(): SourceFile = kotlin(
        "Parent.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |import com.github.hadilq.trackable.compiler.test.Trackable
              |
              |@Trackable
              |open class Parent
              |""".trimMargin("|")
    )

    private fun givenAnnotatedParentInterface(): SourceFile = kotlin(
        "Parent.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |import com.github.hadilq.trackable.compiler.test.Trackable
              |
              |@Trackable
              |interface Parent
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

    private fun givenTrackableClassTest(): SourceFile = kotlin(
        "TrackableClassTest.kt",
        """   |package com.github.hadilq.trackable.compiler.test
              |
              |object TrackableClassTest {
              |    init {
              |        println(TrackableClass().track)
              |    }
              |}
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
