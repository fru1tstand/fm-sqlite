package me.fru1t.sqlite.statement

import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.Constraint
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor

/**
 * Represents an Sqlite table. This class is meant to hold the definition of a table in a database.
 *
 * Use [CreateTable.of] to create instances of the builder class.
 */
class CreateTable<T : TableColumns<T>> private constructor(
    val columnsClass: KClass<T>,
    val withoutRowId: Boolean,
    val constraints: List<Constraint<T>>,
    val defaults: Map<KParameter, Any>,
    val autoIncrementColumn: KParameter?) {

  /**
   * A builder class for [CreateTable] which verifies consistency on [build].
   *
   * [columnsClass] must be a data class with a primary constructor that represents the table's
   * columns. See [TableColumns] for details.
   */
  class Builder<T : TableColumns<T>>(val columnsClass: KClass<T>) {
    init {
      if (!columnsClass.isData) {
        throw LocalSqliteException("${columnsClass.simpleName} must be a kotlin data class.")
      }
    }

    /** Specifies if the `WITHOUT ROWID` clause is added to the `CREATE TABLE` statement. */
    var withoutRowId: Boolean = false

    /** Stores the constraints on the table such as `FOREIGN KEY`, `UNIQUE`, etc. */
    var constraints: MutableList<Constraint<T>> = ArrayList()

    /** Stores the column to mark as `AUTOINCREMENT`. */
    private var autoIncrementColumn: KParameter? = null

    /**
     * Stores the default values associated to each column. If a column doesn't have an entry, no
     * default will be defined in the `CREATE TABLE` statement. A default value must be explicitly
     * passed for each column defined as optional within [columnsClass].
     */
    private val defaults: MutableMap<KParameter, Any> = HashMap()

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
    fun withoutRowId(withoutRowId: Boolean): Builder<T> {
      this.withoutRowId = withoutRowId
      return this
    }

    /** Add a single constraint to this [CreateTable.Builder]. */
    fun constraint(constraint: Constraint<T>): Builder<T> {
      constraints.add(constraint)
      return this
    }

    /**
     * Sets a [column] to use `AUTOINCREMENT`. Please read
     * [the official documentation][http://www.sqlite.org/autoinc.html] before opting to use this.
     * In most cases you don't need to, and instead, you can rely on the `ROWID` column.
     *
     * @throws LocalSqliteException if an [autoIncrementColumn] already exists for this [Builder]
     */
    fun autoIncrement(column: KProperty1<T, Int>): Builder<T> {
      if (autoIncrementColumn != null) {
        throw LocalSqliteException(
            "${columnsClass.simpleName} already has an autoincrement column " + "'${autoIncrementColumn!!.name}', cannot set autoincrement on '${column.name}'")
      }
      autoIncrementColumn = columnsClass.primaryConstructor!!.findParameterByName(column.name)
      return this
    }

    /**
     * Add a [`DEFAULT`][default] value to a [column]. This is required as it's impossible to
     * introspectively resolve the default value for optional parameters in kotlin. The passed
     * [column] must be optional and the [default] should be the same as the optional parameter
     * or unexpected behavior will occur.
     *
     * Example usage:
     * ```
     * data class ExampleTable(val id: Int, val value: Int = VALUE) : TableColumns<ExampleTable>() {
     *   companion object {
     *     private const val VALUE = 0
     *     val DEFINITION = CreateTable.of(ExampleTable::class)
     *         .default(ExampleTable::value, VALUE)
     *         .build()
     *   }
     * }
     * ```
     *
     * @throws LocalSqliteException if [column] isn't an optional parameter
     * @throws LocalSqliteException if [column] already has a default defined
     */
    fun <E : Any> default(column: KProperty1<T, E?>, default: E): Builder<T> {
      val columnAsKParameter = columnsClass.primaryConstructor!!.findParameterByName(column.name)!!

      // Must be optional
      if (!columnAsKParameter.isOptional) {
        throw LocalSqliteException(
            "${columnsClass.simpleName}.${column.name} must be an optional parameter to allow " + "default values.")
      }

      // Must not exist already
      defaults.putIfAbsent(columnAsKParameter, default)?.let {
        throw LocalSqliteException(
            "${columnsClass.simpleName}.${column.name} already has the default value of '$it', " + "cannot set it to '$default'")
      }

      return this
    }

    /** Creates the [CreateTable] from the current state of this [Builder]. */
    fun build(): CreateTable<T> {
      // Optional parameters must have a default defined.
      val optionalColumns = columnsClass.primaryConstructor!!.parameters.filter { it.isOptional }
      optionalColumns.forEach {
        if (!defaults.containsKey(it)) {
          throw LocalSqliteException(
              "Optional parameter ${columnsClass.simpleName}.${it.name} must have its default " + "value passed into the table definition via #default.")
        }
      }

      return CreateTable(
          columnsClass,
          withoutRowId,
          constraints,
          defaults,
          autoIncrementColumn)
    }
  }

  companion object {
    /** Alias for creating new [CreateTable.Builder]s. */
    fun <T : TableColumns<T>> of(columnsClass: KClass<T>): Builder<T> {
      return Builder(columnsClass)
    }
  }
}
