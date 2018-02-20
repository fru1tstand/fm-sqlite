package me.fru1t.sqlite.clause.resolutionstrategy

import me.fru1t.sqlite.Clause
import me.fru1t.sqlite.clause.constraint.Check
import me.fru1t.sqlite.clause.constraint.ForeignKey
import me.fru1t.sqlite.clause.constraint.Unique

/**
 * The [`ON CONFLICT`][OnConflict] clause is not a separate SQL command. It is a non-standard clause
 * that can appear in many other SQL commands. It is given its own section in this document because
 * it is not part of standard SQL and therefore might not be familiar. See [OnForeignKeyConflict]
 * for foreign key conflict resolution strategies (eg. `ON UPDATE` and `ON DELETE`).
 *
 * The syntax for the [`ON CONFLICT`][OnConflict] clause is as shown above for the `CREATE TABLE`
 * command. For the `INSERT` and `UPDATE` commands, the keywords "`ON CONFLICT`" are replaced by
 * "`OR`" so that the syntax reads more naturally. For example, instead of "`INSERT ON CONFLICT
 * IGNORE`" we have "`INSERT OR IGNORE`". The keywords change but the meaning of the clause is the
 * same either way.
 *
 * The [`ON CONFLICT`][OnConflict] clause applies to [`UNIQUE`][Unique], `NOT NULL`,
 * [`CHECK`][Check], and [`PRIMARY KEY`][PrimaryKey] constraints. The [`ON CONFLICT`][OnConflict]
 * algorithm does not apply to [`FOREIGN KEY`][ForeignKey] constraints. There are five conflict
 * resolution algorithm choices: [ROLLBACK], [ABORT], [FAIL], [IGNORE], and [REPLACE]. The default
 * conflict resolution algorithm is [ABORT].
 *
 * This documentation is taken from [https://sqlite.org/lang_conflict.html].
 */
enum class OnConflict(val sqlName: String) : Clause {
  /**
   * When an applicable constraint violation occurs, the [ROLLBACK] resolution algorithm aborts the
   * current SQL statement with an [SQLITE_CONSTRAINT][org.sqlite.SQLiteErrorCode.SQLITE_CONSTRAINT]
   * error and rolls back the current transaction. If no transaction is active (other than the
   * implied transaction that is created on every command) then the [ROLLBACK] resolution algorithm
   * works the same as the [ABORT] algorithm.
   */
  ROLLBACK("ROLLBACK"),

  /**
   * When an applicable constraint violation occurs, the [ABORT] resolution algorithm aborts the
   * current SQL statement with an [SQLITE_CONSTRAINT][org.sqlite.SQLiteErrorCode.SQLITE_CONSTRAINT]
   * error and backs out any changes made by the current SQL statement; but changes caused by prior
   * SQL statements within the same transaction are preserved and the transaction remains active.
   * This is the default behavior and the behavior specified by the SQL standard.
   */
  ABORT("ABORT"),

  /**
   * When an applicable constraint violation occurs, the [FAIL] resolution algorithm aborts the
   * current SQL statement with an [SQLITE_CONSTRAINT][org.sqlite.SQLiteErrorCode.SQLITE_CONSTRAINT]
   * error. But the [FAIL] resolution does not back out prior changes of the SQL statement that
   * failed nor does it end the transaction. For example, if an `UPDATE` statement encountered a
   * constraint violation on the 100th row that it attempts to update, then the first 99 row changes
   * are preserved but changes to rows 100 and beyond never occur.
   */
  FAIL("FAIL"),

  /**
   * When an applicable constraint violation occurs, the [IGNORE] resolution algorithm skips the one
   * row that contains the constraint violation and continues processing subsequent rows of the SQL
   * statement as if nothing went wrong. Other rows before and after the row that contained the
   * constraint violation are inserted or updated normally. No error is returned when the [IGNORE]
   * conflict resolution algorithm is used.
   */
  IGNORE("IGNORE"),

  /**
   * When a [Unique] or [PrimaryKey] constraint violation occurs, the [REPLACE] algorithm deletes
   * pre-existing rows that are causing the constraint violation prior to inserting or updating the
   * current row and the command continues executing normally. If a `NOT NULL` constraint violation
   * occurs, the [REPLACE] conflict resolution replaces the `NULL` value with the default value for
   * that column, or if the column has no default value, then the [ABORT] algorithm is used. If a
   * [`CHECK`][Check] constraint violation occurs, the [REPLACE] conflict resolution algorithm
   * always works like [ABORT].
   *
   * When the [REPLACE] conflict resolution strategy deletes rows in order to satisfy a constraint,
   * delete triggers fire if and only if recursive triggers are enabled.
   *
   * The update hook is not invoked for rows that are deleted by the [REPLACE] conflict resolution
   * strategy. Nor does [REPLACE] increment the change counter. The exceptional behaviors defined in
   * this paragraph might change in a future release.
   */
  REPLACE("REPLACE");

  companion object {
    private const val SQL_CLAUSE = "ON CONFLICT %s"

    /** Alias of [OnConflict.ABORT]. */
    val DEFAULT = OnConflict.ABORT
  }

  /** Example: `ON CONFLICT ABORT`. */
  override fun getClause(): String{
    return SQL_CLAUSE.format(sqlName)
  }
}
