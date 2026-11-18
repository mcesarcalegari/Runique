plugins {
    alias(libs.plugins.runique.android.feature.ui)
    alias(libs.plugins.runique.jvm.junit5)
}

android {
    namespace = "com.plcoding.goals.presentation"

    sourceSets {
        // Fakes shared by JVM unit tests and instrumented UI tests.
        getByName("test").kotlin.directories.add("src/sharedTest/java")
        getByName("androidTest").kotlin.directories.add("src/sharedTest/java")
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)

    implementation(projects.core.domain)
    implementation(projects.goals.domain)

    androidTestImplementation(libs.bundles.android.test)
}
