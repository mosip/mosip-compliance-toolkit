package io.mosip.compliance.toolkit.repository;

import io.mosip.compliance.toolkit.entity.MasterTemplatesEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository("MasterTemplatesRepository")
public interface MasterTemplatesRepository extends BaseRepository<MasterTemplatesEntity, String> {
    @Query("SELECT e FROM MasterTemplatesEntity e WHERE e.langCode = ?1 AND e.templateName = ?2 AND e.version = ?3")
    public Optional<MasterTemplatesEntity> getTemplate(String langCode, String templateName, String version);

    @Query(value = "SELECT e.version FROM master_templates e WHERE e.template_name = ?1 ORDER BY e.cr_dtimes DESC LIMIT 1", nativeQuery = true)
    public String getLatestTemplateVersion(String templateName);

    @Query(value = "SELECT e.cr_dtimes FROM master_templates e WHERE e.version = ?1 AND e.template_name = ?2 ORDER BY e.cr_dtimes DESC LIMIT 1", nativeQuery = true)
    public LocalDateTime getTimestampForTemplateVersion(String version, String templateName);

    @Query(value = "SELECT e.version FROM master_templates e WHERE e.lang_code = ?1 AND e.template_name = ?2 ORDER BY e.cr_dtimes DESC LIMIT 1", nativeQuery = true)
    public String getPreviousTemplateVersion(String langCode, String templateName);
}