package me.fru1t.sqlite.clause.constraint

import me.fru1t.sqlite.clause.Constraint
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.resolutionstrategy.OnForeignKeyConflict
import me.fru1t.sqlite.getSqlName
import me.fru1t.sqlite.getTable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Creates a [ForeignKey] reference from the calling column to the [referenceColumn]. This method
 * defaults the [ForeignKey.onUpdate] and [ForeignKey.onDelete] [OnForeignKeyConflict] values to
 * [OnForeignKeyConflict.DEFAULT]. See also [ForeignKey.onUpdate] and [ForeignKey.onDelete] to
 * specify on foreign key conflict values.
 *
 * Example usage: `ChildTable::b references ParentTable::a onUpdate NO_ACTION onDelete CASCADE`.
 */
infix fun <L : TableColumns<L>, F : TableColumns<F>, T : Any> KProperty1<L, T>.references(
    referenceColumn: KProperty1<F, T>): ForeignKey<L, F, T> {
  return ForeignKey(
      this,
      referenceColumn,
      OnForeignKeyConflict.DEFAULT,
      OnForeignKeyConflict.DEFAULT)
}

/**
 * Represents a single column present on two tables constrained to match in value. The [childColumn]
 * is dependent on the [parentColumn] and if an attempt is made to update or delete the
 * [parentColumn], [onUpdate] or [onDelete] resolution strategy will take effect, respectively.
 *
 * See [https://www.sqlite.org/foreignkeys.html] for official documentation.
 */
data class ForeignKey<L : TableColumns<L>, F : TableColumns<F>, out T : Any>(
    val childColumn: KProperty1<L, T>,
    val parentColumn: KProperty1<F, T>,
    val onUpdate: OnForeignKeyConflict,
    val onDelete: OnForeignKeyConflict) : Constraint<L> {
  companion object {
    private const val CONSTRAINT_NAME = "fk_%s_%s_%s"
    private const val SQL_CLAUSE =
      "CONSTRAINT `%s` FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`) %s %s"
  }

  /**  An infix alias for this data class's [copy] method specifying an [onUpdate]. */
  infix fun onUpdate(onUpdate: OnForeignKeyConflict) = copy(onUpdate = onUpdate)

  /** An infix alis for this data class's [copy] method specifying an [onDelete]. */
  infix fun onDelete(onDelete: OnForeignKeyConflict) = copy(onDelete = onDelete)

  /** Returns the [KClass] of the child (local) table. */
  fun getChildTable(): KClass<L> = childColumn.getTable()

  /** Returns the [KClass] of the parent (referenced) table. */
  fun getParentTable(): KClass<F> = parentColumn.getTable()

  /**
   * Example: ``CONSTRAINT `fk_table_parent_table_a` FOREIGN KEY (`b`) REFERENCES
   * `parent_table`(`a`) ON UPDATE CASCADE ON DELETE RESTRICT``.
   */
  override fun getClause(): String {
    return SQL_CLAUSE.format(
        getConstraintName(),
        childColumn.getSqlName(),
        parentColumn.getTable().getSqlName(),
        parentColumn.getSqlName(),
        onUpdate.getOnUpdateClause(),
        onDelete.getOnDeleteClause())
  }

  /**
   * Returns the proper name this [ForeignKey] should have within the database. This follows the
   * format `fk_<local table name>_<other table name>_<other table's column name>`.
   * Example: `fk_child_table_parent_table_id`.
   */
  override fun getConstraintName(): String {
    return CONSTRAINT_NAME.format(
        childColumn.getTable().getSqlName(),
        parentColumn.getTable().getSqlName(),
        parentColumn.getSqlName())
  }
}
