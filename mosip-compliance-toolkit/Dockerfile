FROM openjdk:11

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

ARG spring_config_label 
ARG active_profile
ARG spring_config_url 
ARG is_glowroot
ARG artifactory_url

ENV active_profile_env=${active_profile}
ENV spring_config_label_env=${spring_config_label}
ENV spring_config_url_env=${spring_config_url}
ENV is_glowroot_env=${is_glowroot}
ENV artifactory_url_env=${artifactory_url}
ENV iam_adapter_url_env=${iam_adapter_url}

ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

RUN apt-get -y update \
&& apt-get install -y unzip \
&& groupadd -g ${container_user_gid} ${container_user_group} \
&& useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/sh -m ${container_user} \
&& mkdir -p /home/${container_user}/additional_jars/ \
&& chown -R ${container_user}:${container_user} /home/${container_user}

WORKDIR /home/${container_user}

COPY ./target/mosip-compliance-toolkit-service-*.jar mosip-compliance-toolkit-service.jar

USER ${container_user_uid}:${container_user_gid}

EXPOSE 8099

CMD if [ "$is_glowroot_env" = "present" ]; then \
    wget "${artifactory_url_env}"/artifactory/libs-release-local/io/mosip/testing/glowroot.zip ; \
    wget "${iam_adapter_url_env}" -O "/home/${container_user}/additional_jars/kernel-auth-adapter.jar"; \
    wget -q --show-progress "${virusscanner_url_env}" -O "/home/${container_user}/additional_jars/kernel-virusscanner-clamav.jar"; \    
    unzip glowroot.zip ; \
    rm -rf glowroot.zip ; \
    sed -i 's/<service_name>/mosip-compliance-toolkit-service/g' glowroot/glowroot.properties ; \
    java -Dloader.path="/home/${container_user}/additional_jars/" -jar -javaagent:glowroot/glowroot.jar -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}" -Dspring.cloud.config.uri="${spring_config_url_env}" mosip-compliance-toolkit-service.jar ; \
    else \
    wget "${iam_adapter_url_env}" -O "/home/${container_user}/additional_jars/kernel-auth-adapter.jar"; \
    wget -q --show-progress "${virusscanner_url_env}" -O "/home/${container_user}/additional_jars/kernel-virusscanner-clamav.jar"; \    
    java -Dloader.path="/home/${container_user}/additional_jars/" -jar -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}" -Dspring.cloud.config.uri="${spring_config_url_env}" mosip-compliance-toolkit-service.jar ; \
    fi
