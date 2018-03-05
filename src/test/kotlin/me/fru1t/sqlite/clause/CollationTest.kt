package me.fru1t.sqlite.clause

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class CollationTest {
  @Test
  fun getClause() {
    assertThat(Collation.BINARY.getClause()).isEqualTo("COLLATE BINARY")
    assertThat(Collation.RTRIM.getClause()).isEqualTo("COLLATE RTRIM")
    assertThat(Collation.NOCASE.getClause()).isEqualTo("COLLATE NOCASE")
  }
}
