package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PureWeightCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Silver ERP", appName)
  }

  @Test
  fun `test pure weight calculation formula`() {
    // Spec Example: 6369 weight @ 76.00 touch = 4840.4 pure weight
    val pure1 = PureWeightCalculator.calculate(6369.0, 76.00, 0.0)
    assertEquals(4840.4, pure1, 0.1)

    // Spec Example: 3616 weight @ 67.02 touch = 2423.4 pure weight
    val pure2 = PureWeightCalculator.calculate(3616.0, 67.02, 0.0)
    assertEquals(2423.4, pure2, 0.1)

    // Spec Example: 1415 weight @ 65.59 touch = 928.1 pure weight
    val pure3 = PureWeightCalculator.calculate(1415.0, 65.59, 0.0)
    assertEquals(928.1, pure3, 0.1)
  }
}
