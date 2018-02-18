package me.fru1t.sqlite.clause

import me.fru1t.sqlite.Clause
import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.getSqlName
import kotlin.reflect.KProperty1

/** Creates an [IndexedColumnGroup] with a single column given in [order][Order]. */
infix fun <T : TableColumns<T>> KProperty1<T, *>.order(order: Order): IndexedColumnGroup<T> =
  IndexedColumnGroup(listOf(IndexedColumn(this, order)))

/**
 * Creates an [IndexedColumnGroup] by appending the [nextColumn] to this column giving both the
 * [default][Order.DEFAULT] [Order].
 */
infix fun <T : TableColumns<T>> KProperty1<T, *>.and(
    nextColumn: KProperty1<T, *>): IndexedColumnGroup<T> =
  IndexedColumnGroup(
      listOf(IndexedColumn(this, Order.DEFAULT), IndexedColumn(nextColumn, Order.DEFAULT)))

/**
 * Creates an [IndexedColumnGroup] by prepending this column with settings the
 * [default][Order.DEFAULT] [Order] to the [existingGroup].
 */
infix fun <T : TableColumns<T>> KProperty1<T, *>.and(
    existingGroup: IndexedColumnGroup<T>): IndexedColumnGroup<T> =
  IndexedColumnGroup(
      listOf(IndexedColumn(this, Order.DEFAULT), *existingGroup.columns.toTypedArray()))

/**
 * A simple container class that represents a single
 * [`indexed-column`][https://www.sqlite.org/syntax/indexed-column.html], that is, a [column] and
 * an [order].
 */
data class IndexedColumn<T : TableColumns<T>>(val column: KProperty1<T, *>, val order: Order) {
  /** Outputs the [column] sql name and [order] if available. Example: `` `foo` ASC ``. */
  override fun toString(): String = "`${column.getSqlName()}` ${order.getClause()}"
}

/**
 * Represents the repeated [`indexed-column`][https://www.sqlite.org/syntax/indexed-column.html]
 * clause. The base use case for these are the `UNIQUE` and `PRIMARY KEY` constraints, but may other
 * column-grouping applications. [IndexedColumnGroup.columns] will always contain at least one
 * column.
 *
 * @throws LocalSqliteException if no columns are given to the group
 */
data class IndexedColumnGroup<T : TableColumns<T>>(val columns: List<IndexedColumn<T>>) : Clause {
  /**
   * Creates an [IndexedColumnGroup] from an initial column with the [default][Order.DEFAULT]
   * [Order].
   */
  constructor(initialColumn: KProperty1<T, *>) :
      this(listOf(IndexedColumn(initialColumn, Order.DEFAULT)))

  init {
    if (columns.isEmpty()) {
      throw LocalSqliteException("IndexedColumnGroup cannot have zero columns.")
    }
  }

  /** Example: ``(`post_id` DESC, `name` ASC, `email` DESC)``. */
  override fun getClause(): String = "(${getClauseWithoutGrouping()})"

  /** Example: `` `post_id` DESC, `name` ASC, `email` DESC``. */
  fun getClauseWithoutGrouping(): String = columns.joinToString()

  /** Adds another [IndexedColumnGroup] to the end of this [IndexedColumnGroup]. */
  infix fun and(indexedColumnGroup: IndexedColumnGroup<T>): IndexedColumnGroup<T> =
    copy(columns = listOf(*columns.toTypedArray(), *indexedColumnGroup.columns.toTypedArray()))

  /**
   * Adds a [column] with the [default][Order.DEFAULT] [Order] to the end of this
   * [IndexedColumnGroup].
   */
  infix fun and(column: KProperty1<T, *>): IndexedColumnGroup<T> =
    copy(columns = listOf(*columns.toTypedArray(), IndexedColumn(column, Order.DEFAULT)))
}
