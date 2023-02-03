package at.corba.startertemplate

import mu.KotlinLogging
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.util.concurrent.Callable

/**
 * Define your options here. Remove the example option.
 */
@CommandLine.Command(
    name = "Template",
    description = ["Description"],
    mixinStandardHelpOptions = true,
    versionProvider = FileVersionProvider::class)
@Component
class StarterTemplateCommand : Callable<Int> {
    /** The logger */
    private val log = KotlinLogging.logger {}

    @CommandLine.Option(names = ["-o", "--option"], description = ["Provide some option."])
    private var option : String = ""

    /**
     * Place your application logic entry point here.
     */
    override fun call(): Int {
        log.info("Option given: $option")
        return 0
    }
}
