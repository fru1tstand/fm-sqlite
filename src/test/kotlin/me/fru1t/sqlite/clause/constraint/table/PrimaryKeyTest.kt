package me.fru1t.sqlite.clause.constraint.table

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.IndexedColumnGroup
import me.fru1t.sqlite.clause.Order.DESC
import me.fru1t.sqlite.clause.and
import me.fru1t.sqlite.clause.order
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict.REPLACE
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict.ROLLBACK
import org.junit.jupiter.api.Test

class PrimaryKeyTest {
  @Test
  fun from_indexedColumnGroup() {
    val result = PrimaryKey from (PrimaryKeyTestTable::a and PrimaryKeyTestTable::b)
    assertThat(result.columnGroup).isEqualTo(PrimaryKeyTestTable::a and PrimaryKeyTestTable::b)
    assertThat(result.onConflict).isEqualTo(OnConflict.DEFAULT)
  }

  @Test
  fun from_kProperty1() {
    val result = PrimaryKey from PrimaryKeyTestTable::a
    assertThat(result.columnGroup).isEqualTo(IndexedColumnGroup(PrimaryKeyTestTable::a))
    assertThat(result.onConflict).isEqualTo(OnConflict.DEFAULT)
  }

  @Test
  fun onConflict() {
    val result =
      PrimaryKey(
          IndexedColumnGroup(PrimaryKeyTestTable::a),
          OnConflict.DEFAULT) onConflict REPLACE
    assertThat(result.onConflict).isEqualTo(REPLACE)
  }

  @Test
  fun getClause() {
    val primaryKey =
      PrimaryKey(
          PrimaryKeyTestTable::a and (PrimaryKeyTestTable::b order DESC),
          ROLLBACK)
    assertThat(primaryKey.getClause())
        .isEqualTo("CONSTRAINT PRIMARY KEY(`a` ASC, `b` DESC) ON CONFLICT ROLLBACK")
  }

  @Test
  fun getConstraintName() {
    val primaryKey =
      PrimaryKey(
          PrimaryKeyTestTable::a and PrimaryKeyTestTable::b,
          ROLLBACK)
    assertThat(primaryKey.getConstraintName()).isNull()
  }

  @Test
  fun overrideToString() {
    val result =
      PrimaryKey(
          PrimaryKeyTestTable::a and (PrimaryKeyTestTable::b order DESC),
          ROLLBACK)
          .toString()
    assertThat(result).contains("Primary Key")
    assertThat(result).contains("onConflict")
  }
}

private data class PrimaryKeyTestTable(val a: Int, val b: Int) : TableColumns<PrimaryKeyTestTable>()
