package me.fru1t.sqlite

import me.fru1t.sqlite.clause.Constraint
import kotlin.reflect.KClass

/**
 * Represents an Sqlite table. This class is meant to hold the definition of a table in a database.
 * [table] must be a data class with fields that represent the columns. [withoutRowId] specifies
 * whether or not the `WITHOUT ROWID` clause is added to the table definition clause. [constraints]
 * define the constraints on the table.
 *
 * Use [TableDefinition.of] to create instances of the builder class.
 */
data class TableDefinition<T : TableColumns<T>>(
    val table: KClass<T>,
    val withoutRowId: Boolean = false,
    val constraints: List<Constraint<T>> = emptyList()) {

  class Builder<T : TableColumns<T>>(val table: KClass<T>) {
    var withoutRowId: Boolean = false
    var constraints: MutableList<Constraint<T>> = ArrayList()

    fun withoutRowId(withoutRowId: Boolean): Builder<T> {
      this.withoutRowId = withoutRowId
      return this
    }

    /** Add a single constraint to this [TableDefinition.Builder]. */
    fun constraint(constraint: Constraint<T>): Builder<T> {
      constraints.add(constraint)
      return this
    }

    /** Creates the [TableDefinition] from the current state of this [Builder]. */
    fun build(): TableDefinition<T> {
      return TableDefinition(table, withoutRowId, constraints)
    }
  }

  companion object {
    /** Alias for creating new [TableDefinition.Builder]s. */
    fun <T : TableColumns<T>> of(table: KClass<T>): Builder<T> = Builder(table)
  }
}
