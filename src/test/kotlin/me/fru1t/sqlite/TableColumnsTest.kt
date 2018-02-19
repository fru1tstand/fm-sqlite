package me.fru1t.sqlite

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TableColumnsFileTest {
  @Test
  fun kClass_getSqlName() {
    assertThat(LongTableNameClassForUseInTesting::class.getSqlName())
        .isEqualTo("long_table_name_class_for_use_in_testing")
  }

  @Test
  fun kProperty1_getSqlName() {
    assertThat(LongColumnClass::thisHasAReallyLongColumnName.getSqlName())
        .isEqualTo("this_has_a_really_long_column_name")
  }

  @Test
  fun kProperty1_getTable() {
    assertThat(TableColumnsTestTable::id.getTable()).isEqualTo(TableColumnsTestTable::class)
  }
}

class TableColumnsTest {
  @Test
  fun constructor() {
    // Adds coverage for the abstract class constructor
    object : TableColumns<TableColumnsTestTable>() { }
  }
}

private data class LongTableNameClassForUseInTesting(val id: Int) :
    TableColumns<LongTableNameClassForUseInTesting>()
private data class LongColumnClass(val thisHasAReallyLongColumnName: String) :
    TableColumns<LongColumnClass>()

private data class TableColumnsTestTable(val id: Int) : TableColumns<TableColumnsTestTable>()
