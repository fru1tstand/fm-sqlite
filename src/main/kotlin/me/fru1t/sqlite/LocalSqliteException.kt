package me.fru1t.sqlite

/** An exception that occurs locally, that is, before it's sent to the Sqlite database. */
class LocalSqliteException(message: String) : Exception(message)
