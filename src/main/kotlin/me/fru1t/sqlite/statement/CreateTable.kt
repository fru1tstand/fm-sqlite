package me.fru1t.sqlite.statement

import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.constraint.TableConstraint
import me.fru1t.sqlite.clause.constraint.table.Check
import me.fru1t.sqlite.clause.constraint.table.ForeignKey
import me.fru1t.sqlite.clause.constraint.table.PrimaryKey
import me.fru1t.sqlite.clause.constraint.table.Unique
import me.fru1t.sqlite.getSqlName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor

/**
 * Represents the `CREATE TABLE` statement, containing all the data required for building the Sqlite
 * query. Use [CreateTable.from] to start an instance of the builder.
 */
class CreateTable<T : TableColumns<T>> private constructor(
    val columnsClass: KClass<T>,
    val withoutRowId: Boolean,
    val constraints: List<TableConstraint<T>>,
    val defaults: Map<KProperty1<T, *>, Any>,
    val autoIncrementColumn: KProperty1<T, Int>?) {
  companion object {
    /** Alias for [Builder] that is more syntactically pleasing than CreateTable.Builder(...). */
    fun <T : TableColumns<T>> from(columnsClass: KClass<T>): Builder<T> = Builder(columnsClass)
  }

  override fun toString(): String =
      "CreateTable for ${columnsClass.qualifiedName}\n" +
          "\t\tTable name: " + columnsClass.getSqlName() + "\n" +
          "\t\tWithout RowID: " + withoutRowId.toString() + "\n" +
          "\t\tAuto Increment Column: " + (autoIncrementColumn?.getSqlName() ?: "<none>") + "\n" +
          "\t\tColumns: " + columnsClass.primaryConstructor!!.parameters.size +
          columnsClass.declaredMemberProperties.joinToString(
              separator = "\n\t\t\t\t",
              prefix = "\n\t\t\t\t",
              postfix = "\n",
              transform = {
                it.getSqlName() + " " + it.returnType +
                    (defaults[it]?.let { " Default: '$it'" } ?: "")
              }) +
          "\t\tConstraints: " + constraints.size +
          constraints.joinToString(
              separator = "\n\t\t\t\t",
              prefix = "\n\t\t\t\t",
              postfix = "\n",
              transform = { it.toString() })

  /**
   * A builder class for [CreateTable] which verifies consistency on [build]. The [columnsClass]
   * must be a kotlin data class who's primary constructor's parameters represent the columns on
   * the table. No other member properties (fields defined with `val`) may exist in the
   * [columnsClass].
   *
   * @throws LocalSqliteException if [columnsClass] is not a data class
   * @throws LocalSqliteException if [columnsClass] has other member properties
   */
  class Builder<T : TableColumns<T>>(private val columnsClass: KClass<T>) {
    init {
      // Must be a data class
      if (!columnsClass.isData) {
        throw LocalSqliteException("${columnsClass.simpleName} must be a kotlin data class.")
      }

      // Must be pure data class
      val primaryConstructor = columnsClass.primaryConstructor!!
      columnsClass.declaredMemberProperties.forEach {
        if (primaryConstructor.findParameterByName(it.name) == null) {
          throw LocalSqliteException(
              "${columnsClass.simpleName} may not have declared member properties (ie. val) in " +
                  "its class definition.\n\t\tFound: ${it.name}")
        }
      }
    }

    private val constraints: MutableList<TableConstraint<T>> = ArrayList()
    private val defaults: MutableMap<KProperty1<T, *>, Any> = HashMap()
    private var withoutRowId: Boolean = false
    private var autoIncrementColumn: KProperty1<T, Int>? = null

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

    /**
     * Add a table constraint to this [CreateTable.Builder].
     *
     * Code snippet for examples:
     * ```
     * data class Table(val a: Int, val b: Int) : TableColumns<Table>()
     * val builder = CreateTable.from(Table::class)
     * ```
     *
     * Example [ForeignKey] constraint:
     * ```
     * data class ParentTable(val a: Int) : TableColumns<ParentTable>()
     * builder.constraint(Table::b references ParentTable::a onUpdate RESTRICT onDelete RESTRICT)
     * ```
     *
     * Example [Unique] constraint:
     * ```
     * builder.constraint(Unique from (Table::a and Table::b) onConflict ROLLBACK)
     * ```
     *
     * Example [PrimaryKey] constraint:
     * ```
     * builder.constraint(PrimaryKey from Table::a onConflict ABORT)
     * ```
     *
     * Example [Check] constraint:
     * ```
     * builder.constraint("ck_greater_than_30" checks "`${Table::a.getSqlName()}` > 30")
     * ```
     */
    fun constraint(constraint: TableConstraint<T>): Builder<T> {
      constraints.add(constraint)
      return this
    }

    /**
     * Sets a [column] to use `AUTOINCREMENT`. Please read
     * [the official documentation][http://www.sqlite.org/autoinc.html] before opting to use this.
     * In most cases you don't need to, and instead, you can rely on the `ROWID` column. Only
     * a single auto increment column may exist per table.
     */
    fun autoIncrement(column: KProperty1<T, Int>): Builder<T> {
      autoIncrementColumn = column
      return this
    }

    /**
     * Add a [default] value to a [column]. More technically, this adds the `DEFAULT '<value>'`
     * clause to the column definition on the `CREATE TABLE` statement. If no default is given here,
     * the `DEFAULT` clause is omitted.
     *
     * Columns passed into this method **must** be optional, and conversely, every optional
     * parameter in [columnsClass] **must** have a default defined using this method. This is
     * required as it's impossible to introspectively resolve the default value for optional
     * parameters in kotlin.
     *
     * Example usage:
     * ```
     * private const val EXAMPLE = 0
     * data class Table(val a: Int, val b: Int = EXAMPLE) : TableColumns<Table>()
     * val createTable =
     *   CreateTable.from(Table::class)
     *       .default(Table::b, EXAMPLE)
     *       .build()
     * ```
     *
     * @throws LocalSqliteException if [column] isn't an optional parameter
     */
    fun <E : Any> default(column: KProperty1<T, E?>, default: E): Builder<T> {
      // Must be optional
      if (!columnsClass.primaryConstructor!!.findParameterByName(column.name)!!.isOptional) {
        throw LocalSqliteException(
            "${columnsClass.simpleName}.${column.name} must be an optional parameter to allow " +
                "default values.")
      }

      defaults[column] = default
      return this
    }

    /**
     * Creates the [CreateTable] from the current state of this [Builder].
     *
     * @throws LocalSqliteException if an optional parameter in [columnsClass] didn't have a
     * [default] declared
     */
    fun build(): CreateTable<T> {
      // Optional parameters must have a default defined.
      val optionalColumns = columnsClass.primaryConstructor!!.parameters.filter { it.isOptional }
      optionalColumns.forEach {
        optionalColumn -> run {
          if (!defaults.containsKey(
                  columnsClass.declaredMemberProperties.find { it.name == optionalColumn.name })) {
            throw LocalSqliteException(
                "Optional parameter ${columnsClass.simpleName}.${optionalColumn.name} must have " +
                    "its default value passed into the table definition via #default.")
          }
        }
      }

      return CreateTable(
          columnsClass = columnsClass,
          withoutRowId = withoutRowId,
          constraints = constraints,
          defaults = defaults,
          autoIncrementColumn = autoIncrementColumn)
    }
  }
}
