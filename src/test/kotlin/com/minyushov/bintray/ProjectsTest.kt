package com.minyushov.bintray

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val PLUGIN_ID = "com.minyushov.bintray"
private const val PLUGIN_VERSION = "2.0-dev"
private val PLUGIN_EXTENSION =
  """
    |bintray {
    |  dryRun = true
    |  user = "test"
    |  key = "test"
    |  repo = "test"
    |  groupId = "com.minyushov.test"
    |  artifactId = "test"
    |  version = "1.0"
    |  vcsUrl = "https://github.com/minyushov/test"
    |  license = "Apache-2.0"
    |}
  """.trimMargin()


class ProjectsTest {
  @TempDir
  lateinit var projectDir: File

  @Test
  fun testEmpty() {
    projectDir.create("build.gradle") {
      """
        plugins {
          id '$PLUGIN_ID' version '$PLUGIN_VERSION'
        }
      """
    }

    GradleRunner
      .create()
      .forwardOutput()
      .withProjectDir(projectDir)
      .withArguments("tasks")
      .withDebug(true)
      .withPluginClasspath()
      .build()
  }

  @Test
  fun testNotSupported() {
    projectDir.create("build.gradle") {
      """
        plugins {
          id '$PLUGIN_ID' version '$PLUGIN_VERSION'
        }

        $PLUGIN_EXTENSION
      """
    }

    GradleRunner
      .create()
      .forwardOutput()
      .withProjectDir(projectDir)
      .withArguments("bintrayUpload")
      .withDebug(true)
      .withPluginClasspath()
      .buildAndFail()
      .apply {
        assert(output.contains("Task 'bintrayUpload' not found"))
      }
  }

  @Test
  fun testJava() {
    projectDir.create("build.gradle") {
      """
        plugins {
          id 'java-library'
          id '$PLUGIN_ID' version '$PLUGIN_VERSION'
        }

        $PLUGIN_EXTENSION
      """
    }

    GradleRunner
      .create()
      .forwardOutput()
      .withProjectDir(projectDir)
      .withArguments("bintrayUpload")
      .withDebug(true)
      .withPluginClasspath()
      .build()
      .apply {
        assertEquals(TaskOutcome.SUCCESS, task(":bintrayUpload")?.outcome)
      }
  }

  @Test
  fun testGroovy() {
    projectDir.create("build.gradle") {
      """
        plugins {
          id 'groovy'
          id '$PLUGIN_ID' version '$PLUGIN_VERSION'
        }

        $PLUGIN_EXTENSION
      """
    }

    GradleRunner
      .create()
      .forwardOutput()
      .withProjectDir(projectDir)
      .withArguments("bintrayUpload")
      .withDebug(true)
      .withPluginClasspath()
      .build()
      .apply {
        assertEquals(TaskOutcome.SUCCESS, task(":bintrayUpload")?.outcome)
      }
  }

  @Test
  fun testGradlePlugin() {
    projectDir.create("build.gradle") {
      """
        plugins {
          id 'java-gradle-plugin'
          id '$PLUGIN_ID' version '$PLUGIN_VERSION'
        }

        gradlePlugin {
          plugins {
            customPlugin {
              id = 'com.minyushov.test'
              implementationClass = 'com.minyushov.test.Plugin'
            }
          }
        }

        $PLUGIN_EXTENSION
      """
    }

    GradleRunner
      .create()
      .forwardOutput()
      .withProjectDir(projectDir)
      .withArguments("bintrayUpload")
      .withDebug(true)
      .withPluginClasspath()
      .build()
      .apply {
        assertEquals(TaskOutcome.SUCCESS, task(":bintrayUpload")?.outcome)
      }
  }

  @Test
  fun testAndroid() {
    projectDir.create("src/main/AndroidManifest.xml") {
      """
        <manifest package="com.test"/>
      """
    }

    projectDir.create("local.properties") {
      """
        sdk.dir=${System.getenv("ANDROID_HOME")}
      """
    }

    projectDir.create("settings.gradle") {
      """
        pluginManagement {
          repositories {
            gradlePluginPortal()
            google()
          }
          resolutionStrategy {
            eachPlugin {
              if (requested.id.id == "com.android.library") {
                useModule("com.android.tools.build:gradle:4.0.0-alpha09")
              }
            }
          }
        }
      """
    }

    projectDir.create("build.gradle") {
      """
        buildscript {
          repositories {
            jcenter()
            google()
          }
        }

        plugins {
          id 'com.android.library'
          id '$PLUGIN_ID' version '$PLUGIN_VERSION'
        }

        repositories {
          jcenter()
          google()
        }

        android {
          compileSdkVersion 29

          defaultConfig {
            minSdkVersion 21
            targetSdkVersion 29
          }
        }

        $PLUGIN_EXTENSION
      """
    }

    GradleRunner
      .create()
      .forwardOutput()
      .withDebug(true)
      .withProjectDir(projectDir)
      .withArguments("bintrayUpload", "--stacktrace")
      .withPluginClasspath()
      .build()
      .apply {
        assertEquals(TaskOutcome.SUCCESS, task(":bintrayUpload")?.outcome)
      }
  }

  private inline fun File.create(path: String, content: () -> String) =
    resolve(path).apply { parentFile.mkdirs() }.appendText(content().trimIndent())

}