package me.fru1t.sqlite.clause

import me.fru1t.sqlite.Clause
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.getSqlName
import kotlin.reflect.KProperty1

/**
 * A simple container class that represents a single
 * [`indexed-column`][https://www.sqlite.org/syntax/indexed-column.html], that is, a [column] and
 * an [order].
 */
data class IndexedColumn<T : TableColumns>(val column: KProperty1<T, *>, val order: Order) {
  /** Outputs the [column] sql name and [order] if available. Example: `` `foo` ASC ``. */
  override fun toString(): String = "`${column.getSqlName()}` ${order.getClause()}"
}

/**
 * Represents the repeated [`indexed-column`][https://www.sqlite.org/syntax/indexed-column.html]
 * clause. The base use case for these are the `UNIQUE` and `PRIMARY KEY` constraints, but may other
 * column-grouping applications. [IndexedColumnGroup.columns] will always contain at least one
 * column.
 */
open class IndexedColumnGroup<T : TableColumns, S : IndexedColumnGroup<T, S>>(
    initialColumn: KProperty1<T, *>) : Clause {
  private val columns = ArrayList<IndexedColumn<T>>()

  init {
    and(initialColumn)
  }

  /** Retrieves all columns in this group. */
  fun columns(): List<IndexedColumn<T>> = columns

  /**
   * Adds a [column] with [Order.DEFAULT] to the end of this [IndexedColumnGroup].
   *
   * Example usage: `Table::a and Table::b and Table::c`.
   */
  infix fun and(column: KProperty1<T, *>): S {
    columns.add(IndexedColumn(column, Order.DEFAULT))
    @Suppress("UNCHECKED_CAST")
    return this as S
  }

  /**
   * Sets the [Order] of the last [IndexedColumn] within this group.
   *
   * Example usage: `Table::a and Table::b order DESC`.
   */
  infix fun order(order: Order): S {
    columns[columns.size - 1] = columns[columns.size - 1].copy(order = order)
    @Suppress("UNCHECKED_CAST")
    return this as S
  }

  /** Example: ``(`post_id` DESC, `name` ASC, `email` DESC)``. */
  override fun getClause(): String = "(${columns.joinToString()})"
}
