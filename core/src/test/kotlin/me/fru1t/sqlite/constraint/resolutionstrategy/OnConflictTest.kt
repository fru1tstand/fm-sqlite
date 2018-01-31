package me.fru1t.sqlite.constraint.resolutionstrategy

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class OnConflictTest {
  @Test
  fun getSqlClause() {
    assertThat(OnConflict.ROLLBACK.getSqlClause()).isEqualTo("ON CONFLICT ROLLBACK")
    assertThat(OnConflict.ABORT.getSqlClause()).isEqualTo("ON CONFLICT ABORT")
    assertThat(OnConflict.FAIL.getSqlClause()).isEqualTo("ON CONFLICT FAIL")
    assertThat(OnConflict.IGNORE.getSqlClause()).isEqualTo("ON CONFLICT IGNORE")
    assertThat(OnConflict.REPLACE.getSqlClause()).isEqualTo("ON CONFLICT REPLACE")
  }
}
