package ru.vm.mpb.config.state

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileReader
import java.io.Reader

object YamlLoader {

    private val yaml = Yaml()

    fun load(reader: Reader): Any? = yaml.load(reader)
    fun load(file: File): Any? = FileReader(file).use { load(it) }

}