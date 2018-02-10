package me.fru1t.sqlite.clause

import me.fru1t.sqlite.Clause
import me.fru1t.sqlite.TableColumns

/**
 * An sqlite `CONSTRAINT` clause that belongs to the [Table] [T]. [T] allows us type safety
 * when specifying [Table] fields in constraints that extend this interface.
 */
interface Constraint<T : TableColumns<T>> : Clause {
  /** Returns the sqlite `CONSTRAINT` clause for this [Constraint]. */
  override fun getClause(): String
}
