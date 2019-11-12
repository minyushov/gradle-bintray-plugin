import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.InputStream

class BuildConfigurationTest {
  @Rule
  @JvmField
  val projectDir = TemporaryFolder()

  @Test
  fun testEmpty() {
    javaClass
      .getResourceAsStream("empty.gradle")
      .copyTo(projectDir.newFile("build.gradle"))
      .let {
        GradleRunner
          .create()
          .forwardOutput()
          .withProjectDir(projectDir.root)
          .withArguments("tasks")
          .withDebug(true)
          .withPluginClasspath()
          .build()
      }
  }

  @Test
  fun testNotSupported() {
    javaClass
      .getResourceAsStream("not-supported.gradle")
      .copyTo(projectDir.newFile("build.gradle"))
      .let {
        GradleRunner
          .create()
          .forwardOutput()
          .withProjectDir(projectDir.root)
          .withArguments("bintrayUpload")
          .withDebug(true)
          .withPluginClasspath()
          .buildAndFail()
      }
      .apply {
        assert(output.contains("Task 'bintrayUpload' not found"))
      }
  }

  @Test
  fun testJava() {
    javaClass
      .getResourceAsStream("java.gradle")
      .copyTo(projectDir.newFile("build.gradle"))
      .let {
        GradleRunner
          .create()
          .forwardOutput()
          .withProjectDir(projectDir.root)
          .withArguments("bintrayUpload")
          .withDebug(true)
          .withPluginClasspath()
          .build()
      }
      .apply {
        assertEquals(TaskOutcome.SUCCESS, task(":bintrayUpload")?.outcome)
      }
  }

  @Test
  fun testGroovy() {
    javaClass
      .getResourceAsStream("groovy.gradle")
      .copyTo(projectDir.newFile("build.gradle"))
      .let {
        GradleRunner
          .create()
          .forwardOutput()
          .withProjectDir(projectDir.root)
          .withArguments("bintrayUpload")
          .withDebug(true)
          .withPluginClasspath()
          .build()
      }
      .apply {
        assertEquals(TaskOutcome.SUCCESS, task(":bintrayUpload")?.outcome)
      }
  }

  @Test
  fun testGradlePlugin() {
    javaClass
      .getResourceAsStream("gradle-plugin.gradle")
      .copyTo(projectDir.newFile("build.gradle"))
      .let {
        GradleRunner
          .create()
          .forwardOutput()
          .withProjectDir(projectDir.root)
          .withArguments("bintrayUpload")
          .withDebug(true)
          .withPluginClasspath()
          .build()
      }
      .apply {
        assertEquals(TaskOutcome.SUCCESS, task(":bintrayUpload")?.outcome)
      }
  }

  @Test
  fun testAndroid() {
    javaClass
      .getResourceAsStream("AndroidManifest.xml")
      .copyTo(File(projectDir.root, "src/main")
        .apply { mkdirs() }
        .run { File(this, "AndroidManifest.xml") })

    javaClass
      .getResourceAsStream("settings.gradle")
      .copyTo(projectDir.newFile("settings.gradle"))

    javaClass
      .getResourceAsStream("android.gradle")
      .copyTo(projectDir.newFile("build.gradle"))
      .let {
        GradleRunner
          .create()
          .forwardOutput()
          .withDebug(true)
          .withProjectDir(projectDir.root)
          .withArguments("bintrayUpload", "--stacktrace")
          .withPluginClasspath()
          .build()
      }
      .apply {
        assertEquals(TaskOutcome.SUCCESS, task(":bintrayUpload")?.outcome)
      }
  }

  private fun InputStream.copyTo(destination: File): File {
    FileUtils.copyInputStreamToFile(this, destination)
    return destination
  }
}