package me.fru1t.sqlite.clause.constraint

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.IndexedColumnGroup
import me.fru1t.sqlite.clause.Order.DESC
import me.fru1t.sqlite.clause.and
import me.fru1t.sqlite.clause.order
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict.Companion.DEFAULT
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict.FAIL
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict.REPLACE
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict.ROLLBACK
import org.junit.jupiter.api.Test

class PrimaryKeyFileTest {
  @Test
  fun indexedColumnGroup_onConflict() {
    val result = (PrimaryKeyTestTable::a and PrimaryKeyTestTable::b) onConflict REPLACE
    assertThat(result.columnGroup).isEqualTo(PrimaryKeyTestTable::a and PrimaryKeyTestTable::b)
    assertThat(result.onConflict).isEqualTo(REPLACE)
  }

  @Test
  fun kProperty1_onConflict() {
    val result = PrimaryKeyTestTable::a onConflict FAIL
    assertThat(result.columnGroup).isEqualTo(IndexedColumnGroup(PrimaryKeyTestTable::a))
    assertThat(result.onConflict).isEqualTo(FAIL)
  }
}

class PrimaryKeyTest {
  @Test
  fun constructor_kProperty1() {
    val result = PrimaryKey(PrimaryKeyTestTable::b, DEFAULT)
    assertThat(result.columnGroup).isEqualTo(IndexedColumnGroup(PrimaryKeyTestTable::b))
    assertThat(result.onConflict).isEqualTo(DEFAULT)
  }

  @Test
  fun getClause() {
    val primaryKey =
      (PrimaryKeyTestTable::a and (PrimaryKeyTestTable::b order DESC) onConflict ROLLBACK)
    assertThat(primaryKey.getClause())
        .isEqualTo("CONSTRAINT PRIMARY KEY(`a` ASC, `b` DESC) ON CONFLICT ROLLBACK")
  }
}

private data class PrimaryKeyTestTable(val a: Int, val b: Int) : TableColumns<PrimaryKeyTestTable>()
