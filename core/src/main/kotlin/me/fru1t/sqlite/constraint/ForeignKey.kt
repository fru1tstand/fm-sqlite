package me.fru1t.sqlite.constraint

import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.Table
import me.fru1t.sqlite.constraint.resolutionstrategy.OnForeignKeyConflict
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Represents a single [`FOREIGN KEY`][ForeignKey] constraint within a [Table].
 * [`FOREIGN KEY`][ForeignKey]s are declared as a fields within the companion object of a [Table]
 * implementation. The name of the field isn't used for anything, but it'd be nice to name them
 * following standard convention:
 * `FK_<local table name>_<foreign table name>_<foreign column name>`.
 *
 * Use [ForeignKey.of] to create instances of this class.
 *
 * Example usage:
 * ```
 * data class Foo(@Column(INTEGER) val id: Int) : Table<Foo>()
 * data class Bar(@Column(INTEGER) val id: Int, @Column(INTEGER) val fooId: Int) : Table<Bar>() {
 *   companion object {
 *     // Defines a foreign key constraint between Bar's fooId to Foo's id
 *     private val FK_FOO_ID = ForeignKey.of(Bar::fooId, Foo::id)
 *   }
 * }
 * ```
 *
 *
 * See [https://sqlite.org/foreignkeys.html] for official SQLite documentation of foreign keys.
 */
data class ForeignKey<T : Table<T>, O : Table<O>>(
    val localColumn: KProperty1<T, *>,
    val foreignColumn: KProperty1<O, *>,
    val onUpdate: OnForeignKeyConflict,
    val onDelete: OnForeignKeyConflict) {
  /**
   * Returns the SQL clause that corresponds to the settings this [ForeignKey] represents to be used
   * in a `CREATE TABLE` statement.
   */
  fun getConstraintClause(): String {
    return SQL_CLAUSE.format(
        getConstraintName(),
        Table.getColumnName(localColumn),
        Table.getTableName(getForeignTable()),
        Table.getColumnName(foreignColumn),
        onUpdate.getOnUpdateClause(),
        onDelete.getOnDeleteClause())
  }

  /**
   * Returns the proper name this [ForeignKey] should have within the database. This follows the
   * format `fk_<local table name>_<other table name>_<other table's column name>`
   */
  fun getConstraintName(): String {
    return CONSTRAINT_NAME.format(
        Table.getTableName(getLocalTable()),
        Table.getTableName(getForeignTable()),
        Table.getColumnName(foreignColumn))
  }

  /**
   * Verifies that this [ForeignKey] is possible. Throws an exception on rule violation. Returns
   * `this` for method chaining.
   * */
  fun validate(): ForeignKey<T, O> {
    if (localColumn.returnType != foreignColumn.returnType) {
      throw LocalSqliteException(
          "Invalid foreign key reference.\n\n" +
              "Local column type must match foreign column type.\n" +
              "\t\tFound <${localColumn.returnType}> in <${getLocalTable().qualifiedName}" +
              "#${localColumn.name}>\n" +
              "\t\tAnd   <${foreignColumn.returnType}> in <${getForeignTable().qualifiedName}" +
              "#${foreignColumn.name}>")
    }

    val localColumnAnnotation = Table.getColumnAnnotation(localColumn)
    val foreignColumnAnnotation = Table.getColumnAnnotation(foreignColumn)

    if (localColumnAnnotation.dataType != foreignColumnAnnotation.dataType) {
      throw LocalSqliteException(
          "Invalid foreign key reference.\n\n" +
              "Local column annotation type must match foreign column annotation type.\n" +
              "\t\tFound <${localColumnAnnotation.dataType}> in <${getLocalTable().qualifiedName}" +
              "#${localColumn.name}>\n" +
              "\t\tAnd   <${foreignColumnAnnotation.dataType}> in " +
              "<${getForeignTable().qualifiedName}#${foreignColumn.name}>")
    }

    return this
  }

  /** Returns the [KClass] of the local (referent) table. */
  fun getLocalTable(): KClass<T> {
    return Table.getTableFromColumn(localColumn)
  }

  /** Returns the [KClass] of the foreign (referenced) table. */
  fun getForeignTable(): KClass<O> {
    return Table.getTableFromColumn(foreignColumn)
  }

  companion object {
    private const val CONSTRAINT_NAME = "fk_%s_%s_%s"
    private const val SQL_CLAUSE =
      "CONSTRAINT `%s` FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`) %s %s"

    /**
     * Creates a [ForeignKey] from [localColumn] to [foreignColumn] optionally specifying the
     * [onUpdate] and [onDelete] behavior.
     */
    fun <T : Table<T>, O : Table<O>> of(
        localColumn: KProperty1<T, *>,
        foreignColumn: KProperty1<O, *>,
        onUpdate: OnForeignKeyConflict = OnForeignKeyConflict.NO_ACTION,
        onDelete: OnForeignKeyConflict = OnForeignKeyConflict.NO_ACTION): ForeignKey<T, O> {
      return ForeignKey(localColumn, foreignColumn, onUpdate, onDelete).validate()
    }
  }
}
