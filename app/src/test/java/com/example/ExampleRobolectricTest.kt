package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
    assertEquals("Eclipse", appName)
  }

  @Test
  fun `test game repository settings defaults`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.eclipse.data.GameRepository(context)
    val settings = repo.loadSettings()
    assertEquals(1.0f, settings.buttonScale, 0.01f)
    assertEquals("DEFAULT", settings.buttonLayoutPreset)
    assertEquals(20f, settings.buttonVerticalOffset, 0.01f)
  }

  @Test
  fun `test player flashlight and battery states`() {
    val player = com.example.eclipse.model.Player()
    assertEquals(true, player.flashlightOn)
    assertEquals(95f, player.batteryPercent, 0.01f)
    assertEquals(false, player.isHiding)
  }
}
