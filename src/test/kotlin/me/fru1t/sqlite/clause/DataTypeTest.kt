package me.fru1t.sqlite.clause

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class DataTypeTest {
  @Test
  fun getClause() {
    assertThat(DataType.INTEGER.getClause()).isEqualTo("INTEGER")
    assertThat(DataType.TEXT.getClause()).isEqualTo("TEXT")
    assertThat(DataType.BLOB.getClause()).isEqualTo("BLOB")
    assertThat(DataType.REAL.getClause()).isEqualTo("REAL")
    assertThat(DataType.NUMERIC.getClause()).isEqualTo("NUMERIC")
  }
}
