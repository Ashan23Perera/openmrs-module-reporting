/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.reporting.report.service;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.report.Report;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.ReportRequest.Status;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.ReportRenderer;
import org.springframework.transaction.annotation.Transactional;

/**
 * ReportService API
 */
@Transactional
public interface ReportService extends OpenmrsService {
	
	//****** REPORT RENDERERS AND DESIGNS *****
		
	/**
	 * @return the ReportDesign with the given uuid
	 */
	@Transactional(readOnly = true)
	public ReportDesign getReportDesignByUuid(String uuid);
	
	/**
	 * @return the {@link ReportDesign} with the given id
	 */
	@Transactional(readOnly = true)
	public ReportDesign getReportDesign(Integer id);
	
	/**
	 * @return return a list of {@link ReportDesign}, optionally including those that are retired
	 */
	@Transactional(readOnly = true)
	public List<ReportDesign> getAllReportDesigns(boolean includeRetired);
	
	/**
	 * Return a list of {@link ReportDesign}s for {@link ReportDefinition} that match the passed parameters
	 * Each input parameter can be null, restricting the returned results only if it is not null.  This allows you
	 * to retrieve all ReportDesigns by ReportDefinition, by RendererType, by retired status, or a combination of these
	 * criteria.
	 * @param reportDefinitionId if not null, only {@link ReportDesign}s for this {@link ReportDefinition} will be returned
	 * @param rendererType if not null, only {@link ReportDesign}s for this {@link ReportRenderer} type will be returned
	 * @param includeRetired if true, indicates that retired {@link ReportDesign}s should also be included
	 * @return a List<ReportDesign> object containing all of the {@link ReportDesign}s
	 */
	@Transactional(readOnly = true)
	public List<ReportDesign> getReportDesigns(ReportDefinition reportDefinition, Class<? extends ReportRenderer> rendererType, boolean includeRetired);
	
	/**
	 * Save or update the given <code>ReportDesign</code> in the database. If this is a new
	 * ReportDesign, the returned ReportDesign will have a new
	 * {@link ReportDesign#getId()} inserted into it that was generated by the database
	 * @param reportDesign The <code>ReportDesign</code> to save or update
	 */
	public ReportDesign saveReportDesign(ReportDesign reportDesign);
	
	/**
	 * Purges a <code>ReportDesign</code> from the database.
	 * @param reportDesign The <code>ReportDesign</code> to remove from the system
	 */
	public void purgeReportDesign(ReportDesign reportDesign);
	
	/**
	 * @return a Collection<ReportRenderer> of all registered ReportRenderers
	 */
	@Transactional(readOnly = true)
	public Collection<ReportRenderer> getReportRenderers();
	
	/**
	 * @return the preferred ReportRenderer for the given class name
	 */
	@Transactional(readOnly = true)
	public ReportRenderer getReportRenderer(String className);
	
	/**
	 * @return	the preferred ReportRenderer for the given object type
	 */
	@Transactional(readOnly = true)
	public ReportRenderer getPreferredReportRenderer(Class<Object> objectType);
	
	/**
	 * @return a List of {@link RenderingMode}s that the passed {@link ReportDefinition} supports, in their preferred order
	 */
	@Transactional(readOnly = true)
	public List<RenderingMode> getRenderingModes(ReportDefinition schema);
	
	//****** REPORT REQUESTS *****
	
	/**
	 * Saves a {@link ReportRequest} to the database and returns it
	 */
	public ReportRequest saveReportRequest(ReportRequest request);

	/**
	 * @return the {@link ReportRequest} with the passed id
	 */
	@Transactional(readOnly = true)
	public ReportRequest getReportRequest(Integer id);

	/**
	 * @return the {@link ReportRequest} with the passed uuid
	 */
	@Transactional(readOnly = true)
	public ReportRequest getReportRequestByUuid(String uuid);
	
	/**
	 * @return all {@link ReportRequest} in the system that match the passed parameters
	 */
	@Transactional(readOnly = true)
	public List<ReportRequest> getReportRequests(ReportDefinition reportDefinition, Date requestOnOrAfter, Date requestOnOrBefore, Status...statuses);

	/**
	 * Deletes the passed {@link ReportRequest}
	 */
	public void purgeReportRequest(ReportRequest request);
	
	//***** REPORTS *****
	
	/**
	 * @return the File that may contain the serialized {@link ReportData} for a given {@link ReportRequest}
	 */
	@Transactional(readOnly = true)
	public File getReportDataFile(ReportRequest request);
	
	/**
	 * @return the File that may contain any errors when evaluating a given {@link ReportRequest}
	 */
	@Transactional(readOnly = true)
	public File getReportErrorFile(ReportRequest request);
	
	/**
	 * @return the File that may contain the rendered output from the evaluation of a {@link ReportRequest}
	 */
	@Transactional(readOnly = true)
	public File getReportOutputFile(ReportRequest request);
	
	/**
	 * <pre>
	 * Runs a report synchronously, blocking until the report is ready. This method populates the uuid
	 * field on the ReportRequest that is passed in, and adds the Request to the history.
	 * 
	 * If request specifies a WebRenderer, then the ReportDefinition will be evaluated, and the Report
	 * returned will contain the raw ReportData output, but no rendering will happen.
	 * 
	 * If request specifies a non-WebRenderer, the ReportDefinition will be evaluated <i>and</i> the
	 * data will be rendered, and the Report returned will include raw ReportData and a File.
	 * 
	 * Implementations of this service may choose to run the report directly, or to queue it,
	 * but if they queue it they should do so with HIGHEST priority.
	 * </pre>
	 * 
	 * @param request
	 * @return the result of running the report.
	 * @throws EvaluationException if the report could not be evaluated
	 * 
	 * @should set uuid on the request
	 * @should render the report if a plain renderer is specified
	 * @should not render the report if a web renderer is specified
	 */
	public Report runReport(ReportRequest request) throws EvaluationException;

	/**
	 * Adds a {@link ReportRequest} to the queue to be run asynchronously
	 */
	public ReportRequest queueReport(ReportRequest request);
	
	/**
	 * Loads the ReportData previously generated Report for the given ReportRequest, first checking the cache
	 */
	@Transactional(readOnly = true)
	public ReportData loadReportData(ReportRequest request);
	
	/**
	 * Loads the Rendered Output for a previously generated Report for the given ReportRequest, first checking the cache
	 */
	@Transactional(readOnly = true)
	public byte[] loadRenderedOutput(ReportRequest request);
	
	/**
	 * Loads the Error message for a previously generated Report for the given ReportRequest, first checking the cache
	 */
	@Transactional(readOnly = true)
	public String loadReportError(ReportRequest request);
	
	/**
	 * @return the persisted Report for the given ReportRequest
	 */
	@Transactional(readOnly = true)
	public Report loadReport(ReportRequest request) throws EvaluationException;
	
	/**
	 * @return any Reports that are currently cached
	 */
	@Transactional(readOnly = true)
	public Map<ReportRequest, Report> getCachedReports();

	/**
	 * Deletes report requests that are not saved, and are older than the value specified by
	 * {@link ReportingConstants#GLOBAL_PROPERTY_DELETE_REPORTS_AGE_IN_HOURS}
	 */
	public void deleteOldReportRequests();

	/**
	 * If there are any reports queued to be run, and we aren't running the maximum number of
	 * parallel reports, then start running the next queued report.
	 */
	public void maybeRunNextQueuedReport();

	/**
	 * Makes sure that the tasks for DeleteOldReports and RunQueuedReports are scheduled
	 */
	public void ensureScheduledTasksRunning();
}
