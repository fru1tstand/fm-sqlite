package me.fru1t.sqlite.clause.constraint.table

import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.Sqlite
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.constraint.TableConstraint

/**
 * Creates a named [Check] constraint with the name of the calling [String] with the sql logic of
 * the passed [String].
 *
 * Example usage:
 * ``"ck_non_matching_columns" checks "`${Table::a.getSqlName()}` != `${Table::b.getSqlName()}`"``
 */
infix fun <T : TableColumns> String.checks(sqlLogicClause: String) =
  Check<T>(sqlLogicClause, this)

/**
 * Declares a [`CHECK`][Check] constraint for [TableColumns] [T]. [Check] constraints are executed
 * every row modification (`INSERT` and `UPDATE`) on the modified row. The optional [name] should
 * be a table-unique sqlite-compliant constraint name (alphanumeric and underscores), preferably
 * starting with `ck`. The [sqlLogicClause] should be a check constraint compliant logic clause.
 *
 * See [https://www.sqlite.org/lang_createtable.html#constraints] for official documentation.
 */
data class Check<T : TableColumns>(
    val sqlLogicClause: String, val name: String? = null) : TableConstraint<T> {
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
