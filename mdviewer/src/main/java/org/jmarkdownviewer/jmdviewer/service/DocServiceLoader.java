package org.jmarkdownviewer.jmdviewer.service;

import java.io.File;
import java.nio.file.ProviderNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jmarkdownviewer.service.DocService;

public class DocServiceLoader {
	
	Logger log = LogManager.getLogger(DocServiceLoader.class);

	private static final String DEFAULT_PROVIDER = "org.jmarkdownviewer.service.md.MarkDownService";

	private static DocServiceLoader instance;

	private DocServiceLoader() {
	}

	public static DocServiceLoader getInstance() {
		if (instance == null)
			instance = new DocServiceLoader();
		return instance;
	}

	public DocService getProviderForFile(File file) {
		Marker marker = MarkerManager.getMarker("getProviderForFile");
		DocService service = null;
		for(DocService s : providers()) {			
			if(s.canHandle(file)) {
				log.debug(marker, "provider: {}", s.getClass().getCanonicalName());
				service = s;
				break;
			}				
		}
		if (service == null)
			try {
				service = provider();
			} catch (Exception e) {
				log.error(marker, e.getMessage());
				throw e;
			}
		return service;
	}
	
	public DocService provider() {
		return provider(DEFAULT_PROVIDER);
	}

	// provider by name
	public DocService provider(String providerName) {
		ServiceLoader<DocService> loader = ServiceLoader.load(DocService.class);
		Iterator<DocService> it = loader.iterator();
		while (it.hasNext()) {
			DocService provider = it.next();
			if (providerName.equals(provider.getClass().getName())) {
				return provider;
			}
		}
		throw new ProviderNotFoundException("DocService provider " + providerName + " not found");
	}

	public List<DocService> providers() {
		Marker marker = MarkerManager.getMarker("providers()");
		
		List<DocService> services = new ArrayList<>();
		try {			
			ServiceLoader<DocService> loader = ServiceLoader.load(DocService.class);
			Iterator<DocService> iter = loader.iterator();
			while(iter.hasNext()) {
				DocService ds = iter.next(); 
				log.debug(marker, "provider: {}", ds.getClass().getCanonicalName());
				services.add(ds);
			}
		} catch (Exception e) {
			log.error(marker, e.getMessage());
			throw e;
		}
		//loader.forEach(services::add);
		return services;
	}

}
