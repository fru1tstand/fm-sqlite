package me.fru1t.sqlite.clause

import me.fru1t.sqlite.Clause
import me.fru1t.sqlite.Order
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.getDatabaseName
import me.fru1t.sqlite.getTable
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor

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
 * A clause that groups one or more columns in sequence. Each column may optionally contain an
 * [Order].
 */
interface IndexedColumnGroupClause : Clause {
  /**
   * Fetches the complete column group sql clause with column [Order] if specified. For example
   * ``(`example_user_id` DESC, `example_post`, `example_target` ASC)``.
   */
  override fun getClause(): String
}

/**
 * Represents the [`indexed-column`][https://www.sqlite.org/syntax/indexed-column.html] clause that
 * holds a [column] and optionally an [order]. The base use case for these are the `UNIQUE` and
 * `PRIMARY KEY` constraints, but may have other column-order applications.
 */
data class IndexedColumn<T : TableColumns<T>>(
    val column: KProperty1<T, *>, val order: Order? = null) : IndexedColumnGroupClause {
  /** Fetches the complete column group sql clause with this single column. */
  override fun getClause(): String = "(${getClauseWithoutGroup()})"

  /** Fetches the [IndexedColumn] sql clause for this single column. */
  fun getClauseWithoutGroup(): String =
    "`${column.getDatabaseName()}`" + (order?.let { " ${it.value}" } ?: "")

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
    initialIndexedColumn: IndexedColumn<T>) : IndexedColumnGroupClause {
  /** Creates an [IndexedColumnGroup] from a single column. */
  constructor(initialColumn: KProperty1<T, *>) : this(IndexedColumn(initialColumn))

  private val _columns = ArrayList<IndexedColumn<T>>(
      initialIndexedColumn.column.getTable().primaryConstructor!!.parameters.size)
  val columns: List<IndexedColumn<T>>
    get() = _columns

  init {
    _columns.add(initialIndexedColumn)
  }


  /**
   * Returns this list of columns and their respective declared [Order]. For example,
   * ``(`col_1` ASC, `col_2` DESC)``.
   */
  override fun getClause(): String = "(${_columns.joinToString { it.getClauseWithoutGroup() }})"

  /**
   * Returns this list of columns without their respective declared [Order]. For example,
   * ``(`col_1`, `col_2`)``.
   */
  fun getColumnOnlyClause(): String =
    "(${_columns.joinToString { "`${it.column.getDatabaseName()}`" }})"


  /** Adds an [indexedColumn] to the end of this [IndexedColumnGroup]. */
  infix fun and(indexedColumn: IndexedColumn<T>): IndexedColumnGroup<T> {
    _columns.add(indexedColumn)
    return this
  }

  /** Adds a column to the end of this [IndexedColumnGroup]. */
  infix fun and(column: KProperty1<T, *>): IndexedColumnGroup<T> {
    _columns.add(IndexedColumn(column))
    return this
  }
}
