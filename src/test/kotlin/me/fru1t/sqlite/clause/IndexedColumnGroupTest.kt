package me.fru1t.sqlite.clause

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.clause.Order.DESC
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.Order.ASC
import me.fru1t.sqlite.clause.Order.Companion.DEFAULT
import org.junit.jupiter.api.Test
import kotlin.reflect.KProperty1

class IndexedColumnTest {
  @Test
  fun indexedColumn() {
    val result = IndexedColumn(IndexedColumnGroupTestTable::a, ASC)
    assertThat(result.column).isEqualTo(IndexedColumnGroupTestTable::a)
    assertThat(result.order).isEqualTo(ASC)
  }

  @Test
  fun overrideToString() {
    val result = IndexedColumn(IndexedColumnGroupTestTable::a, DESC).toString()
    assertThat(result).isEqualTo("`a` DESC")
  }
}

class IndexedColumnGroupTest {
  @Test
  fun constructor() {
    val result = FakeIndexedColumnGroup(IndexedColumnGroupTestTable::a)
    assertThat(result.columns())
        .containsExactly(IndexedColumn(IndexedColumnGroupTestTable::a, DEFAULT))
  }

  @Test
  fun getClause() {
    val indexedColumnGroup = (
        FakeIndexedColumnGroup(IndexedColumnGroupTestTable::a) order ASC
            and IndexedColumnGroupTestTable::b order DESC)
    assertThat(indexedColumnGroup.getClause()).isEqualTo("(`a` ASC, `b` DESC)")
  }

  @Test
  fun and() {
    val result =
      FakeIndexedColumnGroup(IndexedColumnGroupTestTable::a) and IndexedColumnGroupTestTable::b
    assertThat(result.columns())
        .containsExactly(
            IndexedColumn(IndexedColumnGroupTestTable::a, DEFAULT),
            IndexedColumn(IndexedColumnGroupTestTable::b, DEFAULT))
  }

  @Test
  fun order() {
    val result = FakeIndexedColumnGroup(IndexedColumnGroupTestTable::a) order DESC
    assertThat(result.columns())
        .containsExactly(IndexedColumn(IndexedColumnGroupTestTable::a, DESC))
  }
}

private data class IndexedColumnGroupTestTable(val a: Int, val b: String) : TableColumns()

private class FakeIndexedColumnGroup(
    initialColumn: KProperty1<IndexedColumnGroupTestTable, *>) :
    IndexedColumnGroup<IndexedColumnGroupTestTable, FakeIndexedColumnGroup>(initialColumn)
