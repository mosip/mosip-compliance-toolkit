package io.mosip.compliance.toolkit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.compliance.toolkit.repository.TestRunDetailsRepository;
import io.mosip.compliance.toolkit.repository.TestRunRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;

@Service
public class TestRunArchiveService {

	@Autowired
	TestRunRepository testRunRepository;

	@Autowired
	TestRunDetailsRepository testRunDetailsRepository;

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getPartnerId() {
		String partnerId = authUserDetails().getUsername();
		return partnerId;
	}

	@Transactional
	public void archiveTestRun(String runId) {
		testRunDetailsRepository.copyTestRunDetailsToArchive(runId, getPartnerId());
		testRunRepository.copyTestRunToArchive(runId, getPartnerId());
		testRunDetailsRepository.deleteById(runId, getPartnerId());
		testRunRepository.deleteById(runId, getPartnerId());
	}
}
