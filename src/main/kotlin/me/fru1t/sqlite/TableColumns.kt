package me.fru1t.sqlite

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

/** Converts PascalCase or camelCase into snake_case. */
private fun String.toSnakeCase(): String {
  val result = StringBuilder()
  this.toCharArray().forEachIndexed {
    index, c -> run {
    if (c.isUpperCase() && index != 0) {
      result.append('_')
    }
    result.append(c.toLowerCase())
  }}
  return result.toString()
}

/**
 * Returns the proper database name for this [TableColumns] [KClass] as a [String] by converting the
 * pascal case class name to snake case. For example, `ThisExampleTableName` will be converted to
 * `this_example_table_name`.
 */
fun <T : TableColumns<T>> KClass<T>.getSqlName() = this.simpleName!!.toSnakeCase()

/**
 * Returns the proper database name for a column as a [String] by converting the camel case
 * field name to snake case. For example, `thisExampleColumnName` will be converted to
 * `this_example_column_name`.
 */
fun <T : TableColumns<T>> KProperty1<T, *>.getSqlName() = this.name.toSnakeCase()

/** Returns the [TableColumns] [KClass] that this column property belongs to. */
@Suppress("UNCHECKED_CAST")
fun <T : TableColumns<T>> KProperty1<T, *>.getTable() =
  this.javaField!!.declaringClass.kotlin as KClass<T>

/**
 * Base class for defining columns within an SQLite database table. Columns are defined by declaring
 * parameters within a kotlin data class constructor that extends this [TableColumns] class. Table
 * names are generated from the implementing [TableColumns] class by using the class name.
 *
 * Example usage:
 * ```
 * // Declares a table named "example_table"
 * data class ExampleTable(
 *   val id: Int,                              // id INTEGER NOT NULL
 *   val defaultingColumn: String = "example", // defaulting_column TEXT NOT NULL DEFAULT 'example'
 *   val foo: String?,                         // foo TEXT NULL
 *   val bar: String? = "bar"                  // bar TEXT NULL DEFAULT 'bar'
 *                                             // Note: a ColumnConstraint defining a default must
 *                                             // be added for this column in the CreateTable class
 *   ) : TableColumns<ExampleTable>()
 * ```
 *
 * @param T Pass-through of the implementing class for generic methods in this class.
 */
abstract class TableColumns<T : TableColumns<T>>
