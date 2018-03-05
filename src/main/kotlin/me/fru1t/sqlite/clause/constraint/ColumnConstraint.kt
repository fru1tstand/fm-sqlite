package me.fru1t.sqlite.clause.constraint

import me.fru1t.sqlite.Clause
import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.Collation
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict
import me.fru1t.sqlite.getSqlName
import me.fru1t.sqlite.getTable
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor

/**
 * Constraints on a single column within a [TableColumns] definition [T].
 *
 * @throws LocalSqliteException if the [default] is defined and the [KProperty1] associated to the
 * column is not optional
 * @throws LocalSqliteException if the [notNullOnConflict] is defined and the [KProperty1]
 * associated to the column is nullable
 */
data class ColumnConstraint<T : TableColumns<T>, F>(
    val column: KProperty1<T, F>,
    val default: F?,
    val collation: Collation?,
    val notNullOnConflict: OnConflict?) : Clause {
  companion object {
    private const val NULLABLE_CLAUSE: String = "NULL"
    private const val NOT_NULL_CLAUSE: String = "NOT NULL"
    private const val DEFAULT_CLAUSE: String = "DEFAULT `%s`"

    /** Creates a default, all-null [ColumnConstraint] on [column]. */
    infix fun <T : TableColumns<T>, F> on(column: KProperty1<T, F>): ColumnConstraint<T, F> =
        ColumnConstraint(column, null, null, null)
  }

  init {
    if (default != null) {
      if (!column.getTable().primaryConstructor!!.findParameterByName(column.name)!!.isOptional) {
        throw LocalSqliteException(
            "${column.getTable().simpleName}.${column.name} must be an optional parameter to " +
                "allow default values.")
      }
    }

    if (notNullOnConflict != null) {
      if (column.returnType.isMarkedNullable) {
        throw LocalSqliteException(
            "${column.getTable().simpleName}.${column.name} must be marked non-null in order to " +
                "specify a NOT NULL ON CONFLICT clause.")
      }
    }
  }

  /**
   * Sets the default value for this column. Default values are used when the column
   * isn't defined in an `INSERT` statement and may be used with `NULL` and `NOT NULL` defined
   * columns. The [KProperty1] value underneath the column must be optional.
   */
  infix fun default(default: F?) = copy(default = default)

  /**
   * Sets the collation method for the column. Column-specific collations override table-defined
   * collation methods; however, query-specific collation definitions override column-defined ones.
   */
  infix fun collate(collation: Collation?) = copy(collation = collation)

  /**
   * Sets the [OnConflict] clause on the `NOT NULL` constraint if this column. The [KProperty1]
   * underneath the column must be non-nullable.
   *
   */
  infix fun notNullOnConflict(notNullOnConflict: OnConflict?) =
    copy(notNullOnConflict = notNullOnConflict)

  /**
   * Returns the column definition for a `CREATE TABLE` statement with its column constraints. For
   * example: `` `column` INTEGER NOT NULL DEFAULT `30` ``.
   */
  override fun getClause(): String =
      "`${column.getSqlName()}` ${getNullabilityClause()}" +
          (default?.let { " " + DEFAULT_CLAUSE.format(it.toString().replace("`", "\\`")) } ?: "") +
          (collation?.let { " " + it.getClause() } ?: "")

  private fun getNullabilityClause(): String {
    return if (column.returnType.isMarkedNullable) {
      NULLABLE_CLAUSE
    } else {
      NOT_NULL_CLAUSE + (notNullOnConflict?.let { " " + it.getClause() } ?: "")
    }
  }
}
