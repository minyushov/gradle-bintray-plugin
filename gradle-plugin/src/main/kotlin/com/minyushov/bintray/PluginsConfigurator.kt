package com.minyushov.bintray

import com.jfrog.bintray.gradle.BintrayPlugin
import digital.wup.android_maven_publish.AndroidMavenPublishPlugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.jetbrains.dokka.gradle.DokkaAndroidPlugin
import org.jetbrains.dokka.gradle.DokkaPlugin

internal interface PluginsConfigurator {
  fun configure(project: Project)
}

internal class PluginsConfiguratorCommon : PluginsConfigurator {
  override fun configure(project: Project) {
    project.pluginManager.apply {
      apply(MavenPublishPlugin::class.java)
      apply(BintrayPlugin::class.java)
    }
  }
}

internal class PluginsConfiguratorAndroid : PluginsConfigurator {
  override fun configure(project: Project) {
    project.pluginManager.apply {
      apply(AndroidMavenPublishPlugin::class.java)
    }
  }
}

internal class PluginsConfiguratorKotlin : PluginsConfigurator {
  override fun configure(project: Project) {
    project.pluginManager.apply {
      apply(DokkaPlugin::class.java)
    }
  }
}

internal class PluginsConfiguratorKotlinAndroid : PluginsConfigurator {
  override fun configure(project: Project) {
    project.pluginManager.apply {
      apply(DokkaAndroidPlugin::class.java)
    }
  }
}