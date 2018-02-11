package me.fru1t.sqlite

import me.fru1t.sqlite.clause.Constraint
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor

/**
 * Represents an Sqlite table. This class is meant to hold the definition of a table in a database.
 *
 * Use [TableDefinition.of] to create instances of the builder class.
 */
class TableDefinition<T : TableColumns<T>> private constructor(
    val columnsClass: KClass<T>,
    val withoutRowId: Boolean,
    val constraints: List<Constraint<T>>,
    val defaults: Map<KParameter, Any>,
    val autoIncrementColumn: KParameter?) {

  /**
   * A builder class for [TableDefinition] which verifies consistency on [build].
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

    fun withoutRowId(withoutRowId: Boolean): Builder<T> {
      this.withoutRowId = withoutRowId
      return this
    }

    /** Add a single constraint to this [TableDefinition.Builder]. */
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
        throw LocalSqliteException("${columnsClass.simpleName} already has AUTOINCREMENT column " +
            "'${autoIncrementColumn!!.name}', cannot add another '${column.name}'")
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
     *     val DEFINITION = TableDefinition.of(ExampleTable::class)
     *         .default(ExampleTable::value, VALUE)
     *         .build()
     *   }
     * }
     * ```
     */
    fun <E : Any> default(column: KProperty1<T, E?>, default: E): Builder<T> {
      val columnAsKParameter = columnsClass.primaryConstructor!!.findParameterByName(column.name)!!

      // Must be optional
      if (!columnAsKParameter.isOptional) {
        throw LocalSqliteException(
            "${columnsClass.simpleName}.${column.name} must be an optional parameter to allow " +
                "default values.")
      }

      // Must not exist already
      defaults.putIfAbsent(columnAsKParameter, default)?.let {
        throw LocalSqliteException(
            "A default is already defined for ${columnsClass.simpleName}.${column.name}")
      }

      return this
    }

    /** Creates the [TableDefinition] from the current state of this [Builder]. */
    fun build(): TableDefinition<T> {
      // Optional parameters must have a default defined.
      val optionalColumns = columnsClass.primaryConstructor!!.parameters.filter { it.isOptional }
      optionalColumns.forEach {
        if (!defaults.containsKey(it)) {
          throw LocalSqliteException(
              "Optional parameter ${columnsClass.simpleName}.${it.name} must have its default " +
                  "value passed into the table definition via #default.")
        }
      }

      return TableDefinition(columnsClass, withoutRowId, constraints, defaults, autoIncrementColumn)
    }
  }

  companion object {
    /** Alias for creating new [TableDefinition.Builder]s. */
    fun <T : TableColumns<T>> of(columnsClass: KClass<T>): Builder<T> {
      return Builder(columnsClass)
    }
  }
}
