package io.mosip.compliance.toolkit.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Component
@Entity
@Table(name = "master_templates", schema = "toolkit")
@Data
@NoArgsConstructor
@ToString
public class MasterTemplatesEntity {

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

    @Column(name = "version")
    private String version;
}