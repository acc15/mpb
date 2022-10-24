package ru.vm.mpb.util

import java.util.LinkedList

typealias FetchEdgesFunction<K> = (K) -> Iterable<K>
typealias NodePredicate<K> = (K) -> Boolean
typealias EdgePredicate<K> = (K, K) -> Boolean
typealias PathCallback<K> = (List<K>) -> Unit

fun <K> dfs(
    keys: Iterable<K>,
    links: FetchEdgesFunction<K>,
    onNode: NodePredicate<K> = { true },
    onEdge: EdgePredicate<K> = { _, _ -> true },
    onCycle: PathCallback<K> = {}
) {

    val visited = mutableSetOf<K>()
    val queue = OrderedHashMap<K, Iterator<K>?>()

    for (k in keys) {
        if (visited.contains(k)) {
            continue
        }

        queue[k] = null
        while (queue.isNotEmpty()) {
            val e = queue.lastEntry!!
            val node = e.key

            val iter: Iterator<K>? = e.value ?: if (visited.add(node) && onNode(node))
                links(node).iterator().also { e.setValue(it) } else null

            if (iter == null || !iter.hasNext()) {
                queue.removeLast()
                continue
            }

            val link = iter.next()
            if (queue.containsKey(link)) {
                onCycle(queue.subListOf(link).map { it.key } + link)
                continue
            }

            if (!onEdge(e.key, link)) {
                continue
            }
            queue[link] = null
        }

    }

}

fun <K> bfs(
    keys: Iterable<K>,
    links: FetchEdgesFunction<K>,
    onNode: NodePredicate<K> = { true },
    onEdge: EdgePredicate<K> = { _, _ -> true }
) {
    val visited = mutableSetOf<K>()
    val queue = keys.toCollection(LinkedList())
    while (queue.isNotEmpty()) {
        val k = queue.pop()
        if (!visited.add(k) || !onNode(k)) {
            continue
        }
        val it = links(k).iterator()
        while (it.hasNext()) {
            val l = it.next()
            if (!onEdge(k, l)) {
                continue
            }
            queue.add(l)
        }
    }
}

