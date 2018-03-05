package me.fru1t.sqlite.clause

import me.fru1t.sqlite.Clause

/** Defines how strings are compared and is used in the `COLLATE` clause. */
enum class Collation(val sqlName: String) : Clause {
  /** Compares strings using `memcmp()`, regardless of text encoding. This is default. */
  BINARY("BINARY"),

  /** Compares strings using [Collation.BINARY], ignoring trailing whitespace. */
  RTRIM("RTRIM"),

  /** Compares strings using [Collation.BINARY], ignoring case. */
  NOCASE("NOCASE");

  companion object {
    private const val CLAUSE = "COLLATE %s"

    /** Alias for [Collation.BINARY]. */
    val DEFAULT: Collation = Collation.BINARY
  }

  /** Returns the [Collation] name. For example: `COLLATE BINARY`. */
  override fun getClause(): String = CLAUSE.format(sqlName)
}
