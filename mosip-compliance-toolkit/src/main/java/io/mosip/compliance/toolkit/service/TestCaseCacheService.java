package io.mosip.compliance.toolkit.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import io.mosip.compliance.toolkit.entity.TestCaseEntity;
import io.mosip.compliance.toolkit.repository.TestCasesRepository;

@Service
public class TestCaseCacheService {

	@Autowired
	TestCasesRepository testCasesRepository;

	@Cacheable(cacheNames = "testcases", key = "{#type, #specVersion}")
	public List<TestCaseEntity> getSbiTestCases(String type, String specVersion) {
		return testCasesRepository.findAllSbiTestCaseBySpecVersion(specVersion);
	}

	@Cacheable(cacheNames = "testcases", key = "{#type, #specVersion}")
	public List<TestCaseEntity> getSdkTestCases(String type, String specVersion) {
		return testCasesRepository.findAllSdkTestCaseBySpecVersion(specVersion);
	}

	@Cacheable(cacheNames = "testcases", key = "{#type, #specVersion}")
	public List<TestCaseEntity> getAbisTestCases(String type, String specVersion) {
		return testCasesRepository.findAllAbisTestCaseBySpecVersion(specVersion);
	}

	@CacheEvict(cacheNames = "testcases", key = "{#entity.testcaseType, #entity.specVersion}")
	public TestCaseEntity saveTestCase(TestCaseEntity entity) {
		return testCasesRepository.save(entity);
	}

	@CacheEvict(cacheNames = "testcases", key = "{#entity.testcaseType, #entity.specVersion}")
	public TestCaseEntity updateTestCase(TestCaseEntity entity) {
		return testCasesRepository.update(entity);
	}
}
