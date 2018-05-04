### Setting up the plugin
```groovy
buildscript {
  repositories {
    maven { url 'https://dl.bintray.com/minyushov/gradle' }
  }
  dependencies {
    classpath "com.minyushov.gradle:bintray:x"
  }
}
```
Please replace `x` with the latest version: [![Download](https://api.bintray.com/packages/minyushov/gradle/bintray/images/download.svg)](https://bintray.com/minyushov/gradle/bintray/_latestVersion)

```groovy
apply plugin: 'com.minyushov.bintray'
```
Please apply the plugin after `com.android.library`, `kotlin-android`, `kotlin`, or `java-library` plugin. 

### Configuring the plugin
```groovy
bintrayUpload {
  user = "Bintray User"
  key = "Bintray Api Key"
  artifactId = "library"
  repo = "repository"
  groupId = "com.example"
  version = "1.0"
  vcsUrl = "https://github.com/example/example.git"
  license = "Apache-2.0"
  dryRun = true
  docs = true
  sources = true
  docsSettings = {
    // Javadoc or Dokka settings
  }
}
```