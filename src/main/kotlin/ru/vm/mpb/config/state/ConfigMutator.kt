package ru.vm.mpb.config.state

import java.lang.UnsupportedOperationException

typealias ConfigMutator = (Any?) -> Unit

val emptyMutator: ConfigMutator = { }

val immutable: ConfigMutator = {
    throw UnsupportedOperationException("Mutation is not allowed for immutable Config objects")
}