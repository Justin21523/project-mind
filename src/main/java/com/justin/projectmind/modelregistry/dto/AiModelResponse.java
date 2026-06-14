package com.justin.projectmind.modelregistry.dto;

import com.justin.projectmind.modelregistry.entity.Modality;
import com.justin.projectmind.modelregistry.entity.ModelFormat;

import java.time.Instant;

public record AiModelResponse(
        Long id,
        Long ownerId,
        String name,
        String provider,
        Modality modality,
        ModelFormat format,
        String quantization,
        Integer estimatedVramMb,
        String useCase,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
