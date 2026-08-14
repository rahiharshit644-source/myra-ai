package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.AssistantState
import com.example.viewmodel.MahiMood
import com.example.viewmodel.MahiViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Myra", appName)
    }

    @Test
    fun `viewModel initial state test`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = MahiViewModel(app)
        assertEquals(AssistantState.DISCONNECTED, vm.uiState.value.assistantState)
        assertEquals(MahiMood.SASSY, vm.uiState.value.currentMood)
        assertNotNull(vm.uiState.value.mahiResponse)
    }
}

