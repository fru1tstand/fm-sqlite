package me.fru1t.sqlite.clause.constraint

import me.fru1t.sqlite.clause.DataType
import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.Sqlite
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.Constraint

/**
 * Creates a named [Check] constraint with the name of the calling [String] with the sql logic of
 * the passed [String]. See [Check] for example usage.
 */
infix fun <T : TableColumns<T>> String.checks(sqlLogicClause: String) =
  Check<T>(sqlLogicClause, this)

/**
 * Declares a [`CHECK`][Check] [Constraint] on a [TableColumns] [T] [TableDefinition]. [Check]
 * constraints are added via [TableDefinition.Builder.constraint]. The optional [name] should be a
 * table-unique sqlite-compliant constraint name (alphanumeric and underscores), preferably starting
 * with `ck`. The [sqlLogicClause] should be a check constraint compliant logic clause.
 *
 * Example usage:
 * ```
 * // Example table
 * data class Table(val foo: String, val bar: String) : TableColumns<Table>()
 *
 * // Creating a TableDefinition] with a check constraint.
 * val definition = TableDefinition.of(Table::class)
 *     .constraint(
 *         "ck_non_matching_columns" checks
 *             "`${Table::foo.getSqlName()}` != `${Table::bar.getSqlName()}`")
 * ```
 *
 *
 * Documentation from [https://www.sqlite.org/lang_createtable.html#constraints]:
 *
 * A [`CHECK`][Check] constraint may be attached to a column definition or specified as a table
 * constraint. In practice it makes no difference. Each time a new row is inserted into the table
 * or an existing row is updated, the expression associated with each [`CHECK`][Check] constraint
 * is evaluated and cast to a [`NUMERIC`][DataType.NUMERIC] value in the same way as a `CAST`
 * expression. If the result is zero (integer value `0` or real value `0.0`), then a constraint
 * violation has occurred. If the [`CHECK`][Check] expression evaluates to `NULL`, or any other
 * non-zero value, it is not a constraint violation. The expression of a [`CHECK`][Check]
 * constraint may not contain a subquery.
 */
data class Check<T : TableColumns<T>>(
    val sqlLogicClause: String, val name: String? = null) : Constraint<T> {
  companion object {
    private const val NAMED_CHECK_CONSTRAINT = "CONSTRAINT `%s` CHECK (%s)"
    private const val UNNAMED_CHECK_CONSTRAINT = "CONSTRAINT CHECK (%s)"
  }

  init {
    if (name?.matches(Sqlite.VALID_SQL_NAME_REGEX) == false) {
      throw LocalSqliteException(
          "Invalid check constraint name <`$name`>, it must follow the regex " +
              Sqlite.VALID_SQL_NAME_REGEX.pattern)
    }
    if (sqlLogicClause.isBlank()) {
      throw LocalSqliteException("Cannot have blank check constraints.")
    }
  }

  /** Example: `CONSTRAINT CHECK (1 = 1)` or ``CONSTRAINT `ck_example` CHECK (1 = 1)``. */
  override fun getClause(): String {
    return when (name) {
      null -> UNNAMED_CHECK_CONSTRAINT.format(sqlLogicClause)
      else -> NAMED_CHECK_CONSTRAINT.format(name, sqlLogicClause)
    }
  }

  /** Example: `ck_example` or `null`. */
  override fun getConstraintName(): String? = name

  override fun toString(): String = "Check '$sqlLogicClause'"
}
