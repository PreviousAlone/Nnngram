import java.io.File
import java.util.Properties
import org.gradle.api.JavaVersion

object Version {

    @JvmStatic
    val java = JavaVersion.VERSION_17

    @JvmStatic
    val officialVersionName = "10.5.0"

    @JvmStatic
    val isStable = true

    fun findBuildToolsVersion(): String {
        val defaultBuildToolsVersion = "33.0.0"
        return File(System.getenv("ANDROID_HOME"), "build-tools").listFiles()?.filter { it.isDirectory }?.maxOfOrNull { it.name }?.also { println("Using build tools version $it") }
            ?: defaultBuildToolsVersion
    }

    fun findNdkVersion(): String {
        val defaultNdkVersion = "23.2.8568313"
        val ndkBundle = File(System.getenv("ANDROID_HOME"), "ndk-bundle")
        if (ndkBundle.exists()) {
            val sourceProperties = File(ndkBundle, "source.properties")
            if (sourceProperties.exists()) {
                val properties = Properties()
                sourceProperties.inputStream().use {
                    properties.load(it)
                }
                return properties.getProperty("Pkg.Revision").also { if (it != null) println("Using ndk version $it") }
                    ?: defaultNdkVersion
            }
        }
        return File(System.getenv("ANDROID_HOME"), "ndk").listFiles()?.filter { it.isDirectory }?.maxOfOrNull { it.name }.also { if (it != null) println("Using ndk version $it") }
            ?: defaultNdkVersion
    }
}
