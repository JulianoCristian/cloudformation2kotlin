import java.util.regex.Pattern.compile

plugins {
    `kotlin-dsl`
    java
}

repositories {
    mavenCentral()
}

dependencies {
//      compile("com.amazonaws:aws-java-sdk:1.9.2")
  compile("com.amazonaws:aws-java-sdk-s3:1.11.574")
  compile("com.amazonaws:aws-java-sdk-core:1.11.574")

}
