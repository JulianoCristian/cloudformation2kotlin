import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.11"
    maven
}

group  = "com.typedpath"
version  = "1.0.0"

val sourceCompatibility = 1.8

apply {
     plugin("idea")
     plugin("kotlin")
}

repositories {
    // uncomment to use s3
    // maven {
          //url = uri("https://repositorybuckettest65-s3hostingbucket-qpezmj5xooak.s3.amazonaws.com/repository")
    //}
    // uncomment to test maven snapshots repo
    //maven {
    //    url= uri("https://oss.sonatype.org/content/repositories/snapshots")
    //}

    mavenLocal()
    mavenCentral()
}

dependencies {
    //compile(kotlin("org.jetbrains.kotlin:kotlin-stdlib-jdk8"))
    compile("com.typedpath:cloudformation2kotlin:2.0.0")

    testCompile("com.amazonaws:aws-java-sdk-lambda:1.11.574")
    testCompile("com.amazonaws:aws-java-sdk-s3:1.11.574")
    testCompile("com.amazonaws:aws-java-sdk-cloudformation:1.11.574")
    testCompile("com.amazonaws:aws-java-sdk-codecommit:1.11.574")
    testCompile("com.amazonaws:aws-java-sdk-rdsdata:1.11.574")
    testCompile("com.amazonaws:aws-java-sdk-rds:1.11.574")
    testCompile("junit:junit:4.12")
    testCompile(project(":domain"))

}

val compileKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}

val compileTestKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}

