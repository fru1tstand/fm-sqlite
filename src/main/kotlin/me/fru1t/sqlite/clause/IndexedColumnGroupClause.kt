package me.fru1t.sqlite.clause

import me.fru1t.sqlite.Clause
import me.fru1t.sqlite.Order
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.getSqlName
import kotlin.reflect.KProperty1

/** Creates an [IndexedColumn] with an [order]. */
infix fun <T : TableColumns<T>> KProperty1<T, *>.order(order: Order): IndexedColumn<T> =
  IndexedColumn(this, order)

/** Creates an [IndexedColumnGroup] with [column].  */
infix fun <T : TableColumns<T>> KProperty1<T, *>.and(
    column: KProperty1<T, *>): IndexedColumnGroup<T> =
  IndexedColumn(this) and IndexedColumn(column)

/** Creates an [IndexedColumnGroup] with [indexedColumn]. */
infix fun <T : TableColumns<T>> KProperty1<T, *>.and(
    indexedColumn: IndexedColumn<T>): IndexedColumnGroup<T> =
  IndexedColumn(this) and indexedColumn

/**
 * A clause that groups one or more columns in the [TableColumns] definition [T]. Each column may
 * optionally contain an [Order].
 */
interface IndexedColumnGroupClause<T : TableColumns<T>> : Clause {
  /**
   * Fetches the complete column group sql clause with column [Order] if specified. For example
   * ``(`example_user_id` DESC, `example_post`, `example_target` ASC)``.
   */
  override fun getClause(): String

  /**
   * Fetches the complete column group sql clause without each column's respective [Order]. For
   * example, ``(`example_user_id`, `example_post`, `example_target`)``.
   */
  fun getClauseWithoutOrder(): String

  /** Retrieves all [IndexedColumn]s associated to this group, in order. */
  fun getIndexedColumns(): List<IndexedColumn<T>>
}

/**
 * Represents the [`indexed-column`][https://www.sqlite.org/syntax/indexed-column.html] clause that
 * holds a [column] and optionally an [order]. The base use case for these are the `UNIQUE` and
 * `PRIMARY KEY` constraints, but may have other column-order applications.
 */
data class IndexedColumn<T : TableColumns<T>>(
    val column: KProperty1<T, *>, val order: Order? = null) : IndexedColumnGroupClause<T> {
  override fun getClause(): String = "(${getClauseWithoutGroup()})"

  override fun getClauseWithoutOrder(): String = "(`${column.getSqlName()}`)"

  override fun getIndexedColumns(): List<IndexedColumn<T>> = listOf(this)

  fun getClauseWithoutGroup(): String =
    "`${column.getSqlName()}`" + (order?.let { " ${it.value}" } ?: "")

  /** Creates an [IndexedColumnGroup] with this [IndexedColumn] and [another][indexedColumn]. */
  infix fun and(indexedColumn: IndexedColumn<T>): IndexedColumnGroup<T> =
      IndexedColumnGroup(this) and indexedColumn

  /** Creates an [IndexedColumnGroup] with this [IndexedColumn] and [another][column]. */
  infix fun and(column: KProperty1<T, *>): IndexedColumnGroup<T> =
      IndexedColumnGroup(this) and IndexedColumn(column)
}

/**
 * Represents the repeated [`indexed-column`][https://www.sqlite.org/syntax/indexed-column.html]
 * clause. The base use case for these are the `UNIQUE` and `PRIMARY KEY` constraints, but may other
 * column-grouping applications.
 */
class IndexedColumnGroup<T : TableColumns<T>> (
    initialIndexedColumn: IndexedColumn<T>) : IndexedColumnGroupClause<T> {
  /** Creates an [IndexedColumnGroup] from a single column. */
  constructor(initialColumn: KProperty1<T, *>) : this(IndexedColumn(initialColumn))

  private val columns = ArrayList<IndexedColumn<T>>()

  init {
    columns.add(initialIndexedColumn)
  }

  override fun getClause(): String = "(${columns.joinToString { it.getClauseWithoutGroup() }})"

  override fun getClauseWithoutOrder(): String =
    "(${columns.joinToString { "`${it.column.getSqlName()}`" }})"

  override fun getIndexedColumns(): List<IndexedColumn<T>> = columns


  /** Adds an [indexedColumn] to the end of this [IndexedColumnGroup]. */
  infix fun and(indexedColumn: IndexedColumn<T>): IndexedColumnGroup<T> {
    columns.add(indexedColumn)
    return this
  }

  /** Adds a column to the end of this [IndexedColumnGroup]. */
  infix fun and(column: KProperty1<T, *>): IndexedColumnGroup<T> {
    columns.add(IndexedColumn(column))
    return this
  }
}
