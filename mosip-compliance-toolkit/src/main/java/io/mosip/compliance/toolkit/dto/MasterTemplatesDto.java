package io.mosip.compliance.toolkit.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class MasterTemplatesDto {

    private String langCode;

    private String templateName;

    private String template;

    private LocalDateTime crDtimes;

    private String crBy;

    private String version;

}
