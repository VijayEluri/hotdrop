package org.qrone.r7.appengine;

import javax.servlet.ServletContext;

import org.qrone.database.DatabaseService;
import org.qrone.img.ImageSpriteService;
import org.qrone.kvs.KeyValueStoreService;
import org.qrone.login.CookieHandler;
import org.qrone.png.PNGMemoryImageService;
import org.qrone.r7.Extendable;
import org.qrone.r7.PortingService;
import org.qrone.r7.PortingServiceBase;
import org.qrone.r7.TaskManagerService;
import org.qrone.r7.fetcher.HTTPFetcher;
import org.qrone.r7.github.GitHubRepositoryService;
import org.qrone.r7.github.GitHubResolver;
import org.qrone.r7.handler.ExtendableURIHandler;
import org.qrone.r7.handler.DefaultHandler;
import org.qrone.r7.handler.PathFinderHandler;
import org.qrone.r7.handler.ResolverHandler;
import org.qrone.r7.resolver.FilteredResolver;
import org.qrone.r7.resolver.InternalResourceResolver;
import org.qrone.r7.resolver.SHAResolver;
import org.qrone.r7.resolver.URIResolver;
import org.qrone.r7.script.ext.ClassPrototype;
import org.qrone.r7.script.ext.ScriptableList;
import org.qrone.r7.script.ext.ScriptableMap;
import org.qrone.r7.tag.ImageHandler;
import org.qrone.r7.tag.Scale9Handler;
import org.qrone.r7.tag.SecurityTicketHandler;
 
public class AppEngineURIHandler extends ExtendableURIHandler{
	private KeyValueStoreService kvs = new AppEngineKVSService();
	private CookieHandler cookie = new CookieHandler(kvs);
	private HTTPFetcher fetcher = new AppEngineHTTPFetcher();
	private SHAResolver cache  = new AppEngineResolver();
	private DatabaseService db = new AppEngineDatastoreService();
	private GitHubResolver github = new GitHubResolver(fetcher, cache, 
			"qronon","qrone-admintool","master");
	private GitHubRepositoryService repository = new GitHubRepositoryService(fetcher, cache, db);
	
	public AppEngineURIHandler(ServletContext cx) {
		PNGMemoryImageService imagebufferservice = new PNGMemoryImageService();
		ImageSpriteService imagespriteservice = new ImageSpriteService(resolver, cache, imagebufferservice);
		
		resolver.add(imagespriteservice);
		resolver.add(github);
		resolver.add(repository.getResolver());
		resolver.add(new FilteredResolver("/system/resource/", new InternalResourceResolver(cx)));
		resolver.add(cache);
		
		PortingService services = new PortingServiceBase(
				fetcher, 
				resolver, 
				new AppEngineDatastoreService(), 
				new AppEngineMemcachedService(), 
				new AppEngineLoginService(), 
				imagebufferservice,
				imagespriteservice,
				repository,
				cookie,
				null  // TODO TaskManager unimplemented!
			);
		
		DefaultHandler defaulthandler = new DefaultHandler(services);
		
		rawextend(defaulthandler);
		rawextend(this);
		
		handler.add(cookie);
		handler.add(github);
		handler.add(repository);
		handler.add(defaulthandler);
	}
	
}
