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
@file:JvmName("ExecutionScenarioKtCommon")
package org.jetbrains.kotlinx.lincheck.execution

import org.jetbrains.kotlinx.lincheck.Actor
import kotlin.jvm.*

/**
 * This class represents an execution scenario, which
 * is generated by an [ExecutionGenerator] and then
 * used by a [Strategy] which produces an [ExecutionResult].
 */
class ExecutionScenario(
        /**
         * The initial sequential part of the execution.
         * It helps to produce different initial states
         * before the parallel part.
         *
         * The initial execution part should contain only non-suspendable actors;
         * otherwise, the single initial execution thread will suspend with no chance to be resumed.
         */
        val initExecution: List<Actor>,
        /**
         * The parallel part of the execution, which is used
         * to find an interleaving with incorrect behaviour.
         */
        val parallelExecution: List<List<Actor>>,
        /**
         * The last sequential part is used to test that
         * the data structure is in some correct state.
         *
         * If this execution scenario contains suspendable actors, the post part should be empty;
         * if not, an actor could resume a previously suspended one from the parallel execution part.
         */
        val postExecution: List<Actor>
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.appendExecutionScenario(this)
        return sb.toString()
    }
}

/**
 * Returns the number of threads used in the parallel part of this execution.
 */
val ExecutionScenario.threads: Int
    get() = parallelExecution.size

/**
 * Returns `true` if there is at least one suspendable actor in the generated scenario
 */
fun ExecutionScenario.hasSuspendableActors() = parallelExecution.any { actors -> actors.any { it.isSuspendable } } || postExecution.any { it.isSuspendable }

internal fun <T> printInColumnsCustom(
    groupedObjects: List<List<T>>,
    joinColumns: (List<String>) -> String
): String {
    val nRows = groupedObjects.map { it.size }.max() ?: 0
    val nColumns = groupedObjects.size
    val rows = (0 until nRows).map { rowIndex ->
        (0 until nColumns)
            .map { groupedObjects[it] }
            .map { it.getOrNull(rowIndex)?.toString().orEmpty() } // print empty strings for empty cells
    }
    val columnWidths: List<Int> = (0 until nColumns).map { columnIndex ->
        (0 until nRows).map { rowIndex -> rows[rowIndex][columnIndex].length }.max() ?: 0
    }
    return (0 until nRows)
        .map { rowIndex -> rows[rowIndex].mapIndexed { columnIndex, cell -> cell.padEnd(columnWidths[columnIndex]) } }
        .map { rowCells -> joinColumns(rowCells) }
        .joinToString(separator = "\n")
}

internal fun <T> printInColumns(groupedObjects: List<List<T>>) = printInColumnsCustom(groupedObjects) { it.joinToString(separator = " | ", prefix = "| ", postfix = " |") }

internal fun StringBuilder.appendExecutionScenario(scenario: ExecutionScenario): StringBuilder {
    if (scenario.initExecution.isNotEmpty()) {
        appendLine("Execution scenario (init part):")
        appendLine(scenario.initExecution)
    }
    if (scenario.parallelExecution.isNotEmpty()) {
        appendLine("Execution scenario (parallel part):")
        append(printInColumns(scenario.parallelExecution))
        appendLine()
    }
    if (scenario.postExecution.isNotEmpty()) {
        appendLine("Execution scenario (post part):")
        append(scenario.postExecution)
    }
    return this
}