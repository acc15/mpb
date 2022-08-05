package ru.vm.mpb.util

import java.util.Deque
import kotlin.NoSuchElementException
import kotlin.collections.HashMap

class HashedLinkedSet<K>: AbstractMutableSet<K>(), Deque<K> {

    private val map: OrderedHashMap<K, Unit> = OrderedHashMap()

    override val size: Int
        get() = map.size

    override fun iterator() = map.iterator({ it.key }, true)
    override fun descendingIterator() = map.iterator({ it.key }, false)

    override fun add(element: K): Boolean = map.put(element, Unit) == null
    override fun clear() = map.clear()
    override fun remove(element: K): Boolean = map.remove(element) != null
    override fun offer(e: K): Boolean = add(e)

    override fun addFirst(e: K) { map.putFirst(e, Unit) }
    override fun addLast(e: K) { map.putLast(e, Unit) }
    override fun push(e: K) = addFirst(e)

    override fun offerFirst(e: K): Boolean = map.putFirst(e, Unit) == null
    override fun offerLast(e: K): Boolean = map.putLast(e, Unit) == null

    override fun removeFirst(): K = map.removeFirst().key
    override fun removeLast(): K = map.removeLast().key
    override fun remove(): K = removeFirst()
    override fun pop(): K = removeFirst()

    override fun pollFirst(): K? = map.pollFirst()?.key
    override fun pollLast(): K? = map.pollLast()?.key
    override fun poll(): K? = pollFirst()

    override fun peekFirst(): K? = map.firstKey
    override fun peekLast(): K? = map.lastKey
    override fun peek(): K? = peekFirst()

    override fun getFirst(): K = map.firstKey ?: throw NoSuchElementException()
    override fun getLast(): K = map.lastKey ?: throw NoSuchElementException()
    override fun element(): K = first

    override fun removeFirstOccurrence(o: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeLastOccurrence(o: Any?): Boolean {
        TODO("Not yet implemented")
    }

}
