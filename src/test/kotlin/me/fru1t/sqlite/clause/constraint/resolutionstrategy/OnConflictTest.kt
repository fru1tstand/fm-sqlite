package me.fru1t.sqlite.clause.constraint.resolutionstrategy

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class OnConflictTest {
  @Test
  fun getSqlClause() {
    assertThat(OnConflict.ROLLBACK.getClause()).isEqualTo("ON CONFLICT ROLLBACK")
    assertThat(OnConflict.ABORT.getClause()).isEqualTo("ON CONFLICT ABORT")
    assertThat(OnConflict.FAIL.getClause()).isEqualTo("ON CONFLICT FAIL")
    assertThat(OnConflict.IGNORE.getClause()).isEqualTo("ON CONFLICT IGNORE")
    assertThat(OnConflict.REPLACE.getClause()).isEqualTo("ON CONFLICT REPLACE")
  }
}
