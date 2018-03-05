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
class Unique<T : TableColumns> private constructor(
    initialColumn: KProperty1<T, *>) :
    IndexedColumnGroup<T, Unique<T>>(initialColumn), TableConstraint<T> {
  companion object {
    private const val SQL_CLAUSE = "CONSTRAINT `%s` UNIQUE %s"
    private const val CONSTRAINT_NAME = "uq_%s"

    /**
     * Creates a [`UNIQUE`][Unique] constraint from a single [column] using the
     * [default][OnConflict.DEFAULT] [OnConflict] resolution strategy.
     *
     * Example usage: `Unique on Table::a`.
     */
    infix fun <T : TableColumns> on(column: KProperty1<T, *>): Unique<T> = Unique(column)
  }

  var onConflict: OnConflict? = null
    private set

  /** Specifies the [onConflict] resolution strategy for this [Unique] constraint. */
  infix fun onConflict(onConflict: OnConflict): Unique<T> {
    this.onConflict = onConflict
    return this
  }

  /** Example: ``CONSTRAINT `uq_a_b` UNIQUE (`a` ASC, `b` ASC) ON CONFLICT ROLLBACK``. */
  override fun getClause(): String =
    SQL_CLAUSE.format(getConstraintName(), super.getClause()) +
        onConflict?.let { " ${it.getClause()}" }.orEmpty()

  /** Example: `uq_id_post_id`. */
  override fun getConstraintName(): String =
    CONSTRAINT_NAME.format(
        columns().joinToString(separator = "_", transform = { it.column.getSqlName() }))

  override fun toString(): String =
    "Unique on ${super.getClause()}" + onConflict?.let { " onConflict=${it.sqlName}" }.orEmpty()
}
