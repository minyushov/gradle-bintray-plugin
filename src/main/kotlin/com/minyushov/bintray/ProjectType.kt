package com.minyushov.bintray

internal sealed class ProjectType {
  abstract val pluginsConfigurators: List<PluginsConfigurator>
  abstract val artifacts: List<Artifact>

  internal class Unknown : ProjectType() {
    override val pluginsConfigurators = emptyList<PluginsConfigurator>()
    override val artifacts = emptyList<Artifact>()
  }

  internal class Java : ProjectType() {
    override val pluginsConfigurators =
      listOf(
        PluginsConfiguratorCommon()
      )

    override val artifacts =
      listOf(
        ArtifactMainJava(),
        ArtifactJavaDoc(),
        ArtifactJavaSources()
      )
  }

  internal class JavaAndroid : ProjectType() {
    override val pluginsConfigurators =
      listOf(
        PluginsConfiguratorCommon(),
        PluginsConfiguratorAndroid()
      )

    override val artifacts =
      listOf(
        ArtifactMainAndroid(),
        ArtifactAndroidDoc(),
        ArtifactAndroidSources()
      )
  }

  internal class Kotlin : ProjectType() {
    override val pluginsConfigurators =
      listOf(
        PluginsConfiguratorCommon(),
        PluginsConfiguratorKotlin()
      )

    override val artifacts =
      listOf(
        ArtifactMainJava(),
        ArtifactKotlinDoc(),
        ArtifactJavaSources()
      )
  }

  internal class KotlinAndroid : ProjectType() {
    override val pluginsConfigurators =
      listOf(
        PluginsConfiguratorCommon(),
        PluginsConfiguratorAndroid(),
        PluginsConfiguratorKotlinAndroid()
      )

    override val artifacts =
      listOf(
        ArtifactMainAndroid(),
        ArtifactKotlinDoc(),
        ArtifactAndroidSources()
      )
  }
}