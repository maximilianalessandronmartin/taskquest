package org.novize.api.dtos;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
public class UploadDto {
    String filename;
    String originalFilename;
    String fileUrl;
    Boolean success;
}
