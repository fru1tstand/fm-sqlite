package me.fru1t.sqlite.clause.resolutionstrategy

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class OnForeignKeyConflictTest {
  @Test
  fun getOnUpdateClause() {
    assertThat(OnForeignKeyConflict.RESTRICT.getOnUpdateClause()).isEqualTo("ON UPDATE RESTRICT")
    assertThat(OnForeignKeyConflict.NO_ACTION.getOnUpdateClause()).isEqualTo("ON UPDATE NO ACTION")
    assertThat(OnForeignKeyConflict.CASCADE.getOnUpdateClause()).isEqualTo("ON UPDATE CASCADE")
    assertThat(OnForeignKeyConflict.SET_NULL.getOnUpdateClause()).isEqualTo("ON UPDATE SET NULL")
    assertThat(OnForeignKeyConflict.SET_DEFAULT.getOnUpdateClause())
        .isEqualTo("ON UPDATE SET DEFAULT")
  }

  @Test
  fun getOnDeleteClause() {
    assertThat(OnForeignKeyConflict.RESTRICT.getOnDeleteClause()).isEqualTo("ON DELETE RESTRICT")
    assertThat(OnForeignKeyConflict.NO_ACTION.getOnDeleteClause()).isEqualTo("ON DELETE NO ACTION")
    assertThat(OnForeignKeyConflict.CASCADE.getOnDeleteClause()).isEqualTo("ON DELETE CASCADE")
    assertThat(OnForeignKeyConflict.SET_NULL.getOnDeleteClause()).isEqualTo("ON DELETE SET NULL")
    assertThat(OnForeignKeyConflict.SET_DEFAULT.getOnDeleteClause())
        .isEqualTo("ON DELETE SET DEFAULT")
  }

  @Test
  fun getClause() {
    assertThat(OnForeignKeyConflict.RESTRICT.getClause()).isEqualTo("RESTRICT")
    assertThat(OnForeignKeyConflict.NO_ACTION.getClause()).isEqualTo("NO ACTION")
    assertThat(OnForeignKeyConflict.CASCADE.getClause()).isEqualTo("CASCADE")
    assertThat(OnForeignKeyConflict.SET_NULL.getClause()).isEqualTo("SET NULL")
    assertThat(OnForeignKeyConflict.SET_DEFAULT.getClause()).isEqualTo("SET DEFAULT")
  }
}
