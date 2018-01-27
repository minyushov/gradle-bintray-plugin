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

  private lateinit var buildFile: File

  @Before
  fun setup() {
  }

  @Test
  fun testEmpty() {
    javaClass
      .getResourceAsStream("empty.gradle")
      .copyTo(projectDir.newFile("build.gradle"))
      .let {
        GradleRunner.create()
          .withProjectDir(projectDir.root)
          .withPluginClasspath()
          .buildAndFail()
      }
      .apply {
        assert(output.contains("Bintray user is not defined in 'bintrayUpload' extension"))
      }
  }

  @Test
  fun testJava() {
    javaClass
      .getResourceAsStream("java.gradle")
      .copyTo(projectDir.newFile("build.gradle"))
      .let {
        GradleRunner.create()
          .withProjectDir(projectDir.root)
          .withArguments("bintrayUpload")
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