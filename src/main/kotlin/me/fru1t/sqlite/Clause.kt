package me.fru1t.sqlite

/** Represents a clause within an SQL query which contains a single method: [getClause]. */
interface Clause {
  /** Fetches the SQL string this clause represents. */
  fun getClause(): String
}
