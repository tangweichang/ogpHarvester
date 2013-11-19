/*
 * IngestFormBean.java
 *
 * Copyright (C) 2013
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
 * Authors:: Jose García (mailto:jose.garcia@geocat.net)
 */
package org.opengeoportal.harvester.mvc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opengeoportal.harvester.api.domain.CustomRepository;
import org.opengeoportal.harvester.api.domain.InstanceType;
import org.opengeoportal.harvester.api.service.CustomRepositoryService;
import org.opengeoportal.harvester.mvc.bean.CustomRepositoryFormBean;
import org.opengeoportal.harvester.mvc.bean.RemoteRepositoryFormBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;

@Controller
@SessionAttributes(types = { CustomRepositoryFormBean.class })
public class CustomRepositoryController {
	@Autowired
	private CustomRepositoryService service;

	@RequestMapping("/rest/repositories")
	@ResponseBody
	public Map<String, List<SimpleEntry<Long, String>>> getCustomRepositories() {

		ListMultimap<String, CustomRepository> multimap = service
				.getAllGroupByType();
		Map<String, List<SimpleEntry<Long, String>>> result = Maps.newHashMap();
		String[] instanceTypes = new String[] { InstanceType.SOLR.toString(),
				InstanceType.GEONETWORK.toString(),
				InstanceType.CSW.toString(), InstanceType.WEBDAV.toString() };
		for (String instanceType : instanceTypes) {
			List<CustomRepository> reposOfType = multimap.get(instanceType);
			List<SimpleEntry<Long, String>> repositories = new ArrayList<AbstractMap.SimpleEntry<Long, String>>();
			for (CustomRepository repository : reposOfType) {
				SimpleEntry<Long, String> entry = new SimpleEntry<Long, String>(
						repository.getId(), repository.getName());
				repositories.add(entry);
			}
			result.put(instanceType, repositories);
		}
		return result;
	}
	
	@RequestMapping("/rest/repositories/{repoId}/remoteSources")
	@ResponseBody
	public List<SimpleEntry<String, String>> getRemoteRepositoriesById(@PathVariable Long repoId) {
		return service.getRemoteRepositoriesByRepoId(repoId);
		
	}

	@RequestMapping(value = "/rest/repositoriesbyurl/remoteSources", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<SimpleEntry<String, String>> getRemoteRepositoriesByUrl(
			@RequestBody RemoteRepositoryFormBean repository)
			throws MalformedURLException {
		URL urlObj = new URL(repository.getRepoUrl());

		List<SimpleEntry<String, String>> repositories = service
				.getRemoteRepositories(repository.getRepoType(), urlObj);

		return repositories;
	}

	@RequestMapping(value = "/rest/localSolr/institutions")
	@ResponseBody
	public List<SimpleEntry<String, String>> getLocalSolrInstitutions() {
		List<SimpleEntry<String, String>> institutions = service
				.getLocalSolrInstitutions();
		return institutions;
	}

	@RequestMapping(value = "/admin")
	public String admin(ModelMap model) {
		CustomRepositoryFormBean formBean = new CustomRepositoryFormBean();
		model.put("customRepositoryFormBean", formBean);

		// TODO: Test data, remove.
		List<CustomRepository> customRepositoriesList = new ArrayList<CustomRepository>();
		CustomRepository cr = new CustomRepository();
		cr.setName("Repository 1");
		cr.setUrl("http://repository1.com");
		customRepositoriesList.add(cr);

		cr = new CustomRepository();
		cr.setName("Repository 2");
		cr.setUrl("http://repository2.com");
		customRepositoriesList.add(cr);

		model.put("customRepositories", customRepositoriesList);

		return "admin";
	}
}