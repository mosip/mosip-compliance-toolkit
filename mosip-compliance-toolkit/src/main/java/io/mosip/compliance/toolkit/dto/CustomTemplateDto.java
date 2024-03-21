package io.mosip.compliance.toolkit.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class CustomTemplateDto {

    private String langCode;

    private String templateName;

    private String template;

    private LocalDateTime crDtimes;

    private String crBy;

}
