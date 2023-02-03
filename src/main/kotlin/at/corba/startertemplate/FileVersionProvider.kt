package at.corba.startertemplate

import picocli.CommandLine
import java.io.File
import java.io.InputStreamReader

/**
 * VersionProvider to automatically link the version number from `gradle.properties` to Picocli.
 * See also `gradle/version.gradle`.
 */
class FileVersionProvider : CommandLine.IVersionProvider {
    override fun getVersion(): Array<String> {
        val url = javaClass.getResource("/version.txt")
            ?: return arrayOf("Unknown version.")

        return if (url.protocol == "jar") {
            val pathInsideOfJar = url.file.substringAfterLast('!')
            val inputStream = javaClass.getResourceAsStream(pathInsideOfJar)
            val reader = InputStreamReader(inputStream, "UTF-8")
            reader.readLines().toTypedArray()
        } else {
            val version = File(url.toURI()).readLines()
            version.toTypedArray()
        }
    }
}
