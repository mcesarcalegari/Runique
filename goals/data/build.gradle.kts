plugins {
    alias(libs.plugins.runique.android.library)
    alias(libs.plugins.runique.android.room)
    alias(libs.plugins.runique.jvm.junit5)
}

android {
    namespace = "com.plcoding.goals.data"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.koin)
    implementation(libs.org.mongodb.bson)
    implementation(libs.timber)

    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.goals.domain)

    androidTestImplementation(libs.bundles.android.test)
}
