package com.andreitufeanu.backend.ai.rag;

import com.andreitufeanu.backend.event.service.EventRagService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/rag")
@RequiredArgsConstructor
public class RagAdminController {

    private final EventRagService eventRagService;

    @PostMapping("/reindex-events")
    @PreAuthorize("hasRole('ADMIN')")
    public void reindexEvents() {
        eventRagService.reindexAllEvents();
    }
}