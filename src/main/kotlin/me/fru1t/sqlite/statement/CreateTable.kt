package me.fru1t.sqlite.statement

import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.constraint.ColumnConstraint
import me.fru1t.sqlite.clause.constraint.TableConstraint
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
    val columnConstraints: Map<KProperty1<T, *>, ColumnConstraint<T, *>>,
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
                    columnConstraints[it]?.let {
                      (if (it.column.returnType.isMarkedNullable) "?" else "") +
                          it.default?.let { "=<$it>" }.orEmpty() +
                          it.collation?.let { " collation=" + it.sqlName }.orEmpty() +
                          it.notNullOnConflict?.let { " notNullOnConflict=" + it.sqlName }.orEmpty()
                    }.orEmpty()
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
    private val columnConstraints: HashMap<KProperty1<T, *>, ColumnConstraint<T, *>> = HashMap()
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
     * Example usage:
     * ```
     * data class ParentTable(val a: Int) : TableColumns<ParentTable>()
     * data class Table(val a: Int, val b: Int) : TableColumns<Table>()
     * val builder = CreateTable.from(Table::class)
     *     // Foreign key constraint
     *     .constraint(Table::b references ParentTable::a onUpdate RESTRICT onDelete RESTRICT)
     *     // Unique constraint
     *     .constraint(Unique from (Table::a and Table::b) onConflict ROLLBACK)
     *     // Primary key constraint
     *     .constraint(PrimaryKey from Table::a onConflict ABORT)
     *     // Check constraint
     *     .constraint("ck_greater_than_30" checks "`${Table::a.getSqlName()}` > 30")
     * ```
     */
    fun constraint(constraint: TableConstraint<T>): Builder<T> {
      constraints.add(constraint)
      return this
    }

    /**
     * Adds a column constraint to this [CreateTable.Builder].
     *
     * Example usage:
     * ```
     * data class Table(val a: Int, val b: Int = 30) : TableColumns<Table>()
     * val builder = CreateTable.from(Table::class)
     *     .constraint(ColumnConstraint on Table::b default 30 notNullOnConflict OnConflict.ABORT)
     * ```
     */
    fun constraint(columnConstraint: ColumnConstraint<T, *>): Builder<T> {
      columnConstraints[columnConstraint.column] = columnConstraint
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
     * Creates the [CreateTable] from the current state of this [Builder].
     *
     * @throws LocalSqliteException if an optional parameter in [columnsClass] doesn't have a
     * default passed via [ColumnConstraint]
     */
    fun build(): CreateTable<T> {
      // Optional parameters must have a default defined.
      val optionalColumns = columnsClass.primaryConstructor!!.parameters.filter { it.isOptional }
      optionalColumns.forEach {
        optionalColumn -> run {
          val column = columnsClass.declaredMemberProperties.find { it.name == optionalColumn.name }
          if (columnConstraints[column]?.default == null) {
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
          columnConstraints = columnConstraints,
          autoIncrementColumn = autoIncrementColumn)
    }
  }
}
