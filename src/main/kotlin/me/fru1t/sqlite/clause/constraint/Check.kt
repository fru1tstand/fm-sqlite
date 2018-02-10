package me.fru1t.sqlite.clause.constraint

import me.fru1t.sqlite.annotation.DataType

/**
 * Declares a [`CHECK`][Check] constraint on a [Table]. [`CHECK`][Check] constraints are declared
 * as fields within the companion object of a [Table] implementation. The name of the field isn't
 * used for anything, but it'd be nice to name them following standard convention:
 * `CK_<something meaningful>`
 *
 * Example usage:
 * ```
 * data class ExampleTable(
 *     @Column(TEXT) val username: String,
 *     @Column(TEXT) val email: String
 * ) extends TableColumns<ExampleTable>() {
 *   companion object {
 *     val CK_HAS_EITHER_USERNAME_OR_EMAIL = Check("`username` IS NOT NULL OR `email` IS NOT NULL")
 *   }
 * }
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
data class Check(val sqlLogicClause: String) {
  /**
   * Returns the SQL clause to create this [Check] constraint from a `CREATE TABLE` statement.
   * Returns an empty string if the [sqlLogicClause] is empty.
   */
  fun getConstraintClause(): String {
    return if (sqlLogicClause.isEmpty()) "" else SQL_CLAUSE.format(sqlLogicClause)
  }

  companion object {
    private const val SQL_CLAUSE = "CHECK (%s)"
  }
}
