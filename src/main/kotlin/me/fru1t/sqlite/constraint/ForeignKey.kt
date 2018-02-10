package me.fru1t.sqlite.constraint

import me.fru1t.sqlite.Constraint
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.constraint.resolutionstrategy.OnForeignKeyConflict
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Creates a [ForeignKey] reference from the calling column to the [referenceColumn]. This method
 * defaults the [ForeignKey.onUpdate] and [ForeignKey.onDelete] [OnForeignKeyConflict] values to the
 * Sqlite default of [OnForeignKeyConflict.NO_ACTION]. See also [ForeignKey.onUpdate] and
 * [ForeignKey.onDelete] to specify on foreign key conflict values.
 *
 * See [ForeignKey] for example usage.
 */
infix fun <L : TableColumns<L>, F : TableColumns<F>, T : Any> KProperty1<L, T>.references(
    referenceColumn: KProperty1<F, T>): ForeignKey<L, F, T> {
  return ForeignKey(
      this,
      referenceColumn,
      OnForeignKeyConflict.NO_ACTION,
      OnForeignKeyConflict.NO_ACTION)
}

/**
 * Represents a single [`FOREIGN KEY`][ForeignKey] constraint within a [Table]. Both child and
 * parent fields must be the same type [T]. Instances of this class are traditionally created with
 * [references]. For clarity, the [childColumn] is the local column, or the table that holds the
 * constraint. The [parentColumn] is the foreign column, or the table that is referenced.
 *
 * Example usage:
 * ```
 * // Define the tables that will be used
 * data class ExampleUser(val id: Int) : TableColumns<ExampleUser>()
 * data class ExamplePost(val id: Int, val exampleUserId: Int) : TableColumns<ExamplePost>()
 *
 * // Example code block
 * fun main(args: Array<String>) {
 *   // One can use the inline variant of this method as such:
 *   val foreignKey =
 *     ExamplePost::exampleUserId references ExampleUser::id onUpdate RESTRICT onDelete RESTRICT
 *
 *   // Or more traditionally
 *   val foreignKey =
 *     ExamplePost::exampleUserId.references(ExampleUser::id).onUpdate(RESTRICT).onDelete(RESTRICT)
 * }
 * ```
 *
 * See [https://sqlite.org/foreignkeys.html] for official SQLite documentation of foreign keys.
 */
data class ForeignKey<L : TableColumns<L>, F : TableColumns<F>, out T : Any>(
    val childColumn: KProperty1<L, T>,
    val parentColumn: KProperty1<F, T>,
    val onUpdate: OnForeignKeyConflict,
    val onDelete: OnForeignKeyConflict) : Constraint<L> {

  /**  An infix alias for this data class's [copy] method specifying an [onUpdate]. */
  infix fun onUpdate(onUpdate: OnForeignKeyConflict) = copy(onUpdate = onUpdate)

  /** An infix alis for this data class's [copy] method specifying an [onDelete]. */
  infix fun onDelete(onDelete: OnForeignKeyConflict) = copy(onDelete = onDelete)

  override fun getConstraintSqlClause(): String {
    return SQL_CLAUSE.format(
        getConstraintName(),
        TableColumns.getColumnName(childColumn),
        TableColumns.getTableName(getForeignTable()),
        TableColumns.getColumnName(parentColumn),
        onUpdate.getOnUpdateClause(),
        onDelete.getOnDeleteClause())
  }

  /**
   * Returns the proper name this [ForeignKey] should have within the database. This follows the
   * format `fk_<local table name>_<other table name>_<other table's column name>`
   */
  fun getConstraintName(): String {
    return CONSTRAINT_NAME.format(
        TableColumns.getTableName(getLocalTable()),
        TableColumns.getTableName(getForeignTable()),
        TableColumns.getColumnName(parentColumn))
  }

  /** Returns the [KClass] of the local (referent) table. */
  fun getLocalTable(): KClass<L> {
    return TableColumns.getTableFromColumn(childColumn)
  }

  /** Returns the [KClass] of the foreign (referenced) table. */
  fun getForeignTable(): KClass<F> {
    return TableColumns.getTableFromColumn(parentColumn)
  }

  companion object {
    private const val CONSTRAINT_NAME = "fk_%s_%s_%s"
    private const val SQL_CLAUSE =
      "CONSTRAINT `%s` FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`) %s %s"
  }
}
