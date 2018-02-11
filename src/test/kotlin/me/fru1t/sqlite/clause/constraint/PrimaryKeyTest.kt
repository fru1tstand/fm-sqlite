package me.fru1t.sqlite.clause.constraint

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.TableColumns
import org.junit.jupiter.api.Test

class PrimaryKeyTest {
  @Test
  fun getConstraintClause() {
    val primaryKey = PrimaryKey(arrayOf(TestTable::param1, TestTable::param2))
    assertThat(primaryKey.getConstraintClause())
        .isEqualTo("PRIMARY KEY(`param1`,`param2`)")
  }

  @Test
  fun getConstraintClause_empty() {
    val primaryKey = PrimaryKey<TestTable>(emptyArray())
    assertThat(primaryKey.getConstraintClause()).isEmpty()
  }

  @Test
  fun of() {
    val result = PrimaryKey.of(TestTable::param1, TestTable::param2)
    assertThat(result.columns).asList().containsExactly(TestTable::param1, TestTable::param2)
  }

  @Test
  fun equals_valid() {
    val primaryKey = PrimaryKey(arrayOf(TestTable::param1, TestTable::param2))
    val otherPrimaryKey = PrimaryKey(arrayOf(TestTable::param1, TestTable::param2))
    assertThat(primaryKey == otherPrimaryKey).isTrue()
  }

  @Test
  fun equals_sameInstance() {
    val primaryKey = PrimaryKey(arrayOf(TestTable::param1, TestTable::param2))
    assertThat(primaryKey == primaryKey).isTrue()
  }

  @Test
  fun equals_notSameClass() {
    val primaryKey = PrimaryKey(arrayOf(TestTable::param1, TestTable::param2))
    val other = arrayOf(TestTable::param1, TestTable::param2)
    @Suppress("ReplaceCallWithComparison")
    assertThat(primaryKey.equals(other)).isFalse()
  }

  @Test
  fun equals_differentColumns() {
    val primaryKey = PrimaryKey(arrayOf(TestTable::param1, TestTable::param2))
    val otherPrimaryKey = PrimaryKey(arrayOf(TestTable::param1))
    assertThat(primaryKey == otherPrimaryKey).isFalse()
  }

  @Test
  fun hasCode_valid() {
    val primaryKey = PrimaryKey(arrayOf(TestTable::param1, TestTable::param2))
    val otherPrimaryKey = PrimaryKey(arrayOf(TestTable::param1, TestTable::param2))
    assertThat(primaryKey.hashCode()).isEqualTo(otherPrimaryKey.hashCode())
  }
}

private data class TestTable(val param1: Int, val param2: Int) : TableColumns<TestTable>()
