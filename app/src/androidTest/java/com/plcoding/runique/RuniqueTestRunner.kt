package com.plcoding.runique

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/** Starts the app as [TestRuniqueApp] so tests can swap the network boundary. */
class RuniqueTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, TestRuniqueApp::class.java.name, context)
    }
}
