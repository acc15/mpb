package ru.vm.mpb.util

import java.util.LinkedList

typealias FetchEdgesFunction<K> = (K) -> Iterable<K>
typealias NodeFunction<K> = (K) -> Boolean
typealias EdgeFunction<K> = (K, K) -> Boolean
typealias PathFunction<K> = (List<K>) -> Boolean

fun <K> dfs(
    root: K,
    links: FetchEdgesFunction<K>,
    onNode: NodeFunction<K>? = null,
    onEdge: EdgeFunction<K>? = null,
    onCycle: PathFunction<K>? = null
) {

    val q = OrderedHashMap<K, Iterator<K>?>()
    q[root] = null

    while (q.isNotEmpty()) {

        val e = q.lastEntry!!
        val k = e.key
        var iter = e.value
        if (iter == null) {
            if (onNode != null && !onNode(k)) {
                continue
            }
            iter = links(k).iterator()
            e.setValue(iter)
        }

        if (!iter.hasNext()) {
            q.removeLast()
            continue
        }

        val l = iter.next()
        if (q.containsKey(l) && (onCycle == null || !onCycle(q.subListOf(l).map { it.key }))) {
            continue
        }

        if (onEdge != null && !onEdge(e.key, l)) {
            continue
        }
        q[l] = null
    }
}

fun <K> bfs(keys: Set<K>, links: FetchEdgesFunction<K>, onNode: ((K) -> Boolean)? = null, onEdge: ((K, K) -> Boolean)? = null) {
    val q = LinkedList(keys)
    while (q.isNotEmpty()) {
        val k = q.pop()
        if (onNode != null && !onNode(k)) {
            continue
        }
        val it = links(k).iterator()
        while (it.hasNext()) {
            val l = it.next()
            if (onEdge != null && !onEdge(k, l)) {
                continue
            }
            q.addLast(l)
        }
    }
}

fun <K> bfsFirstVisitOnly(
    keys: Set<K>,
    links: FetchEdgesFunction<K>,
    onNode: ((K) -> Boolean)? = null,
    onEdge: ((K, K) -> Boolean)? = null,
) {
    val visitedSet = mutableSetOf<K>()
    bfs(
        keys,
        links,
        onEdge = onEdge,
        onNode = { k -> visitedSet.add(k) && (onNode == null || onNode(k)) }
    )
}
