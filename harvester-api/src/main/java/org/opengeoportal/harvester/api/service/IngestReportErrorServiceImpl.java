/**
 * IngestReportErrorServiceImpl.java
 *
 * Copyright (C) 2014
 *
 * This file is part of Open Geoportal Harvester.
 *
 * This software is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * As a special exception, if you link this library with other files to produce
 * an executable, this library does not by itself cause the resulting executable
 * to be covered by the GNU General Public License. This exception does not
 * however invalidate any other reasons why the executable file might be covered
 * by the GNU General Public License.
 *
 * Authors:: Juan Luis Rodríguez (mailto:juanluisrp@geocat.net)
 */
package org.opengeoportal.harvester.api.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.opengeoportal.harvester.api.dao.IngestReportErrorRepository;
import org.opengeoportal.harvester.api.domain.IngestReportError;
import org.opengeoportal.harvester.api.domain.IngestReportErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author <a href="mailto:juanluisrp@geocat.net">Juan Luis Rodríguez</a>.
 * 
 */
@Service
public class IngestReportErrorServiceImpl implements IngestReportErrorService {
	/** Logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/** Report error repository. */
	@Autowired
	private IngestReportErrorRepository reportErrorRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opengeoportal.harvester.api.service.IngestReportErrorService#save
	 * (org.opengeoportal.harvester.api.domain.IngestReportError)
	 */
	@Override
	@Transactional
	public IngestReportError save(IngestReportError reportError) {
		return reportErrorRepository.save(reportError);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengeoportal.harvester.api.service.IngestReportErrorService#
	 * getCountErrorTypesByReportId(java.lang.Long)
	 */
	@Override
	@Transactional(readOnly = true)
	public Map<IngestReportErrorType, Long> getCountErrorTypesByReportId(
			Long reportId) {
		List<Object[]> items = reportErrorRepository
				.getCountErrorTypesByReportId(reportId);
		List<IngestReportErrorType> remainingTypes = Lists
				.newArrayList(IngestReportErrorType.values());
		Map<IngestReportErrorType, Long> result = Maps.newHashMap();
		for (Object item : items) {
			Object[] tuple = (Object[]) item;
			IngestReportErrorType errorType = (IngestReportErrorType) tuple[0];
			Long count = (Long) tuple[1];
			result.put(errorType, count);
			remainingTypes.remove(errorType);
		}

		// Set a default value for error types not returned by the repository
		for (IngestReportErrorType errorType : remainingTypes) {
			result.put(errorType, 0L);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengeoportal.harvester.api.service.IngestReportErrorService#
	 * getCountFieldErrorsByReportId(java.lang.Long)
	 */
	@Override
	@Transactional(readOnly = true)
	public Map<String, Long> getCountErrorsByReportId(Long id,
			IngestReportErrorType errorType) {
		List<Object[]> errorList = reportErrorRepository
				.getCountErrorsByReportId(id, errorType);
		Map<String, Long> result = Maps.newTreeMap();
		for (Object[] fieldError : errorList) {
			String fieldName = (String) fieldError[0];
			Long errorCount = (Long) fieldError[1];

			result.put(fieldName, errorCount);

		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengeoportal.harvester.api.service.IngestReportErrorService#
	 * writeErrorZipForIngest(java.lang.Long, java.util.zip.ZipOutputStream,
	 * java.lang.String[], java.lang.String[], java.lang.String[])
	 */
	@Override
	@Transactional(readOnly = true)
	public void writeErrorZipForIngest(Long reportId, ZipOutputStream out,
			String[] requiredFieldErrors, String[] webserviceErrors,
			String[] systemErrors) {

		writeErrors(reportId, IngestReportErrorType.REQUIRED_FIELD_ERROR,
				requiredFieldErrors, out);
		writeErrors(reportId, IngestReportErrorType.WEB_SERVICE_ERROR,
				webserviceErrors, out);
		writeErrors(reportId, IngestReportErrorType.SYSTEM_ERROR, systemErrors,
				out);

	}

	private void writeErrors(Long reportId, IngestReportErrorType errorType,
			String[] subcategories, ZipOutputStream out) {
		if (subcategories != null && subcategories.length > 0) {
			for (String subcat : subcategories) {
				String fieldDir = errorType.toString().toLowerCase(Locale.ENGLISH) + "/" + subcat;
				int page = 0;
				int pageSize = 100;
				boolean existNextPage = true;
				while (existNextPage) {
					Pageable pageRequest = new PageRequest(page, pageSize);
					Page<IngestReportError> p = reportErrorRepository
							.findByReportIdAndTypeAndField(reportId, errorType,
									subcat, pageRequest);
					for (IngestReportError error : p.getContent()) {
						String metadata = error.getMetadata();

						if (metadata != null) {
							try {
								out.putNextEntry(new ZipEntry(fieldDir
										+ "/error_" + error.getId() + ".xml"));
								out.write(metadata.getBytes(Charset
										.forName("UTF-8")));
							} catch (IOException e) {
								if (logger.isErrorEnabled()) {
									logger.error("Error adding entry (errorId="
											+ error.getId() + " to zip file", e);
								}
							}
						}
					}

					page++;
					existNextPage = page < p.getTotalPages();
				}
			}
		}
	}

}
