package com.minyushov.bintray

internal sealed class ProjectType {
  abstract val pluginsConfigurators: List<PluginsConfigurator>
  abstract val artifacts: List<Artifact>
  val bintrayConfigurator = BintrayConfigurator()

  internal class UnknownProject : ProjectType() {
    override val pluginsConfigurators: List<PluginsConfigurator> = emptyList()
    override val artifacts: List<Artifact> = emptyList()
  }

  internal class JavaProject : ProjectType() {
    override val pluginsConfigurators by lazy {
      listOf(
        PluginsConfiguratorCommon()
      )
    }

    override val artifacts: List<Artifact> by lazy {
      listOf(
        ArtifactMainJava(),
        ArtifactJavaDoc(),
        ArtifactJavaSources()
      )
    }
  }

  internal class JavaAndroidProject : ProjectType() {
    override val pluginsConfigurators by lazy {
      listOf(
        PluginsConfiguratorCommon(),
        PluginsConfiguratorAndroid()
      )
    }

    override val artifacts: List<Artifact> by lazy {
      listOf(
        ArtifactMainAndroid(),
        ArtifactAndroidDoc(),
        ArtifactAndroidSources()
      )
    }
  }

  internal class KotlinProject : ProjectType() {
    override val pluginsConfigurators by lazy {
      listOf(
        PluginsConfiguratorCommon(),
        PluginsConfiguratorKotlin()
      )
    }

    override val artifacts: List<Artifact> by lazy {
      listOf(
        ArtifactMainJava(),
        ArtifactKotlinDoc(),
        ArtifactJavaSources()
      )
    }
  }

  internal class KotlinAndroidProject : ProjectType() {
    override val pluginsConfigurators by lazy {
      listOf(
        PluginsConfiguratorCommon(),
        PluginsConfiguratorAndroid(),
        PluginsConfiguratorKotlinAndroid()
      )
    }

    override val artifacts: List<Artifact> by lazy {
      listOf(
        ArtifactMainAndroid(),
        ArtifactKotlinDoc(),
        ArtifactAndroidSources()
      )
    }
  }
}