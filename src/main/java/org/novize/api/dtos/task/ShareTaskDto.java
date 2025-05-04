package org.novize.api.dtos.task;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareTaskDto {
    @NotBlank(message = "Benutzername darf nicht leer sein")
    private String username;
}
