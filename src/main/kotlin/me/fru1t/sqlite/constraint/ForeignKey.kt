package me.fru1t.sqlite.constraint

import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.Table
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Represents a single [ForeignKey] constraint within a database [Table]. The name of the field this
 * [ForeignKey] is stored in is ignored as the name of the constraint is generated from the
 * tables and columns referenced. See [getConstraintName] for more detail about name generation. See
 * [https://sqlite.org/foreignkeys.html] for official SQLite documentation of foreign keys.
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
 */
data class ForeignKey<T : Table<T>, O : Table<O>>(
    val localColumn: KProperty1<T, *>,
    val foreignColumn: KProperty1<O, *>,
    val onUpdate: OnModificationAction,
    val onDelete: OnModificationAction) {
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
        onUpdate.sqlClause,
        onDelete.sqlClause)
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
      "CONSTRAINT `%s` FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`) ON UPDATE %s ON DELETE %s"

    /**
     * Creates a [ForeignKey] from [localColumn] to [foreignColumn] optionally specifying the
     * [onUpdate] and [onDelete] behavior.
     */
    fun <T : Table<T>, O : Table<O>> of(
        localColumn: KProperty1<T, *>,
        foreignColumn: KProperty1<O, *>,
        onUpdate: OnModificationAction = OnModificationAction.NO_ACTION,
        onDelete: OnModificationAction = OnModificationAction.NO_ACTION): ForeignKey<T, O> {
      return ForeignKey(localColumn, foreignColumn, onUpdate, onDelete).validate()
    }
  }
}

/**
 * Specifies to the SQLite engine what should be done when a [ForeignKey] is `UPDATE`ed or
 * `DELETE`ed when that [ForeignKey] value exists on the independent (upstream) table.
 * Documentation stems directly from the `sqlite.org` website found at
 * [https://sqlite.org/foreignkeys.html#fk_actions]
 */
enum class OnModificationAction(val sqlClause: String) {
  /**
   * The [RESTRICT] action means that the application is prohibited from deleting (for `ON DELETE
   * RESTRICT`) or modifying (for `ON UPDATE RESTRICT`) a parent key when there exists one or more
   * child keys mapped to it. The difference between the effect of a [RESTRICT] action and normal
   * foreign key constraint enforcement is that the [RESTRICT] action processing happens as soon as
   * the field is updated - not at the end of the current statement as it would with an immediate
   * constraint, or at the end of the current transaction as it would with a deferred constraint.
   * Even if the foreign key constraint it is attached to is deferred, configuring a [RESTRICT]
   * action causes SQLite to return an error immediately if a parent key with dependent child keys
   * is deleted or modified.
   */
  RESTRICT("RESTRICT"),

  /**
   * Configuring [NO_ACTION] means just that: when a parent key is modified or deleted from the
   * database, no special action is taken.
   */
  NO_ACTION("NO ACTION"),

  /**
   * A [CASCADE] action propagates the delete or update operation on the parent key to each
   * dependent child key. For an `ON DELETE CASCADE` action, this means that each row in the child
   * table that was associated with the deleted parent row is also deleted. For an `ON UPDATE
   * CASCADE` action, it means that the values stored in each dependent child key are modified to
   * match the new parent key values.
   */
  CASCADE("CASCADE"),

  /**
   * If the configured action is [SET_NULL], then when a parent key is deleted (for `ON DELETE SET
   * NULL`) or modified (for `ON UPDATE SET NULL`), the child key columns of all rows in the child
   * table that mapped to the parent key are set to contain SQL `NULL` values.
   */
  SET_NULL("SET NULL"),

  /**
   * The [SET_DEFAULT] actions are similar to [SET_NULL], except that each of the child key columns
   * is set to contain the columns default value instead of `NULL`. Refer to the
   * [`CREATE TABLE`][https://www.sqlite.org/lang_createtable.html] documentation for details on how
   * default values are assigned to table columns.
   */
  SET_DEFAULT("SET DEFAULT")
}
