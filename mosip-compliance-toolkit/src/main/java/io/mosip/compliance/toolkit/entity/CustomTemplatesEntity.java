package io.mosip.compliance.toolkit.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.time.LocalDateTime;

@Component
@Entity
@Table(name = "custom_templates", schema = "toolkit")
@Data
@NoArgsConstructor
@ToString
public class CustomTemplatesEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "lang_code")
    private String langCode;

    @Column(name = "template_name")
    private String templateName;

    @Column(name = "template")
    private String template;

    @Column(name = "cr_dtimes")
    private LocalDateTime crDtimes;

    @Column(name = "cr_by")
    private String crBy;
}