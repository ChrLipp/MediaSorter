package at.corba.tools.media_sorter

import at.corba.tools.media_sorter.libs.FileVersionProvider
import at.corba.tools.media_sorter.logic.MediaSorterLogic
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

/**
 * Define your options here. Remove the example option.
 */
@CommandLine.Command(
    name = "media-sorter",
    description = ["Sorts iPhone media files into monthly directories"],
    mixinStandardHelpOptions = true,
    versionProvider = FileVersionProvider::class)
@Component
class MediaSorterCommand : Callable<Int> {
    /** The logger */
    private val log = KotlinLogging.logger {}

    /** Group for directory movement */
    @CommandLine.ArgGroup(exclusive = false)
    private var directories: Dependent? = null

    class Dependent {
        /** Input directory */
        @CommandLine.Option(names = ["-i", "--input"], description = ["Input directory"], required = true)
        lateinit var inputDirectory: File

        /** Output directory */
        @CommandLine.Option(names = ["-o", "--output"], description = ["Output directory"], required = true)
        lateinit var outputDirectory: File

        @CommandLine.Option(names = ["-t", "--test"], description = ["Simulation mode, no movement"])
        var doSimulateOnly = false

    }

    /** Test file */
    @CommandLine.Option(names = ["-e", "--examine"], description = ["Examine single file"])
    var testFile: File? = null

    /** Business logic */
    @Autowired
    private lateinit var mediaSorterLogic: MediaSorterLogic

    /**
     * Call business logic.
     */
    override fun call(): Int {
        val dirEntry = directories
        val fileEntry = testFile

        if (dirEntry != null) {
            mediaSorterLogic.sort(dirEntry.inputDirectory, dirEntry.outputDirectory, dirEntry.doSimulateOnly)
            return 0
        }
        else if (fileEntry != null) {
            mediaSorterLogic.testFile(fileEntry)
            return 0
        }
        return -1
    }
}
