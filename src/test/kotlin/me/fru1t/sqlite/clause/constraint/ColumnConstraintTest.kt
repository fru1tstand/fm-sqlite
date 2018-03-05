package me.fru1t.sqlite.clause.constraint

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.Collation
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class ColumnConstraintTest {
  @Test
  fun on() {
    val result = ColumnConstraint on ColumnConstraintTestTable::a
    assertThat(result.column).isEqualTo(ColumnConstraintTestTable::a)
    assertThat(result.default).isNull()
    assertThat(result.collation).isNull()
    assertThat(result.notNullOnConflict).isNull()
  }

  @Test
  fun init_defaultOnNonOptionalColumn() {
    try {
      ColumnConstraint(ColumnConstraintTestTable::a, 30, null, null)
      fail<Unit>(
          "Expected LocalSqliteException for defining default value for non-optional parameter")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(ColumnConstraintTestTable::class.simpleName!!)
      assertThat(e).hasMessageThat().contains(ColumnConstraintTestTable::a.name)
      assertThat(e).hasMessageThat().contains("must be an optional parameter")
    }
  }

  @Test
  fun init_notNullOnConflictOnNullableColumn() {
    try {
      ColumnConstraint(ColumnConstraintTestTable::c, null, null, OnConflict.REPLACE)
      fail<Unit>(
          "Expected LocalSqliteException for defining not null on conflict on a nullable column")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(ColumnConstraintTestTable::class.simpleName!!)
      assertThat(e).hasMessageThat().contains(ColumnConstraintTestTable::a.name)
      assertThat(e).hasMessageThat().contains("must be marked non-null")
    }
  }

  @Test
  fun default() {
    val result = ColumnConstraint on ColumnConstraintTestTable::b default 30
    assertThat(result.default).isEqualTo(30)
  }

  @Test
  fun collate() {
    val result = ColumnConstraint on ColumnConstraintTestTable::a collate Collation.NOCASE
    assertThat(result.collation).isEqualTo(Collation.NOCASE)
  }

  @Test
  fun notNullOnConflict() {
    val result =
      ColumnConstraint on ColumnConstraintTestTable::a notNullOnConflict OnConflict.ROLLBACK
    assertThat(result.notNullOnConflict).isEqualTo(OnConflict.ROLLBACK)
  }

  @Test
  fun getClause() {
    val result =
      (ColumnConstraint on ColumnConstraintTestTable::b
          default 30
          collate Collation.RTRIM
          notNullOnConflict OnConflict.ABORT)
          .getClause()
    assertThat(result).startsWith("`b` NOT NULL ON CONFLICT ABORT")
    assertThat(result).contains("DEFAULT `30`")
    assertThat(result).contains("COLLATE RTRIM")
  }

  @Test
  fun getClause_backTickDefault() {
    val result = (ColumnConstraint on ColumnConstraintTestTable::d default "`").getClause()
    assertThat(result).contains("DEFAULT `\\``")
  }

  @Test
  fun getClause_nullable() {
    val result = (ColumnConstraint on ColumnConstraintTestTable::c).getClause()
    assertThat(result).startsWith("`c` NULL")
  }
}

private data class ColumnConstraintTestTable(
    val a: Int, val b: Int = 30, val c: Int?, val d: String = "`") : TableColumns()
