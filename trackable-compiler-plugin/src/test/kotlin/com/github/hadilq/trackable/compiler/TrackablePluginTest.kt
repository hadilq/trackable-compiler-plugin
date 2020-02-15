package com.github.hadilq.trackable.compiler

import com.github.hadilq.trackable.compiler.TrackableCodegenExtension.Companion.DATA_INLINE_CLASS_ERROR_MESSAGE
import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.JvmTarget
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TrackablePluginTest {

    @Rule
    @JvmField
    var temporaryFolder: TemporaryFolder = TemporaryFolder()

    private val trackable = kotlin(
        "Trackable.kt", """
          |package com.github.hadilq.trackable.compiler.test
          |
          |import kotlin.annotation.AnnotationRetention.BINARY
          |import kotlin.annotation.AnnotationTarget.CLASS
          |
          |@Retention(BINARY)
          |@Target(CLASS)
          |annotation class Trackable
          |""".trimMargin("|")
    )

    @Test
    fun `generate the getTrack method`() {
        val result = compile(
            kotlin(
                "TrackableClass.kt", """
              |package com.github.hadilq.trackable.compiler.test
              |
              |import com.github.hadilq.trackable.compiler.test.Trackable
              |
              |@Trackable
              |class TrackableClass
              |""".trimMargin("|")
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `generate and use the getTrack method`() {
        val result = compile(
            kotlin(
                "TrackableClass.kt", """
              |package com.github.hadilq.trackable.compiler.test
              |
              |import com.github.hadilq.trackable.compiler.test.Trackable
              |
              |@Trackable
              |class TrackableClass
              |""".trimMargin("|")
            ), kotlin(
                "TrackableClassTest.kt", """
              |package com.github.hadilq.trackable.compiler.test
              |
              |object TrackableClassTest {
              |    init {
              |        TrackableClass().track
              |    }
              |}
              |""".trimMargin("|")
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `generate data class`() {
        val result = compile(
            kotlin(
                "TrackableDataClass.kt", """
              |package com.github.hadilq.trackable.compiler.test
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
                "TrackableDataClass.kt", """
              |package com.github.hadilq.trackable.compiler.test
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

    private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation {
        return KotlinCompilation()
            .apply {
                workingDir = temporaryFolder.root
                compilerPlugins = listOf<ComponentRegistrar>(
                    TrackableComponentRegistrar("com.github.hadilq.trackable.compiler.test.Trackable")
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
}
