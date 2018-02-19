package me.fru1t.sqlite.clause

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.clause.Order.DESC
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.Order.ASC
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FileTest {
  @Test
  fun kProperty1_order() {
    val result = IndexedColumnGroupTestTable::a order DESC
    assertThat(result.columns).hasSize(1)
    assertThat(result.columns[0].column).isEqualTo(IndexedColumnGroupTestTable::a)
    assertThat(result.columns[0].order).isEqualTo(DESC)
  }

  @Test
  fun kProperty1_and_kProperty1() {
    val result = IndexedColumnGroupTestTable::a and IndexedColumnGroupTestTable::b
    assertThat(result.columns)
        .containsExactly(
            IndexedColumn(IndexedColumnGroupTestTable::a, Order.DEFAULT),
            IndexedColumn(IndexedColumnGroupTestTable::b, Order.DEFAULT))
  }

  @Test
  fun kProperty1_and_indexedColumn() {
    val result = IndexedColumnGroupTestTable::a and (IndexedColumnGroupTestTable::b order DESC)
    assertThat(result.columns)
        .containsExactly(
            IndexedColumn(IndexedColumnGroupTestTable::a, Order.DEFAULT),
            IndexedColumn(IndexedColumnGroupTestTable::b, DESC))
  }
}

class IndexedColumnTest {
  @Test
  fun overrideToString() {
    val result = IndexedColumn(IndexedColumnGroupTestTable::a, DESC).toString()
    assertThat(result).isEqualTo("`a` DESC")
  }
}

class IndexedColumnGroupTest {
  private lateinit var indexedColumnGroup: IndexedColumnGroup<IndexedColumnGroupTestTable>

  @BeforeEach
  fun setUp() {
    indexedColumnGroup =
        IndexedColumnGroup(
            listOf(IndexedColumn(IndexedColumnGroupTestTable::a, Order.DEFAULT), IndexedColumn(
                IndexedColumnGroupTestTable::b, ASC)))
  }

  @Test
  fun constructor_kProperty1() {
    val result = IndexedColumnGroup(IndexedColumnGroupTestTable::a)
    assertThat(result.columns)
        .containsExactly(IndexedColumn(IndexedColumnGroupTestTable::a, Order.DEFAULT))
  }

  @Test
  fun init_noColumns() {
    try {
      IndexedColumnGroup<IndexedColumnGroupTestTable>(emptyList())
      fail<Unit>("Expected a LocalSqliteException about passing zero columns")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains("cannot have zero columns")
    }
  }

  @Test
  fun getClause() {
    assertThat(indexedColumnGroup.getClause()).isEqualTo("(`a` ASC, `b` ASC)")
  }

  @Test
  fun getClauseWithoutGrouping() {
    assertThat(indexedColumnGroup.getClauseWithoutGrouping()).isEqualTo("`a` ASC, `b` ASC")
  }

  @Test
  fun and_indexedColumnGroup() {
    val result = indexedColumnGroup and (IndexedColumnGroupTestTable::c order DESC)
    assertThat(result.columns)
        .containsExactly(
            IndexedColumn(IndexedColumnGroupTestTable::a, Order.DEFAULT),
            IndexedColumn(IndexedColumnGroupTestTable::b, ASC),
            IndexedColumn(IndexedColumnGroupTestTable::c, DESC))
  }

  @Test
  fun and_kProperty1() {
    val result = indexedColumnGroup and IndexedColumnGroupTestTable::c
    assertThat(result.columns)
        .containsExactly(
            IndexedColumn(IndexedColumnGroupTestTable::a, Order.DEFAULT),
            IndexedColumn(IndexedColumnGroupTestTable::b, ASC),
            IndexedColumn(IndexedColumnGroupTestTable::c, Order.DEFAULT))
  }
}

private data class IndexedColumnGroupTestTable(
    val a: Int, val b: String, val c: String) : TableColumns<IndexedColumnGroupTestTable>()
