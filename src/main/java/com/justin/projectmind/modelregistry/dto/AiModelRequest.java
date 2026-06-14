package com.justin.projectmind.modelregistry.dto;

import com.justin.projectmind.modelregistry.entity.Modality;
import com.justin.projectmind.modelregistry.entity.ModelFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AiModelRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 100)
        String provider,

        @NotNull
        Modality modality,

        ModelFormat format,

        @Size(max = 50)
        String quantization,

        @Positive
        Integer estimatedVramMb,

        @Size(max = 500)
        String useCase,

        @Size(max = 2000)
        String notes
) {
}
