package me.fru1t.sqlite.clause.constraint

import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.Constraint
import me.fru1t.sqlite.clause.IndexedColumnGroup
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict
import kotlin.reflect.KProperty1

/** Creates a [PrimaryKey] with a column group, specifying [onConflict]. */
infix fun <T : TableColumns<T>> IndexedColumnGroup<T>.onConflict(
    onConflict: OnConflict): PrimaryKey<T> =
  PrimaryKey(this, onConflict)

/** Creates a [PrimaryKey] with a single column, specifying [onConflict]. */
infix fun <T : TableColumns<T>> KProperty1<T, *>.onConflict(onConflict: OnConflict): PrimaryKey<T> =
  PrimaryKey(IndexedColumnGroup(this), onConflict)

/**
 * Represents the [`PRIMARY KEY`][PrimaryKey] constraint clause in an SQL `CREATE TABLE` statement.
 */
data class PrimaryKey<T : TableColumns<T>>(
    val columnGroup: IndexedColumnGroup<T>, val onConflict: OnConflict) : Constraint<T> {
  companion object {
    private const val PRIMARY_KEY_CLAUSE = "CONSTRAINT PRIMARY KEY%s"
  }

  /** Creates a [PrimaryKey] with only a single [column] resolving conflicts with [onConflict]. */
  constructor(column: KProperty1<T, *>, onConflict: OnConflict) :
      this(IndexedColumnGroup(column), onConflict)

  /** Example: ``CONSTRAINT PRIMARY KEY(`id` DESC, `post_id`) ON CONFLICT ABORT``. */
  override fun getClause(): String =
    PRIMARY_KEY_CLAUSE.format(columnGroup.getClause()) + " " + onConflict.getClause()
}
