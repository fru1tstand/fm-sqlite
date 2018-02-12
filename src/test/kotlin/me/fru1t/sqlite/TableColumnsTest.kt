package me.fru1t.sqlite

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TableColumnsTest {
  @Test
  fun kClass_getDatabaseName() {
    assertThat(LongTableNameClassForUseInTesting::class.getDatabaseName())
        .isEqualTo("long_table_name_class_for_use_in_testing")
  }

  @Test
  fun kProperty1_getDatabaseName() {
    assertThat(LongColumnClass::thisHasAReallyLongColumnName.getDatabaseName())
        .isEqualTo("this_has_a_really_long_column_name")
  }

  @Test
  fun kProperty1_getTable() {
    assertThat(ValidTable::id.getTable()).isEqualTo(ValidTable::class)
  }
}

private data class LongTableNameClassForUseInTesting(val id: Int) :
    TableColumns<LongTableNameClassForUseInTesting>()
private data class LongColumnClass(val thisHasAReallyLongColumnName: String) :
    TableColumns<LongColumnClass>()

private data class ValidTable(val id: Int) : TableColumns<ValidTable>()
