package me.fru1t.sqlite.clause.constraint

import me.fru1t.sqlite.DataType
import me.fru1t.sqlite.TableColumns
import java.util.Arrays
import kotlin.reflect.KProperty1

/**
 * Declares a [Table]'s [`PRIMARY KEY`][PrimaryKey] columns. Only a single primary key group may
 * exist for a single table. A [`PRIMARY KEY`][PrimaryKey] constraint is declared as a field
 * within the companion object of a [Table] implementation. The name of the field isn't used for
 * anything, but it'd be nice to name them something meaningful like `PRIMARY_KEY`.
 *
 * Use [PrimaryKey.of] to create instances of this class.
 *
 * Example usage:
 * ```
 * data class ExampleTable(
 *     @Column(TEXT) val username: String,
 *     @Column(TEXT) val email: String
 * ) extends TableColumns<ExampleTable>() {
 *   companion object {
 *     val PRIMARY_KEY = PrimaryKey.of(ExampleTable::username, ExampleTable::email)
 *   }
 * }
 * ```
 *
 *
 * Documentation from [https://www.sqlite.org/lang_createtable.html#constraints]:
 *
 * Each table in SQLite may have at most one [`PRIMARY KEY`][PrimaryKey]. If the keywords
 * [`PRIMARY KEY`][PrimaryKey] are added to a column definition, then the primary key for the table
 * consists of that single column. Or, if a [`PRIMARY KEY`][PrimaryKey] clause is specified as a
 * table-constraint, then the primary key of the table consists of the list of columns specified as
 * part of the [`PRIMARY KEY`][PrimaryKey] clause. The [`PRIMARY KEY`][PrimaryKey] clause must
 * contain only column names — the use of expressions in an indexed-column of a
 * [`PRIMARY KEY`][PrimaryKey] is not supported. An error is raised if more than one
 * [`PRIMARY KEY`][PrimaryKey] clause appears in a `CREATE TABLE` statement. The
 * [`PRIMARY KEY`][PrimaryKey] is optional for ordinary tables but is required for `WITHOUT ROWID`
 * tables.
 *
 * If a table has a single column primary key and the declared type of that column is
 * "[`INTEGER`][DataType.INTEGER]" and the table is not a `WITHOUT ROWID` table, then the column is
 * known as an `INTEGER PRIMARY KEY`. See below for a description of the special properties and
 * behaviors associated with an `INTEGER PRIMARY KEY`.
 *
 * Each row in a table with a primary key must have a unique combination of values in its primary
 * key columns. For the purposes of determining the uniqueness of primary key values, `NULL` values
 * are considered distinct from all other values, including other `NULL`s. If an `INSERT` or
 * `UPDATE` statement attempts to modify the table content so that two or more rows have identical
 * primary key values, that is a constraint violation.
 *
 * According to the SQL standard, [`PRIMARY KEY`][PrimaryKey] should always imply `NOT NULL`.
 * Unfortunately, due to a bug in some early versions, this is not the case in SQLite. Unless the
 * column is an `INTEGER PRIMARY KEY` or the table is a `WITHOUT ROWID` table or the column is
 * declared `NOT NULL`, SQLite allows `NULL` values in a `PRIMARY KEY` column. SQLite could be fixed
 * to conform to the standard, but doing so might break legacy applications. Hence, it has been
 * decided to merely document the fact that SQLite allowing `NULL`s in most
 * [`PRIMARY KEY`][PrimaryKey] columns.
 */
data class PrimaryKey<T : TableColumns<T>>(val columns: Array<out KProperty1<T, *>>) {
  /**
   * Returns the SQL clause to create this [PrimaryKey] constraint from a `CREATE TABLE` statement.
   * Returns an empty string if no columns are specified for the [PrimaryKey].
   */
  fun getConstraintClause(): String {
    if (columns.isEmpty()) {
      return ""
    }

    val primaryKeyColumnText = StringBuilder()
    columns.forEachIndexed { index, column -> run {
      if (index > 0) {
        primaryKeyColumnText.append(',')
      }
      primaryKeyColumnText.append('`').append(TableColumns.getColumnName(column)).append('`')
    }}
    return SQL_CLAUSE.format(primaryKeyColumnText.toString())
  }

  override fun equals(other: Any?): Boolean {
    // Auto-generated by IntelliJ
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as PrimaryKey<*>
    if (!Arrays.equals(columns, other.columns)) return false
    return true
  }

  override fun hashCode(): Int {
    return Arrays.hashCode(columns)
  }

  companion object {
    private const val SQL_CLAUSE = "PRIMARY KEY(%s)"

    /** Creates a [PrimaryKey] on [Table] ([T]) consisting of [columns]. */
    fun <T : TableColumns<T>> of(vararg columns: KProperty1<T, *>): PrimaryKey<T> {
      return PrimaryKey(columns)
    }
  }
}
