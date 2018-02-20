package me.fru1t.sqlite.clause.constraint.table

import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.IndexedColumnGroup
import me.fru1t.sqlite.clause.constraint.TableConstraint
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict
import me.fru1t.sqlite.getSqlName
import kotlin.reflect.KProperty1

/**
 * Represents a grouping of one or more columns whose concatenated values must be unique on the
 * table. More technically, this represents the `UNIQUE` constraint on a `CREATE TABLE` statement.
 * If one attempts to insert a row that violates this [Unique] constraint, the [onConflict]
 * resolution strategy is followed.
 *
 * See [https://www.sqlite.org/lang_createtable.html#constraints] for official documentation.
 */
data class Unique<T : TableColumns<T>>(
    val columnGroup: IndexedColumnGroup<T>, val onConflict: OnConflict) : TableConstraint<T> {
  companion object {
    private const val SQL_CLAUSE = "CONSTRAINT `%s` UNIQUE %s %s"
    private const val CONSTRAINT_NAME = "uq_%s"

    /**
     * Creates a [`UNIQUE`][Unique] constraint from a [columnGroup] using the
     * [default][OnConflict.DEFAULT] [OnConflict] resolution strategy.
     *
     * Example usage: `Unique from (Table::a and (Table::b order DESC))`.
     */
    infix fun <T : TableColumns<T>> from(columnGroup: IndexedColumnGroup<T>): Unique<T> =
      Unique(columnGroup, OnConflict.DEFAULT)

    /**
     * Creates a [`UNIQUE`][Unique] constraint from a single [column] using the
     * [default][OnConflict.DEFAULT] [OnConflict] resolution strategy.
     *
     * Example usage: `Unique from Table::a`.
     */
    infix fun <T : TableColumns<T>> from(column: KProperty1<T, *>): Unique<T> =
      Unique(IndexedColumnGroup(column), OnConflict.DEFAULT)
  }

  /** Specifies the [onConflict] resolution strategy for this [Unique] constraint. */
  infix fun onConflict(onConflict: OnConflict): Unique<T> = copy(onConflict = onConflict)

  /** Example: ``CONSTRAINT `uq_a_b` UNIQUE (`a` ASC, `b` ASC) ON CONFLICT ROLLBACK``. */
  override fun getClause(): String {
    return SQL_CLAUSE.format(getConstraintName(), columnGroup.getClause(), onConflict.getClause())
  }

  /** Example: `uq_id_post_id`. */
  override fun getConstraintName(): String {
    return CONSTRAINT_NAME.format(
        columnGroup.columns.joinToString(separator = "_", transform = { it.column.getSqlName() }))
  }

  override fun toString(): String =
    "Unique on ${columnGroup.getClause()} onConflict=" + onConflict.sqlName
}
