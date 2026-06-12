package com.asitsounds.android.feature

import com.asitsounds.android.feature.navigation.Routes
import com.asitsounds.android.feature.navigation.TopDestination
import com.asitsounds.android.feature.onboarding.OnboardingPages
import org.junit.Assert.*
import org.junit.Test

class NavigationTest {

    @Test
    fun projectDetail_buildsRoute() {
        assertEquals("project/42", Routes.projectDetail(42))
    }

    @Test
    fun extractProjectId_fromConcreteRoute() {
        assertEquals(42L, Routes.extractProjectId("project/42"))
    }

    @Test
    fun extractProjectId_invalid_returnsNull() {
        assertNull(Routes.extractProjectId("project/{projectId}"))
        assertNull(Routes.extractProjectId(null))
    }

    @Test
    fun topDestinations_haveFourTabs() {
        assertEquals(4, TopDestination.entries.size)
        assertEquals("home", TopDestination.HOME.route)
        assertEquals("library", TopDestination.LIBRARY.route)
        assertEquals("projects", TopDestination.PROJECTS.route)
        assertEquals("settings", TopDestination.SETTINGS.route)
    }

    @Test
    fun topDestinations_routesUnique() {
        val routes = TopDestination.entries.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun onboarding_hasFourPages() {
        assertEquals(4, OnboardingPages.pages.size)
    }

    @Test
    fun onboarding_allPagesNonBlank() {
        OnboardingPages.pages.forEach {
            assertTrue(it.title.isNotBlank())
            assertTrue(it.subtitle.isNotBlank())
            assertTrue(it.emoji.isNotBlank())
            assertTrue("each page should have features or previews",
                it.features.isNotEmpty() || it.previews.isNotEmpty())
        }
    }

    @Test
    fun onboarding_firstPageIsBrand() {
        assertEquals("As It Sounds", OnboardingPages.pages.first().title)
    }
}
