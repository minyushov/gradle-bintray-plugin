### Setting up the plugin
```groovy
buildscript {
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
  pkgName = "Bintray Package Name" // Optional. Default is 'artifactId'
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

### Using the plugin
`./gradlew bintrayUpload` will publish artifacts to the Bintray.

If you don't want to expose your `Bintray User` or `Bintray Api Key` in `build.gradle`, follow the instructions below.

1. Configure the plugin like this:
```groovy
bintrayUpload {
  user = findProperty('user') ?: ''
  key = findProperty('key') ?: ''
  ...
}
```

2. Run the following in the command line.
> ./gradlew -Puser=`<user-name>` -Pkey=`<bintray-api-key>` bintrayUpload
