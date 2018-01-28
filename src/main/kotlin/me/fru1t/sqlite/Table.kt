package me.fru1t.sqlite

import me.fru1t.sqlite.annotation.Column
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

/**
 * Base class for SQLite database tables. Tables must be defined as kotlin data classes whose
 * constructor contains the table's columns annotated with @[Column]. When written to the database,
 * tables and columns will be named by their class and field names following the convention used in
 * [getTableName] and [getColumnName], respectively.
 *
 * Example usage:
 * ```
 * data class Foo(@Column(INTEGER) val id: Int)
 * ```
 *
 * Constraint information
 *  + [Primary Key][me.fru1t.sqlite.constraint.PrimaryKey]
 *  + [Foreign Keys][me.fru1t.sqlite.constraint.ForeignKey]
 */
abstract class Table<T : Table<T>> {
  companion object {
    /**
     * Returns the proper database name for a [table] as a [String] by converting the pascal case
     * data class name to snake case. For example, `ThisExampleTableName` will be converted to
     * `this_example_table_name`.
     */
    fun <T : Table<T>> getTableName(table: KClass<T>): String {
      return convertCamelCaseToSnakeCase(table.simpleName!!)
    }

    /**
     * Returns the proper database name for a [column] as a [String]. by converting the camel case
     * field name to snake case. For example, `thisExampleColumnName` will be converted to
     * `this_example_column_name`.
     */
    fun <T : Table<T>> getColumnName(column: KProperty1<T, *>): String {
      return convertCamelCaseToSnakeCase(column.name)
    }

    /**
     * Retrieves the [Column] annotation for the given [column] belonging to a table.
     *
     * Take the following code snippet, for example:
     * `data class Foo(@Column(INTEGER) val bar: String)`
     *
     * One would want to believe one could write `Foo::bar.findAnnotation<Column>`
     * however, the reference `Foo::bar` isn't referencing the constructor, it's referencing the
     * generated field within Foo `val bar` which doesn't have the annotation.
     * Two solutions:
     *   1) Repeat the annotation by defining it in the field as well:
     *      `data class Foo(@property:Column(INTEGER) @Column(INTEGER) bar)`
     *      which would make our call `Foo::bar.findAnnotation<Column>` work.
     *   2) Create a helper method that extracts the constructor's annotation from a generated
     *      field.
     *
     * This method solves #2, that is, one can use [getColumnAnnotation] by passing in the field
     * [KProperty1] like `getColumnAnnotation(Foo::bar)`.
     */
    fun <T : Table<T>> getColumnAnnotation(column: KProperty1<T, *>): Column {
      val table = getTableFromColumn(column)
      return table.primaryConstructor!!.findParameterByName(column.name)!!.findAnnotation()
          ?: throw LocalSqliteException(
              "Missing @Column annotation on <${table.qualifiedName}#${column.name}>")
    }

    /** Returns the table [KClass] that the [column] belongs to. */
    @Suppress("UNCHECKED_CAST")
    fun <T : Table<T>> getTableFromColumn(column: KProperty1<T, *>): KClass<T> {
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
