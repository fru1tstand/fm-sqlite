package me.fru1t.sqlite.clause.constraint

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.TableColumns
import org.junit.jupiter.api.Test

private const val VALID_NAME = "ck_example"
private const val VALID_CLAUSE = "0 = 0"

class FileTest {
  @Test
  fun string_checks() {
    val result: Check<Table> = VALID_NAME checks VALID_CLAUSE
    assertThat(result.name).isEqualTo(VALID_NAME)
    assertThat(result.sqlLogicClause).isEqualTo(VALID_CLAUSE)
  }
}

class CheckTest {
  @Test
  fun init_invalidName() {
    try {
      Check<Table>(VALID_CLAUSE, "#invalid name#")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains("Invalid check constraint name")
      assertThat(e).hasMessageThat().contains("#invalid name#")
    }
  }

  @Test
  fun init_blankLogicClause() {
    try {
      Check<Table>("\t    ", VALID_NAME)
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains("Cannot have blank check constraints")
    }
  }

  @Test
  fun getClause_noName() {
    val result = Check<Table>(VALID_CLAUSE)
    assertThat(result.getClause()).isEqualTo("CONSTRAINT CHECK ($VALID_CLAUSE)")
  }

  @Test
  fun getClause_named() {
    val result: Check<Table> = VALID_NAME checks VALID_CLAUSE
    assertThat(result.getClause()).isEqualTo("CONSTRAINT `$VALID_NAME` CHECK ($VALID_CLAUSE)")
  }

  @Test
  fun getConstraintName_validName() {
    val result: Check<Table> = VALID_NAME checks VALID_CLAUSE
    assertThat(result.getConstraintName()).isEqualTo(VALID_NAME)
  }

  @Test
  fun getConstraintName_null() {
    val result = Check<Table>(VALID_CLAUSE)
    assertThat(result.getConstraintName()).isNull()
  }
}

private data class Table(val foo: String, val bar: String) : TableColumns<Table>()
