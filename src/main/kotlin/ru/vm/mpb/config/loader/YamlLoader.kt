package ru.vm.mpb.config.loader

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole
import org.yaml.snakeyaml.Yaml
import java.io.Reader
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.reader

object YamlLoader {

    private val yaml = Yaml()

    fun load(reader: Reader): Any? = yaml.load(reader)
    fun load(path: Path): Any? = path.reader().use { load(it) }

    fun loadOrNull(path: Path): Any? = try {
        load(path)
    } catch (e: NoSuchFileException) {
        AnsiConsole.err().println(
            Ansi.ansi().fgYellow().bold().a("[warn]").reset().a(" config file ${path.toAbsolutePath()} doesn't exists")
        )
        null
    }

}