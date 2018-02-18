package me.fru1t.sqlite.clause

import me.fru1t.sqlite.Clause

/** Direction of sorting for `ORDER BY`, `UNIQUE` and `PRIMARY KEY` clauses. */
enum class Order(private val value: String): Clause {
  /**
   * Ascending order: starting from the lowest value going to the highest.
   *
   * Lexicographically: (0 -> 9) -> (A -> Z)
   * Numerically: -Infinity -> Infinity
   */
  ASC("ASC"),

  /**
   * Descending order: starting from the highest value going to the lowest.
   *
   * Alphabetically: (Z -> A) -> (9 -> 0)
   * Numerically: Infinity -> -Infinity
   */
  DESC("DESC");

  companion object {
    /** Alias for [Order.ASC]. */
    val DEFAULT = ASC
  }

  /** Example: `DESC`. */
  override fun getClause(): String = value
}
