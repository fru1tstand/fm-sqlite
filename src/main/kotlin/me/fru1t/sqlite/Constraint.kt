package me.fru1t.sqlite

/**
 * An sqlite `CONSTRAINT` clause that belongs to the [Table] [T]. [T] allows us type safety
 * when specifying [Table] fields in constraints that extend this interface.
 */
interface Constraint<T : Table<T>> {
  /** Returns the sqlite `CONSTRAINT` clause for this [Constraint]. */
  fun getConstraintSqlClause(): String
}
