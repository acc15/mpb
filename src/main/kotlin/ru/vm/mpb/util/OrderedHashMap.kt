package ru.vm.mpb.util

import kotlin.NoSuchElementException
import kotlin.collections.HashMap

class OrderedHashMap<K, V>: MutableMap<K, V> {

    private val map: HashMap<K, LinkedNode<K, V>> = HashMap()
    private var head: LinkedNode<K, V>? = null
    private var tail: LinkedNode<K, V>? = null
    private var cachedSize: Int = 0

    override val size: Int
        get() = cachedSize

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = EntrySet()

    override val keys: MutableSet<K>
        get() = KeySet()

    override val values: MutableCollection<V>
        get() = ValueCollection()

    override fun put(key: K, value: V): V? = put(key, value, firstOrLast = false)

    fun put(key: K, value: V, firstOrLast: Boolean, relinkExisting: Boolean = false): V? {
        val pn = map[key]
        if (pn != null) {
            val prevValue = pn.value
            pn.value = value
            if (relinkExisting) {
                relinkNodeToFirstOrLast(pn, firstOrLast)
            }
            return prevValue
        }

        map[key] = addNode(key, value, firstOrLast)
        return null
    }

    fun putFirst(key: K, element: V): V? = put(key, element, firstOrLast = true, relinkExisting = true)
    fun putLast(key: K, element: V): V? = put(key, element, firstOrLast = false, relinkExisting = true)

    override fun containsValue(value: V): Boolean {
        var n = head
        while (n != null) {
            if (n.value == value) {
                return true
            }
            n = n.next
        }
        return false
    }

    override fun get(key: K): V? = map[key]?.value

    override fun putAll(from: Map<out K, V>) {
        for (e in from) {
            put(e.key, e.value)
        }
    }

    override fun remove(key: K): V? {
        val n = map.remove(key) ?: return null
        unlinkNode(n)
        return n.value
    }

    override fun containsKey(key: K): Boolean = map.containsKey(key)

    fun makeFirst(key: K): Boolean = make(key, true)
    fun makeLast(key: K): Boolean = make(key, false)

    fun make(key: K, firstOrLast: Boolean): Boolean {
        val n = map[key] ?: return false
        relinkNode(n, if (firstOrLast) null else tail, if (firstOrLast) head else null)
        return true
    }

    fun iteratorAt(key: K, direction: Boolean = true): MutableIterator<Map.Entry<K, V>> = LinkedNodeIterator(direction, { it }, map[key])
    fun subListOf(key: K, direction: Boolean = true): List<Map.Entry<K, V>> = iteratorAt(key, direction).asSequence().toList()

    override fun clear() {
        map.clear()
        head = null
        tail = null
        cachedSize = 0
    }

    override fun isEmpty(): Boolean = head == null

    fun iterator(direction: Boolean): MutableIterator<MutableMap.MutableEntry<K, V>> =
        LinkedNodeIterator(direction, { it }, if (direction) head else tail)

    fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> = iterator(true)
    fun descendingIterator(): MutableIterator<MutableMap.MutableEntry<K, V>> = iterator(false)

    val firstEntry: MutableMap.MutableEntry<K, V>?
        get() = head

    val lastEntry: MutableMap.MutableEntry<K, V>?
        get() = tail

    val firstKey: K?
        get() = head?.key

    val lastKey: K?
        get() = tail?.key

    fun removeFirst(): MutableMap.MutableEntry<K, V>? = removeFirstOrLast(true)
    fun removeLast(): MutableMap.MutableEntry<K, V>? = removeFirstOrLast(false)
    fun removeFirstOrLast(firstOrLast: Boolean): MutableMap.MutableEntry<K, V>? {
        val n = (if (firstOrLast) head else tail) ?: return null
        unlinkNode(n)
        map.remove(n.key)
        return n
    }

    private inner class LinkedNodeIterator<R>(
        val direction: Boolean,
        val mapper: (LinkedNode<K, V>) -> R,
        var nextNode: LinkedNode<K, V>?
    ): MutableIterator<R> {

        var prevNode: LinkedNode<K, V>? = null

        override fun hasNext(): Boolean = nextNode != null

        override fun next(): R {
            val n = nextNode ?: throw NoSuchElementException()
            prevNode = n
            nextNode = getNextNode(n)
            return mapper(n)
        }

        override fun remove() {
            val n = prevNode ?: throw IllegalStateException()
            map.remove(n.key)
            unlinkNode(n)
            prevNode = null
        }

        private fun getNextNode(n: LinkedNode<K, V>): LinkedNode<K, V>? = if (direction) n.next else n.prev
    }

    private fun linkNode(node: LinkedNode<K, V>): LinkedNode<K, V> {
        val p = node.prev
        if (p == null) {
            node.next = head
            head = node
        } else {
            node.next = p.next
            p.next = node
        }

        val n = node.next
        if (n == null) {
            node.prev = tail
            tail = node
        } else {
            node.prev = n.prev
            n.prev = node
        }

        ++cachedSize
        return node
    }

    private fun unlinkNode(node: LinkedNode<K, V>): LinkedNode<K, V> {
        val p = node.prev
        if (p == null) {
            head = node.next
        } else {
            p.next = node.next
        }

        val n = node.next
        if (n == null) {
            tail = node.prev
        } else {
            n.prev = node.prev
        }

        node.prev = null
        node.next = null

        --cachedSize
        return node
    }

    private fun relinkNodeToFirstOrLast(n: LinkedNode<K, V>, firstOrLast: Boolean) {
        relinkNode(n, if (firstOrLast) null else tail, if (firstOrLast) head else null)
    }

    private fun relinkNode(n: LinkedNode<K, V>, prev: LinkedNode<K, V>?, next: LinkedNode<K, V>?) {
        unlinkNode(n)
        n.prev = prev
        n.next = next
        linkNode(n)
    }

    private fun removeFromMap(n: LinkedNode<K, V>): LinkedNode<K, V> {
        if (n.key != null) {
            map.remove(n.key)
        }
        return n
    }

    private fun addNode(key: K, element: V, firstOrLast: Boolean): LinkedNode<K, V> {
        return linkNode(LinkedNode(key, element, if (firstOrLast) null else tail, if (firstOrLast) head else null))
    }

    private fun removeItem(firstOrLast: Boolean): V? {
        val n = (if (firstOrLast) head else tail) ?: return null
        return removeFromMap(unlinkNode(n)).value
    }

    private fun removeByIterator(iter: MutableIterator<V>, o: Any?): Boolean {
        while (iter.hasNext()) {
            if (iter.next() == o) {
                iter.remove()
                return true
            }
        }
        return false
    }

    class LinkedNode<K, V>(
        override val key: K,
        override var value: V,
        var prev: LinkedNode<K, V>? = null,
        var next: LinkedNode<K, V>? = null
    ): MutableMap.MutableEntry<K, V>  {
        override fun setValue(newValue: V): V {
            val prev = value
            value = newValue
            return prev
        }
    }

    inner class EntrySet: AbstractMutableSet<MutableMap.MutableEntry<K, V>>() {
        override fun add(element: MutableMap.MutableEntry<K, V>): Boolean = put(element.key, element.value) == null
        override fun clear() = this@OrderedHashMap.clear()
        override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> = this@OrderedHashMap.iterator()
        override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean = this@OrderedHashMap.remove(element.key) != null
        override fun removeAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean = elements.any { remove(it) }
        override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean = containsKey(element.key)
        override fun containsAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean = elements.all{ contains(it) }
        override fun isEmpty(): Boolean = cachedSize == 0
        override val size: Int
            get() = cachedSize
    }

    inner class KeySet: AbstractMutableSet<K>() {
        override fun add(element: K): Boolean = throw UnsupportedOperationException()
        override fun clear() = this@OrderedHashMap.clear()
        override fun iterator(): MutableIterator<K> = LinkedNodeIterator(true, { it.key }, head)
        override fun remove(element: K): Boolean = this@OrderedHashMap.remove(element) != null
        override fun removeAll(elements: Collection<K>): Boolean = elements.any { remove(it) }
        override fun isEmpty(): Boolean = cachedSize == 0
        override val size: Int
            get() = cachedSize
    }

    inner class ValueCollection: AbstractMutableCollection<V>() {
        override fun add(element: V): Boolean = throw UnsupportedOperationException()
        override fun clear() = this@OrderedHashMap.clear()
        override fun iterator(): MutableIterator<V> = LinkedNodeIterator(true, { it.value }, head)
        override fun isEmpty(): Boolean = cachedSize == 0
        override val size: Int
            get() = cachedSize
    }

}
