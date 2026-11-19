plugins {
    alias(libs.plugins.runique.android.library)
    alias(libs.plugins.runique.android.room)
    alias(libs.plugins.runique.jvm.junit5)
}

android {
    namespace = "com.plcoding.core.database"
}

dependencies {
    implementation(libs.org.mongodb.bson)
    implementation(libs.bundles.koin)

    implementation(projects.core.domain)

    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(libs.room.testing)
}