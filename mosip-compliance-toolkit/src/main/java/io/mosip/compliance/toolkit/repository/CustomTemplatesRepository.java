package io.mosip.compliance.toolkit.repository;

import io.mosip.compliance.toolkit.entity.CustomTemplatesEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository("CustomTemplatesRepository")
public interface CustomTemplatesRepository extends BaseRepository<CustomTemplatesEntity, String> {
    @Query(value = "SELECT * FROM custom_templates e WHERE e.lang_code = ?1 AND e.template_name = ?2 ORDER BY e.cr_dtimes DESC LIMIT 1", nativeQuery = true)
    public Optional<CustomTemplatesEntity> getTemplate(String langCode, String templateName);

    @Query("SELECT MAX(e.crDtimes) FROM CustomTemplatesEntity e where templateName = ?1")
    public LocalDateTime getLatestTemplateTimeStamp(String templateName);

}