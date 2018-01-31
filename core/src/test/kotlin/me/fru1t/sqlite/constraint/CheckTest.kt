package me.fru1t.sqlite.constraint

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class CheckTest {
  @Test
  fun getConstraintClause() {
    val check = Check("`username` IS NOT NULL OR `email` IS NOT NULL")
    assertThat(check.getConstraintClause())
        .isEqualTo("CHECK (`username` IS NOT NULL OR `email` IS NOT NULL)")
  }

  @Test
  fun getConstraintClause_noLogic() {
    val check = Check("")
    assertThat(check.getConstraintClause()).isEmpty()
  }
}
