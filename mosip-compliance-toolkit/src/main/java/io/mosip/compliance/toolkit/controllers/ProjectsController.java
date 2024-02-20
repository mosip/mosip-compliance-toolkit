package io.mosip.compliance.toolkit.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.projects.ProjectsResponseDto;
import io.mosip.compliance.toolkit.service.ProjectsService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.ResponseWrapper;


/**
 * This controller class defines the endpoints for all projects.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@RestController
@Tag(name = "projects-controller")
public class ProjectsController {

	@Autowired
	private ProjectsService projectsService;

	@Autowired
	private RequestValidator requestValidator;

	/**
	 * Initiates the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requestValidator);
	}

	/**
	 * Gives the list of all projects created by the user. If "type" is provided
	 * then will give filtered list of projects based on the type.
	 * 
	 * @param type   the type of project
	 * @param errors
	 * @return the list of all projects created by the user
	 */
	@GetMapping(value = "/getProjects")
	@Operation(summary = "Get projects", description = "Get partner's projects", tags = "projects-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<ProjectsResponseDto> getProjects(@RequestParam(required = false) String type) {
		return projectsService.getProjects(type);
	}

}