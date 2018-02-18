package me.fru1t.sqlite

/** Useful constants relating to Sqlite databases. */
object Sqlite {
  val VALID_SQL_NAME_REGEX = Regex("^[a-z_][a-z0-9_]*\$")
}
