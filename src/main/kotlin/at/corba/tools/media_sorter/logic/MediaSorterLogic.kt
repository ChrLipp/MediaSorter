package at.corba.tools.media_sorter.logic

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


@Component
class MediaSorterLogic
{
    /** The logger */
    private val log = KotlinLogging.logger {}

    fun sort(inputDirectory: File, outputDirectory: File, testOnly: Boolean) {
        val formatter = DateTimeFormatter.ofPattern("yyyyMM")
        inputDirectory.listFiles()
            .filter { fileEntry -> fileEntry.isFile }
            .forEach { fileEntry ->
                val mediaTS = getCreationDate(fileEntry)
                if (mediaTS != null) {
                    val monthDirectory = mediaTS.format(formatter)
                    performFileMove(fileEntry, outputDirectory, monthDirectory, testOnly)
                }
            }
    }

    private fun isPhoto(file: File) : Boolean {
        val extension = file.extension.uppercase()
        return (listOf<String>("JPG", "JPEG").contains(extension))
    }

    private fun isVideo(file: File) : Boolean {
        val extension = file.extension.uppercase()
        return (listOf<String>("MP4", "MOV").contains(extension))
    }

    private fun getCreationDate(file: File) : LocalDateTime? {
        var date : LocalDateTime?
        if (isPhoto(file)) {
            date = geMetadataIFD0Date(file)
            if (date == null) {
                date = geMetadataISubIFDDate(file)
            }
        }
        else if (isVideo(file)) {
            date = geMetadataQuicktimeMetadataDate(file)
        }
        else {
            log.debug { "Unknown file: ${file.absoluteFile}" }
            return null
        }

        if (date == null) {
            log.warn { "*** No meta data entry: ${file.absoluteFile}" }
            date = getFileDate(file)
        }
        return date
    }

    private fun listMetaEntries(file: File) {
        log.info { file.absoluteFile }
        ImageMetadataReader
            .readMetadata(file)
            .directories
            .forEach { directory ->
                log.info { "- $directory.name" }
                for (tag in directory.tags) {
                    log.info("  - ${directory.getName()}, ${tag.getTagName()}, ${tag.getDescription()}")
                }
            }
    }

    fun testFile(testFile: File) {
        listMetaEntries(testFile)
        val date = getCreationDate(testFile)
        log.info { date }
    }

    private fun geMetadataIFD0Date(file: File) : LocalDateTime? {
        val directory = ImageMetadataReader
            .readMetadata(file)
            .getFirstDirectoryOfType(ExifIFD0Directory::class.java)
            ?: return null
        val date = directory
            .getDate(ExifIFD0Directory.TAG_DATETIME, TimeZone.getDefault())
            ?: return null

        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }

    fun geMetadataISubIFDDate(file: File) : LocalDateTime? {
        val directory = ImageMetadataReader
            .readMetadata(file)
            .getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            ?: return null
        val date = directory
            .getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault())
            ?: return null

        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }

    private fun geMetadataQuicktimeMetadataDate(file: File) : LocalDateTime? {
        val directory = ImageMetadataReader
            .readMetadata(file)
            .getFirstDirectoryOfType(QuickTimeMetadataDirectory::class.java)
            ?: return null
        val date = directory
            .getDate(QuickTimeMetadataDirectory.TAG_CREATION_DATE, TimeZone.getDefault())
            ?: return null

        return LocalDateTime
            .ofInstant(date.toInstant(), ZoneId.systemDefault())
            .plusHours(1)
    }

    private fun getFileDate(file: File) : LocalDateTime {
        val attr = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
        val creationTS = LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault())
        val lastModifiedTS = LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.systemDefault())
        return if (creationTS < lastModifiedTS) {
            creationTS.plusHours(1)
        } else {
            lastModifiedTS.plusHours(1)
        }
    }

    private fun performFileMove(source: File, outputDir: File, monthDirectory: String, testOnly: Boolean) {
        val destinationDirectory = File(outputDir, monthDirectory)
        makeDirectory(destinationDirectory, testOnly)

        val destination = File(destinationDirectory, source.name)
        moveFile(source, destination, testOnly)
    }

    private fun makeDirectory(directory: File, testOnly: Boolean) {
        if (!directory.exists()) {
            if (testOnly) {
                log.info { "Make directory ${directory.path}" }
            }
            else {
                directory.mkdir()
            }
        }
    }

    private fun moveFile(source: File, destination: File, testOnly: Boolean) {
        if (testOnly) {
            log.info { "Move file from ${source.path} to ${destination.path}" }
        }
        else {
            Files.move(source.toPath(), destination.toPath())
        }
    }
}