/*
 * Lincheck
 *
 * Copyright (C) 2019 - 2021 JetBrains s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>
 */

package org.jetbrains.kotlinx.lincheck.test.verifier.durable

import org.jetbrains.kotlinx.lincheck.*
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.nvm.Recover
import org.jetbrains.kotlinx.lincheck.nvm.api.nonVolatile
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.*
import org.jetbrains.kotlinx.lincheck.strategy.stress.*
import org.jetbrains.kotlinx.lincheck.test.verifier.nlr.AbstractNVMLincheckTest
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState

private const val THREADS = 3

class DurableStackTest : AbstractNVMLincheckTest(Recover.DURABLE, THREADS, SequentialStack::class, true) {
    private val stack = DurableStack()

    @Operation
    fun push(v: Int): Unit = stack.push(v)

    @Operation
    fun pop(): Int? = stack.pop()

//    override fun <O : Options<O, *>> O.customize() {
//        actorsBefore(1)
//        threads(2)
//        actorsPerThread(1)
//        actorsAfter(1)
//    }
}

internal class SequentialStack : VerifierState() {
    private val stack = ArrayList<Int>()

    fun push(v: Int) { stack.add(v) }
    fun pop(): Int? = stack.removeLastOrNull()

    override fun extractState() = stack
}

private class Node(val next: Node? = null, val value: Int = 0)

internal class DurableStack {
    private val head = nonVolatile<Node?>(null)

    fun push(v: Int) {
        while (true) {
            val cur = head.value
            if (head.compareAndSet(cur, Node(cur, v))) break
        }
        head.flush()
    }

    fun pop(): Int? {
        while (true) {
            val cur = head.value
            if (cur == null) return null
            val next = cur.next
            if (head.compareAndSet(cur, next)) {
                head.flush()
                return cur.value
            }
        }
    }
}
