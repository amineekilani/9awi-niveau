package com.kawi_niveau.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class BulkActionRequest {
    private List<Long> userIds;
    private String action; // "archive", "activate", "delete", "change_role"
    private String newRole; // Pour l'action change_role
}