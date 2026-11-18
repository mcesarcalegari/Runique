import com.plcoding.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * Runs JVM unit tests on the JUnit Platform (JUnit 5) and adds the shared unit-test
 * libraries. The vintage engine keeps existing JUnit 4 tests running.
 */
class JvmJUnit5ConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.run {
            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
            }

            dependencies {
                "testImplementation"(libs.findBundle("unit.test").get())
                // Existing JUnit 4 tests keep compiling and run on the vintage engine.
                "testImplementation"(libs.findLibrary("junit").get())
                "testRuntimeOnly"(libs.findLibrary("junit.vintage.engine").get())
                "testRuntimeOnly"(libs.findLibrary("junit.platform.launcher").get())
            }
        }
    }
}
