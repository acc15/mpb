package ru.vm.mpb.jansi

import org.fusesource.jansi.Ansi

class NoColorAnsi: Ansi {

    constructor(): super()
    constructor(size: Int): super(size)
    constructor(builder: StringBuilder): super(builder)
    constructor(parent: Ansi): super(parent)

    override fun fg(color: Color?) = this
    override fun fg(color: Int) = this
    override fun fgRgb(color: Int) = this
    override fun fgRgb(r: Int, g: Int, b: Int) = this
    override fun bg(color: Color?) = this
    override fun bg(color: Int) = this
    override fun bgRgb(color: Int) = this
    override fun bgRgb(r: Int, g: Int, b: Int) = this
    override fun fgBright(color: Color?) = this
    override fun bgBright(color: Color?) = this
    override fun reset() = this
    override fun bold() = this
    override fun boldOff() = this
}