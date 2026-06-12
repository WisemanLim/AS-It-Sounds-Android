package com.asitsounds.android.feature.library

import com.asitsounds.android.core.data.db.ProjectEntity
import com.asitsounds.android.core.data.db.ProjectStatus

enum class StatusFilter(val label: String) {
    ALL("전체"),
    PROCESSING("처리중"),
    DONE("완료"),
    ERROR("오류");

    fun matches(status: ProjectStatus): Boolean = when (this) {
        ALL        -> true
        PROCESSING -> status == ProjectStatus.PENDING || status == ProjectStatus.PROCESSING
        DONE       -> status == ProjectStatus.DONE
        ERROR      -> status == ProjectStatus.ERROR
    }
}

object LibraryFilter {
    fun apply(
        projects: List<ProjectEntity>,
        query: String,
        filter: StatusFilter
    ): List<ProjectEntity> {
        val q = query.trim().lowercase()
        return projects.filter { p ->
            filter.matches(p.status) &&
                (q.isEmpty() || p.name.lowercase().contains(q))
        }
    }
}
