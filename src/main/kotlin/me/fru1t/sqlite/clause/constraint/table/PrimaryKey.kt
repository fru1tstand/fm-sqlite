package me.fru1t.sqlite.clause.constraint.table

import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.IndexedColumnGroup
import me.fru1t.sqlite.clause.constraint.TableConstraint
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
class PrimaryKey<T : TableColumns> private constructor(
    initialColumn: KProperty1<T, *>) :
    IndexedColumnGroup<T, PrimaryKey<T>>(initialColumn), TableConstraint<T> {
  companion object {
    private const val PRIMARY_KEY_CLAUSE = "CONSTRAINT PRIMARY KEY%s"

    /**
     * Creates a [`PRIMARY KEY`][PrimaryKey] constraint from a single [column] using the
     * [default][OnConflict.DEFAULT] [OnConflict] resolution strategy.
     *
     * Example usage: `PrimaryKey on Table::a`.
     */
    infix fun <T : TableColumns> on(column: KProperty1<T, *>): PrimaryKey<T> = PrimaryKey(column)
  }

  var onConflict: OnConflict? = null
    private set

  /** Specifies the [onConflict] resolution strategy for this [PrimaryKey] constraint. */
  infix fun onConflict(onConflict: OnConflict): PrimaryKey<T> {
    this.onConflict = onConflict
    return this
  }

  /** Example: ``CONSTRAINT PRIMARY KEY(`id` DESC, `post_id`) ON CONFLICT ABORT``. */
  override fun getClause(): String =
    PRIMARY_KEY_CLAUSE.format(super.getClause()) +
        onConflict?.let { " ${it.getClause()}" }.orEmpty()

  /** This will always return `null`. */
  override fun getConstraintName(): String? = null

  override fun toString(): String =
    "Primary Key on ${super.getClause()}" +
        onConflict?.let { " onConflict=${it.sqlName}" }.orEmpty()
}
