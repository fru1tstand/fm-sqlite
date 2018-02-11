package me.fru1t.sqlite

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

/**
 * Base class for SQLite database tables. Tables must be defined as kotlin data classes whose
 * constructor contains the table's columns annotated with @[Column]. When written to the database,
 * tables and columns will be named by their class and field names following the convention used in
 * [getTableName] and [getColumnName], respectively.
 *
 * Example usage:
 * ```
 * data class Foo(@Column(INTEGER) val id: Int) : TableColumns<Foo>
 * ```
 *
 * For adding columns to the table, see [Column].
 *
 * For adding constraints, see the following constraint types:
 *  + [Primary Key][me.fru1t.sqlite.constraint.PrimaryKey]
 *  + [Foreign Keys][me.fru1t.sqlite.constraint.ForeignKey]
 *  + [Check][me.fru1t.sqlite.constraint.Check]
 *  + [Unique][me.fru1t.sqlite.constraint.Unique]
 *
 * @param T Pass-through of the implementing class used to return a proper statically typed class
 * in several of the abstract methods within this class
 * @param withoutRowId Specifies if the `WITHOUT ROWID` is added to the `CREATE TABLE` statement.
 * See [withoutRowId].
 * @param schemaName Specifies a schema name. See [schemaName].
 */
abstract class TableColumns<T : TableColumns<T>>(
    /**
     * By default, every row in SQLite has a special column, usually called the "`rowid`", that
     * uniquely identifies that row within the table. However if the phrase "`WITHOUT ROWID`" is
     * added to the end of a `CREATE TABLE` statement, then the special "`rowid`" column is omitted.
     * There are sometimes space and performance advantages to omitting the rowid.
     *
     * A `WITHOUT ROWID` table is a table that uses a Clustered Index as the primary key.
     *
     * See [https://sqlite.org/withoutrowid.html] for official documentation.
     */
    val withoutRowId: Boolean = false,

    /**
     * If a schema-name is specified, it must be either "`main`", "`temp`", or the name of an
     * attached database. In this case the new table is created in the named database. If the
     * "`TEMP`" or "`TEMPORARY`" keyword occurs between the "`CREATE`" and "`TABLE`" then the new
     * table is created in the temp database. It is an error to specify both a schema-name and the
     * `TEMP` or `TEMPORARY` keyword, unless the schema-name is "`temp`". If no schema name is
     * specified and the `TEMP` keyword is not present then the table is created in the main
     * database.
     *
     * See [https://sqlite.org/lang_createtable.html] for official documentation.
     */
    val schemaName: String? = null) {
  companion object {
    /**
     * Returns the proper database name for a [table] as a [String] by converting the pascal case
     * data class name to snake case. For example, `ThisExampleTableName` will be converted to
     * `this_example_table_name`.
     */
    fun <T : TableColumns<T>> getTableName(table: KClass<T>): String {
      return convertCamelCaseToSnakeCase(table.simpleName!!)
    }

    /**
     * Returns the proper database name for a [column] as a [String]. by converting the camel case
     * field name to snake case. For example, `thisExampleColumnName` will be converted to
     * `this_example_column_name`.
     */
    fun <T : TableColumns<T>> getColumnName(column: KProperty1<T, *>): String {
      return convertCamelCaseToSnakeCase(column.name)
    }

    /** Returns the table [KClass] that the [column] belongs to. */
    @Suppress("UNCHECKED_CAST")
    fun <T : TableColumns<T>> getTableFromColumn(column: KProperty1<T, *>): KClass<T> {
      return column.javaField!!.declaringClass.kotlin as KClass<T>
    }

    private fun convertCamelCaseToSnakeCase(string: String): String {
      val result = StringBuilder()
      string.toCharArray().forEachIndexed {
        index, c -> run {
        if (c.isUpperCase() && index != 0) {
          result.append('_')
        }
        result.append(c.toLowerCase())
      }}
      return result.toString()
    }
  }
}
