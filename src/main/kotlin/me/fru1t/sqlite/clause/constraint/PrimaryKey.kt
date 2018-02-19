package me.fru1t.sqlite.clause.constraint

import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.Constraint
import me.fru1t.sqlite.clause.IndexedColumnGroup
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict
import kotlin.reflect.KProperty1

/**
 * Represents a grouping of one or more columns by which the Sqlite engine will store as the "key"
 * to a table row. More technically, this represents the `PRIMARY KEY` constraint on a
 * `CREATE TABLE` statement. If one attempts to insert a row that matches another primary key, the
 * [onConflict] resolution strategy is followed.
 *
 * See [https://www.sqlite.org/lang_createtable.html#constraints] for official documentation.
 */
data class PrimaryKey<T : TableColumns<T>>(
    val columnGroup: IndexedColumnGroup<T>, val onConflict: OnConflict) : Constraint<T> {
  companion object {
    private const val PRIMARY_KEY_CLAUSE = "CONSTRAINT PRIMARY KEY%s"

    /**
     * Creates a [`PRIMARY KEY`][PrimaryKey] constraint from a [columnGroup] using the
     * [default][OnConflict.DEFAULT] [OnConflict] resolution strategy.
     *
     * Example usage: `PrimaryKey from (Table::a and (Table::b order DESC))`.
     */
    infix fun <T : TableColumns<T>> from(columnGroup: IndexedColumnGroup<T>): PrimaryKey<T> =
      PrimaryKey(columnGroup, OnConflict.DEFAULT)

    /**
     * Creates a [`PRIMARY KEY`][PrimaryKey] constraint from a single [column] using the
     * [default][OnConflict.DEFAULT] [OnConflict] resolution strategy.
     *
     * Example usage: `PrimaryKey from Table::a`.
     */
    infix fun <T : TableColumns<T>> from(column: KProperty1<T, *>): PrimaryKey<T> =
      PrimaryKey(IndexedColumnGroup(column), OnConflict.DEFAULT)
  }

  /** Specifies the [onConflict] resolution strategy for this [Unique] constraint. */
  infix fun onConflict(onConflict: OnConflict): PrimaryKey<T> = copy(onConflict = onConflict)

  /** Example: ``CONSTRAINT PRIMARY KEY(`id` DESC, `post_id`) ON CONFLICT ABORT``. */
  override fun getClause(): String =
    PRIMARY_KEY_CLAUSE.format(columnGroup.getClause()) + " " + onConflict.getClause()
}
