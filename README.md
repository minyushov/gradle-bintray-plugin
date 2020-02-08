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
Please apply the plugin after `com.android.library`, `kotlin-android`, `kotlin`, or `java-library`. 

### Configuring the plugin
```groovy
bintray {
  user = "user" // Your Bintray username. Required
  organization = "org" // Organization name. If not set will use `user` by default.
  key = "key" // Your Bintray API Key. Required
  repo = "repo" // Name of existing Bintray repository. Required
  groupId = "com.example" // Group ID of the publication. Required
  artifactId = "library" // Artifact ID of the publication. Required
  pkgName = "Bintray Package Name" // Optional. Default is 'artifactId'
  version = "1.0" // Version of the publication. Required
  vcsUrl = "https://github.com/example/example.git" // Your VCS URL. Required
  license = "Apache-2.0" // Publication licenses. Required
  dryRun = true // Whether to run this as dry-run, without deploying
  docs = true // Whether to attach a javadoc artifact
  sources = true // Whether to attach a sources artifact
  docsSettings = {
    // Javadoc or Dokka settings
  }
}
```

### Using the plugin
`./gradlew bintrayUpload` will publish artifacts to the Bintray.

If you don't want to expose your `Bintray User` or `Bintray Api Key` in `build.gradle`, follow the instructions below.
#### Option 1
1. Do not specify `user` and `key` in `bintrayUpload` extension
2. Add your `Bintray User` and `Bintray Api Key` to `local.properties` file:
```properties
bintray.user=username
bintray.key=key
```

#### Option 2
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
