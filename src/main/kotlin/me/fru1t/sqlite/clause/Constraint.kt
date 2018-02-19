package me.fru1t.sqlite.clause

import me.fru1t.sqlite.Clause
import me.fru1t.sqlite.TableColumns

/**
 * An sqlite `CONSTRAINT` clause that belongs to the [TableColumns] [T]. [T] allows us type safety
 * when specifying [TableColumns] fields in constraints that extend this interface.
 */
interface Constraint<T : TableColumns<T>> : Clause {
  /**
   * Returns the entire Sqlite `CONSTRAINT` clause for this [Constraint] including the `CONSTRAINT`
   * keyword. For example: ``CONSTRAINT `fk_post_user_id` FOREIGN KEY (`user_id`) REFERENCES
   * user(id)``.
   */
  override fun getClause(): String

  /**
   * Returns the Sqlite name for the constraint if it has one, otherwise `null`. For example:
   * `fk_post_user_id`.
   */
  fun getConstraintName(): String?
}
