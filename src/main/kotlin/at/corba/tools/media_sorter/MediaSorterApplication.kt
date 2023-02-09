package at.corba.tools.media_sorter

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import picocli.CommandLine
import kotlin.system.exitProcess

@SpringBootApplication
class MediaSorterApplication(
    private val commandLineParameter: MediaSorterCommand
) : CommandLineRunner, ExitCodeGenerator {
    /** Variable for passing the exit code */
    private var exitCode: Int = 0

    override fun run(vararg args: String?) {
        exitCode = CommandLine(commandLineParameter).execute(*args)
    }

    override fun getExitCode(): Int {
        return exitCode
    }
}

fun main(args: Array<String>) {
    val context = runApplication<MediaSorterApplication>(*args)
    exitProcess(SpringApplication.exit(context))
}
