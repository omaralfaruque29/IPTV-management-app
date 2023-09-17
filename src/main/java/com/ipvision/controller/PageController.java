package com.ipvision.controller;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ipvision.domain.Category;
import com.ipvision.domain.ChannelLink;
import com.ipvision.domain.ChannelLog;
import com.ipvision.domain.Country;
import com.ipvision.domain.Language;
import com.ipvision.domain.LiveChannel;
import com.ipvision.domain.PrimaryTag;
import com.ipvision.domain.Role;
import com.ipvision.domain.Server;
import com.ipvision.domain.Tag;
import com.ipvision.domain.User;
import com.ipvision.domain.UserRole;
import com.ipvision.domain.VOD;
import com.ipvision.monitorModule.FFMPEGCommands;
import com.ipvision.monitorModule.threads.MonitoringThread;
import com.ipvision.service.CategoryService;
import com.ipvision.service.ChannelLinkService;
import com.ipvision.service.ChannelLogService;
import com.ipvision.service.CommandService;
import com.ipvision.service.CountryService;
import com.ipvision.service.LanguageService;
import com.ipvision.service.LiveChannelService;
import com.ipvision.service.RoleService;
import com.ipvision.service.ServerService;
import com.ipvision.service.TagService;
import com.ipvision.service.UserRoleService;
import com.ipvision.service.UserService;
import com.ipvision.service.VODService;
import com.ipvision.threads.FFMPEGServerResponseReceive;
import com.ipvision.threads.ServerHandler;

@Controller
@RequestMapping("/page")
public class PageController {

	@Autowired
	UserService userService;

	@Autowired
	CountryService countryService;

	@Autowired
	LanguageService languageService;

	@Autowired
	CategoryService categoryService;

	@Autowired
	TagService tagService;

	@Autowired
	RoleService roleService;

	@Autowired
	UserRoleService userRoleService;

	@Autowired
	VODService vodService;

	@Autowired
	LiveChannelService liveChannelService;

	@Autowired
	ChannelLinkService channelLinkService;

	@Autowired
	ChannelLogService channelLogService;

	@Autowired
	ServerService serverService;

	@Autowired
	CommandService commandService;

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	ServletContext context;
	
	int counter = 0;

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index(Tag tag, User user, final ModelMap model) {
		
		if(counter == 0){
//			System.out.println("Monitor thread started.."+counter);
//			Thread monitoringThread = new Thread(new MonitoringThread());
//			monitoringThread.start();
			counter ++;
		}
		
		model.put("user", user);
		List<String> tags = new ArrayList<String>();
		try {
			tags = tagService.getAllTags();
			List<PrimaryTag> primarytags = tagService.getAllPrimaryTags();
			for (PrimaryTag pt : primarytags) {
				tags.add(pt.getPrimaryTagName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.put("tags", tags);

		Map<Integer, String> countryMap = new HashMap<Integer, String>();
		List countries = countryService.returnAllCountry();
		for (Iterator iterator = countries.iterator(); iterator.hasNext();) {
			Country country = (Country) iterator.next();
			countryMap.put(country.getCountryId(), country.getCountryName());
		}

		model.put("countryMap", countryMap);
		model.put("id", "new");

		return "index";
	}

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String home(final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			
			return "redirect:" + "/page/ownchannel/alll/1";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String login(User user, ModelMap model, HttpSession session) {

		session.setAttribute("Admin", "");
		session.setAttribute("Publisher", "");

		boolean check = userService.checkUserExist(user);
		Set<UserRole> roleSet = userService.returnUserRoles(user);
		for (UserRole userRole : roleSet) {
			if (userRole.getUserRoleName().equals("Admin")) {
				session.setAttribute("Admin", "Admin");
			} else if (userRole.getUserRoleName().equals("Publisher")) {
				session.setAttribute("Publisher", "Publisher");
			}
		}

		if (check) {

			User userFromDb = userService.returnUser(user);

			session.setAttribute("loggedin", "true");
			session.setAttribute("username", userFromDb.getUserName());
			session.setAttribute("userId", userFromDb.getUserId());
			
			return "redirect:" + "/page/ownchannel/alll/1";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			session.setAttribute("loggedin", "");
			session.setAttribute("username", "");
			session.setAttribute("userId", "");
			session.setAttribute("Admin", "");
			session.setAttribute("Publisher", "");
			session.invalidate();

			return "redirect:" + "/page/index";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/country/{id}", method = RequestMethod.GET)
	public String showCountry(@PathVariable String id, Country country,
			final ModelMap model, HttpSession session) {
		System.out.println(session.getAttribute("loggedin"));
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			Country ct = null;
			if (id == null || id.equalsIgnoreCase("new")) {
				model.put("country", country);
				model.put("id", id);
			} else if (!id.equals("new")) {
				model.put("id", id);

				ct = countryService.returnCountryById(id);

				if (ct != null) {
					model.put("country", ct);
				}
			}
			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "country";
		} else {

			return "redirect:" + "/page/index";
		}
	}

	@RequestMapping(value = "/country/all/{ctPerPage}/{startCountryNo}", method = RequestMethod.GET)
	public String showAllCountry(@PathVariable String ctPerPage,
			@PathVariable String startCountryNo, @RequestParam String i,
			final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int selectedCountryPerPage = Integer.parseInt(ctPerPage);
			session.setAttribute("selectedCountryPerPage",
					selectedCountryPerPage);
			int startCountry = Integer.parseInt(startCountryNo);
			session.setAttribute("lastCountryStatus", startCountry);

			List<Country> countries = countryService.returnAllCountry(
					startCountry, selectedCountryPerPage);
			Integer totalCountry = countryService.returnNumberOfCountry();

			int lastCountryStatus = startCountry;

			model.put("countries", countries);
			float totalPage = (float) totalCountry / selectedCountryPerPage;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastCountryStatus", lastCountryStatus);
			model.put("selectedCountryPerPage", selectedCountryPerPage);

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("context", context.getContextPath());
			model.put("username", session.getAttribute("username"));
			if (i.equals("e")) {
				model.put("indicator", "error");
			} else if (i.equals("s")) {
				model.put("indicator", "success");
			} else {
				model.put("indicator", "");
			}
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "allCountry";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/country/alll/{pageNo}", method = RequestMethod.GET)
	public String showAllCountry(@PathVariable String pageNo,
			final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int countryPerPage = 10;
			int lastCountryStatus = 0;
			int pageNumber = Integer.parseInt(pageNo);
			int selectedCountryPerPage = (Integer) session
					.getAttribute("selectedCountryPerPage");
			int first = (pageNumber * selectedCountryPerPage)
					- (selectedCountryPerPage - 1);
			first--;

			List<Country> countries = countryService.returnAllCountry(first,
					selectedCountryPerPage);
			Integer totalCountry = countryService.returnNumberOfCountry();

			lastCountryStatus = first;

			model.put("countries", countries);
			float totalPage = (float) totalCountry / 10;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastCountryStatus", lastCountryStatus);
			model.put("selectedCountryPerPage", selectedCountryPerPage);

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("context", context.getContextPath());
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "allCountry";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/country/save", method = RequestMethod.POST)
	public String saveCountry(Country country, @RequestParam String oldId,
			ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			if (oldId.equals("new")) {
				try {
					countryService.saveCountry(country);
					/**
					 * primary tag save to database
					 */
					PrimaryTag pTag = new PrimaryTag();
					pTag.setPrimaryTagName(country.getCountryName());
					tagService.savePrimaryTag(pTag);
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/country/all/10/0?i=e";
				}

				return "redirect:" + "/page/country/all/10/0?i=s";
			} else {
				try {
					countryService.updateCountry(country, oldId);
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/country/all/10/0?i=e";
				}

				return "redirect:" + "/page/country/all/10/0?i=s";
			}
		} else {

			return "redirect:" + "/page/index";
		}

	}
	
	@RequestMapping(value = "/country/delete", method = RequestMethod.POST)
	public String deleteCountry(Country country, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {
            int start = (Integer) session.getAttribute("lastCountryStatus");
			try {
		    	System.out.println("country.getCountryId():" +country.getCountryId());
				
				String countryId = Integer.toString(country.getCountryId());
				Country ct = countryService.returnCountryById(countryId);
				countryService.deleteCountry(ct);
				String[] tagsArray = {ct.getCountryName()};
				PrimaryTag  pTag = tagService.getSpecificPrimaryTags(tagsArray).get(0);
				tagService.deletePrimaryTag(pTag);
				
			} catch (Exception e) {
				e.printStackTrace();
				return "redirect:" + "/page/country/all/10/" + start + "?i=e";
			}

			return "redirect:" + "/page/country/all/10/" + start + "?i=s";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/language/{id}", method = RequestMethod.GET)
	public String showLanguage(@PathVariable String id, Language language,
			final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			Language ln = null;
			if (id == null || id.equalsIgnoreCase("new")) {
				model.put("language", language);
				model.put("id", id);
			} else if (!id.equals("new")) {
				model.put("id", id);

				ln = languageService.returnLanguageById(id);

				if (ln != null) {
					model.put("language", ln);
				}
			}

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "language";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/language/all/{languagePerPage}/{startLanguageNo}", method = RequestMethod.GET)
	public String showAllLanguage(@PathVariable String languagePerPage,
			@PathVariable String startLanguageNo, @RequestParam String i,
			final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int selectedLanguagePerPage = Integer.parseInt(languagePerPage);
			session.setAttribute("selectedLanguagePerPage",
					selectedLanguagePerPage);
			int startLanguage = Integer.parseInt(startLanguageNo);
			session.setAttribute("lastLanguageStatus", startLanguage);
			int lastLanguageStatus = startLanguage;

			List<Language> languages = languageService.returnAllLanguage(
					startLanguage, selectedLanguagePerPage);
			Integer totalLanguage = languageService.returnNumberOfLanguage();

			model.put("languages", languages);
			float totalPage = (float) totalLanguage / selectedLanguagePerPage;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastLanguageStatus", lastLanguageStatus);
			model.put("selectedLanguagePerPage", selectedLanguagePerPage);

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("context", context.getContextPath());
			model.put("username", session.getAttribute("username"));

			if (i.equals("e")) {
				model.put("indicator", "error");
			} else if (i.equals("s")) {
				model.put("indicator", "success");
			} else {
				model.put("indicator", "");
			}
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "allLanguage";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/language/alll/{pageNo}", method = RequestMethod.GET)
	public String showAllLanguages(@PathVariable String pageNo,
			final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int lastLanguageStatus = 0;
			int pageNumber = Integer.parseInt(pageNo);
			int selectedLanguagePerPage = (Integer) session
					.getAttribute("selectedLanguagePerPage");
			int first = (pageNumber * selectedLanguagePerPage)
					- (selectedLanguagePerPage - 1);
			first--;

			lastLanguageStatus = first;

			List<Language> languages = languageService.returnAllLanguage(first,
					selectedLanguagePerPage);
			Integer totalLanguage = languageService.returnNumberOfLanguage();

			model.put("languages", languages);
			float totalPage = (float) totalLanguage / 10;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastLanguageStatus", lastLanguageStatus);
			model.put("selectedLanguagePerPage", selectedLanguagePerPage);

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("context", context.getContextPath());
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "allLanguage";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/language/save", method = RequestMethod.POST)
	public String saveLanguage(Language language, @RequestParam String oldId,
			ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			if (oldId.equals("new")) {

				try {
					languageService.saveLanguage(language);
					PrimaryTag pTag = new PrimaryTag();
					pTag.setPrimaryTagName(language.getLanguageName());
					tagService.savePrimaryTag(pTag);
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/language/all/10/0?i=e";
				}

				model.put("Admin", session.getAttribute("Admin"));
				model.put("Publisher", session.getAttribute("Publisher"));

				return "redirect:" + "/page/language/all/10/0?i=s";
			} else {

				try {
					languageService.updateLanguage(language, oldId);
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/language/all/10/0?i=e";
				}

				model.put("Admin", session.getAttribute("Admin"));
				model.put("Publisher", session.getAttribute("Publisher"));

				return "redirect:" + "/page/language/all/10/0?i=s";
			}
		} else {

			return "redirect:" + "/page/index";
		}

	}
	
	@RequestMapping(value = "/language/delete", method = RequestMethod.POST)
	public String deleteLanguage(Language language, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {
            int start = (Integer) session.getAttribute("lastLanguageStatus");
			try {
				//System.out.println("country.getCountryId():" +country.getCountryId());
				Language lang = languageService.returnLanguageById(Integer.toString(language.getLanguageId()));
				languageService.deleteLanguage(lang);
				String[] tagsArray = {lang.getLanguageName()};
				PrimaryTag  pTag = tagService.getSpecificPrimaryTags(tagsArray).get(0);
				tagService.deletePrimaryTag(pTag);
				
			} catch (Exception e) {
				e.printStackTrace();
				return "redirect:" + "/page/language/all/10/" + start + "?i=e";
			}

			return "redirect:" + "/page/language/all/10/" + start + "?i=s";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/category/{id}", method = RequestMethod.GET)
	public String showCategory(@PathVariable String id, Category category,
			final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			Category cat = null;
			if (id == null || id.equalsIgnoreCase("new")) {
				model.put("category", category);
				model.put("id", id);
			} else if (!id.equals("new")) {
				model.put("id", id);

				cat = categoryService.returnCategoryById(id);

				if (cat != null) {
					model.put("category", cat);
				}
			}

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "category";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/category/all/{categorytPerPage}/{startCategoryNo}", method = RequestMethod.GET)
	public String showAllCategory(@PathVariable String categorytPerPage,
			@PathVariable String startCategoryNo, @RequestParam String i,
			final ModelMap model, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int selectedCategoryPerPage = Integer.parseInt(categorytPerPage);
			int startCategory = Integer.parseInt(startCategoryNo);
			session.setAttribute("selectedCategoryPerPage",
					selectedCategoryPerPage);
			session.setAttribute("lastCategoryStatus", startCategory);

			List<Category> categories = categoryService.returnAllCategory(
					startCategory, selectedCategoryPerPage);
			Integer totalCategory = categoryService.returnNumberOfCategory();

			int lastCategoryStatus = startCategory;

			model.put("categories", categories);
			float totalPage = (float) totalCategory / selectedCategoryPerPage;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastCategoryStatus", lastCategoryStatus);
			model.put("selectedCategoryPerPage", selectedCategoryPerPage);

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("context", context.getContextPath());
			model.put("username", session.getAttribute("username"));

			if (i.equals("e")) {
				model.put("indicator", "error");
			} else if (i.equals("s")) {
				model.put("indicator", "success");
			} else {
				model.put("indicator", "");
			}
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "allCategory";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/category/alll/{pageNo}", method = RequestMethod.GET)
	public String showAllCategory(@PathVariable String pageNo,
			final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			int lastCategoryStatus = 0;
			int pageNumber = Integer.parseInt(pageNo);
			int selectedCategoryPerPage = (Integer) session
					.getAttribute("selectedCategoryPerPage");
			int first = (pageNumber * selectedCategoryPerPage)
					- (selectedCategoryPerPage - 1);
			first--;

			List<Category> categories = categoryService.returnAllCategory(
					first, selectedCategoryPerPage);
			Integer totalCategory = categoryService.returnNumberOfCategory();

			model.put("categories", categories);
			float totalPage = (float) totalCategory / 10;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastCategoryStatus", lastCategoryStatus);
			model.put("selectedCategoryPerPage", selectedCategoryPerPage);

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("context", context.getContextPath());
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "allCategory";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/category/save", method = RequestMethod.POST)
	public String saveCategory(Category category, @RequestParam String oldId,
			ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			if (oldId.equals("new")) {
				try {
					categoryService.saveCategory(category);
					PrimaryTag pTag = new PrimaryTag();
					pTag.setPrimaryTagName(category.getCategoryName());
					tagService.savePrimaryTag(pTag);
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/category/all/10/0?i=e";
				}

				model.put("Admin", session.getAttribute("Admin"));
				model.put("Publisher", session.getAttribute("Publisher"));

				return "redirect:" + "/page/category/all/10/0?i=s";
			} else {

				try {
					categoryService.updateCategory(category, oldId);
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/category/all/10/0?i=e";
				}

				model.put("Admin", session.getAttribute("Admin"));
				model.put("Publisher", session.getAttribute("Publisher"));

				return "redirect:" + "/page/category/all/10/0?i=s";
			}
		} else {

			return "redirect:" + "/page/index";
		}

	}
	
	@RequestMapping(value = "/category/delete", method = RequestMethod.POST)
	public String deleteCategory(Category category, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {
            int start = (Integer) session.getAttribute("lastCategoryStatus");
			try {
				//System.out.println("country.getCountryId():" +country.getCountryId());
				Category cat = categoryService.returnCategoryById(Integer.toString(category.getCategoryId()));
				categoryService.deleteCategory(cat);
				String[] tagsArray = {cat.getCategoryName()};
				PrimaryTag  pTag = tagService.getSpecificPrimaryTags(tagsArray).get(0);
				tagService.deletePrimaryTag(pTag);
				
			} catch (Exception e) {
				e.printStackTrace();
				return "redirect:" + "/page/category/all/10/" + start + "?i=e";
			}

			return "redirect:" + "/page/category/all/10/" + start + "?i=s";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/tag/{id}", method = RequestMethod.GET)
	public String showTag(@PathVariable String id, Tag tag,
			final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			Tag tg = null;
			if (id == null || id.equalsIgnoreCase("new")) {
				model.put("tag", tag);
				model.put("id", id);
			} else if (!id.equals("new")) {
				model.put("id", id);

				tg = tagService.returnTagById(id);

				if (tg != null) {
					model.put("tag", tg);
				}
			}

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "tag";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/tag/all/{tagPerPage}/{startTagNo}", method = RequestMethod.GET)
	public String showAllTag(@PathVariable String tagPerPage,
			@PathVariable String startTagNo, @RequestParam String i,
			final ModelMap model, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int selectedTagPerPage = Integer.parseInt(tagPerPage);
			session.setAttribute("selectedTagPerPage", selectedTagPerPage);
			int startTag = Integer.parseInt(startTagNo);
			session.setAttribute("lastTagStatus", startTag);

			List<Tag> tags = tagService.returnAllTag(startTag,
					selectedTagPerPage);
			int totalTag = tagService.returnNumberOfTag();

			int lastTagStatus = startTag;

			model.put("tags", tags);
			float totalPage = (float) totalTag / selectedTagPerPage;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastTagStatus", lastTagStatus);
			model.put("selectedTagPerPage", selectedTagPerPage);

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("context", context.getContextPath());
			model.put("username", session.getAttribute("username"));

			if (i.equals("e")) {
				model.put("indicator", "error");
			} else if (i.equals("s")) {
				model.put("indicator", "success");
			} else {
				model.put("indicator", "");
			}
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "allTag";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/tag/alll/{pageNo}", method = RequestMethod.GET)
	public String showAllTag(@PathVariable String pageNo, final ModelMap model,
			HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int lastTagStatus = 0;
			int pageNumber = Integer.parseInt(pageNo);
			int selectedTagPerPage = (Integer) session
					.getAttribute("selectedTagPerPage");
			int first = (pageNumber * selectedTagPerPage)
					- (selectedTagPerPage - 1);
			first--;

			List<Tag> tags = tagService.returnAllTag(first, selectedTagPerPage);
			Integer totalTag = tagService.returnNumberOfTag();

			lastTagStatus = first;

			model.put("tags", tags);
			float totalPage = (float) totalTag / 10;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastTagStatus", lastTagStatus);
			model.put("selectedTagPerPage", selectedTagPerPage);

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("context", context.getContextPath());
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "allTag";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/tag/save", method = RequestMethod.POST)
	public String saveTag(Tag tag, @RequestParam String oldId, ModelMap model,
			HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			if (oldId.equals("new")) {

				try {
					tagService.saveTag(tag);
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/tag/all/10/0?i=e";
				}

				

				return "redirect:" + "/page/tag/all/10/0?i=s";
			} else {

				try {
					tagService.updateTag(tag, oldId);
					;
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/tag/all/10/0?i=e";
				}


				return "redirect:" + "/page/tag/all/10/0?i=s";
			}
		} else {

			return "redirect:" + "/page/index";
		}

	}
	
	@RequestMapping(value = "/tag/delete", method = RequestMethod.POST)
	public String deleteTag(Tag tag, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {
            int start = (Integer) session.getAttribute("lastTagStatus");
			try {
				//System.out.println("country.getCountryId():" +country.getCountryId());
				Tag tg = tagService.returnTagById(Integer.toString(tag.getTagId()));
				tagService.deleteTag(tg);
				
			} catch (Exception e) {
				e.printStackTrace();
				return "redirect:" + "/page/tag/all/10/" + start + "?i=e";
			}

			return "redirect:" + "/page/tag/all/10/" + start + "?i=s";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/server/{id}", method = RequestMethod.GET)
	public String showServer(@PathVariable String id, Server server,
			final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			Server sv = null;
			if (id == null || id.equalsIgnoreCase("new")) {
				model.put("server", server);
				model.put("id", id);
			} else if (!id.equals("new")) {
				model.put("id", id);

				sv = serverService.returnServerById(id);

				if (sv != null) {
					model.put("server", sv);
					model.put("serverType", sv.getServerType());
				}
			}

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "server";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/server/all/{pageNumber}", method = RequestMethod.GET)
	public String showAllServer(@PathVariable String pageNumber,
			@RequestParam String i, final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			Session dbsession = sessionFactory.openSession();
			Transaction tx = null;
			int serverPerPage = 10;
			List<Server> servers = serverService.returnAllServer(pageNumber,
					serverPerPage);
			Integer totalServer = serverService.returnNumberOfServer();

			session.setAttribute("lastServerStatus", Integer.parseInt(pageNumber));
			
			model.put("servers", servers);
			float totalPage = (float) totalServer / 10;
			model.put("totalServer", Math.ceil(totalPage));

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));

			if (i.equals("e")) {
				model.put("indicator", "error");
			} else if (i.equals("s")) {
				model.put("indicator", "success");
			} else {
				model.put("indicator", "");
			}
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "allServer";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/server/save", method = RequestMethod.POST)
	public String saveServer(Server server, @RequestParam String oldId,
			ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			if (oldId.equals("new")) {

				try {
					server.setCpuUsage(100);
					server.setRamUsage(100);
					server.setTotalNumberOfStream(0);
					server.setBandwidth(0);
					serverService.saveServer(server);
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/server/all/1?i=e";
				}

				model.put("Admin", session.getAttribute("Admin"));
				model.put("Publisher", session.getAttribute("Publisher"));

				return "redirect:" + "/page/server/all/1?i=s";
			} else {

				try {
					Server prevServer = serverService.returnServerById(oldId);
					server.setCpuUsage(prevServer.getCpuUsage());
					server.setRamUsage(prevServer.getRamUsage());
					server.setTotalNumberOfStream(prevServer
							.getTotalNumberOfStream());
					server.setBandwidth(prevServer.getBandwidth());
					serverService.updateServer(server, oldId);
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/server/all/1?i=e";
				}

				model.put("Admin", session.getAttribute("Admin"));
				model.put("Publisher", session.getAttribute("Publisher"));

				return "redirect:" + "/page/server/all/1?i=s";
			}
		} else {

			return "redirect:" + "/page/index";
		}

	}
	
	@RequestMapping(value = "/server/delete", method = RequestMethod.POST)
	public String deleteServer(Server server, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {
            int start = (Integer) session.getAttribute("lastServerStatus");
			try {
				//System.out.println("country.getCountryId():" +country.getCountryId());
				Server srv = serverService.returnServerById(Integer.toString(server.getServerId()));
				serverService.deleteServer(srv);
				
			} catch (Exception e) {
				e.printStackTrace();
				return "redirect:" + "/page/server/all/" + start + "?i=e";
			}

			return "redirect:" + "/page/server/all/" + start + "?i=s";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/channel/{id}", method = RequestMethod.GET)
	public String showChannel(@PathVariable String id, LiveChannel channel,
			final ModelMap model, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {

			Map<Integer, String> categoryMap = new HashMap<Integer, String>();
			List categories = categoryService.returnAllCategory();
			for (Iterator iterator = categories.iterator(); iterator.hasNext();) {
				Category category = (Category) iterator.next();
				categoryMap.put(category.getCategoryId(),
						category.getCategoryName());
			}
			Map<Integer, String> countryMap = new HashMap<Integer, String>();

			List countries = countryService.returnAllCountry();
			for (Iterator iterator = countries.iterator(); iterator.hasNext();) {
				Country country = (Country) iterator.next();
				countryMap.put(country.getCountryId(), country.getCountryName());
			}
			Map<Integer, String> languageMap = new HashMap<Integer, String>();
			List languages = languageService.returnAllLanguage();
			for (Iterator iterator = languages.iterator(); iterator.hasNext();) {
				Language language = (Language) iterator.next();
				languageMap.put(language.getLanguageId(),
						language.getLanguageName());
			}
			Map<Integer, String> tagMap = new HashMap<Integer, String>();
			tagMap = tagService.getTagMap();

			model.put("categoryMap", categoryMap);
			model.put("countryMap", countryMap);
			model.put("languageMap", languageMap);
			model.put("tagMap", tagMap);

			if (id == null || id.equalsIgnoreCase("new")) {
				model.put("channel", channel);
				model.put("id", id);
			} else if (!id.equals("new")) {
				model.put("id", id);
				LiveChannel ch = liveChannelService.returnLiveChannelById(id);
				if (ch != null) {
					model.put("channel", ch);

					model.put("countryName", ch.getCountry().getCountryName());
					model.put("countryId", ch.getCountry().getCountryId());
					model.put("categoryName", ch.getCategory()
							.getCategoryName());
					model.put("categoryId", ch.getCategory().getCategoryId());
					model.put("languageName", ch.getLanguage()
							.getLanguageName());
					model.put("languageId", ch.getLanguage().getLanguageId());
				}
			}

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);
			return "channel";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/channel/all/{channelPerPage}/{startChannelNo}", method = RequestMethod.GET)
	public String showAllChannel(@PathVariable String channelPerPage,
			@PathVariable String startChannelNo, @RequestParam String i,
			final ModelMap model, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int selectedChannelPerPage = Integer.parseInt(channelPerPage);
			session.setAttribute("selectedChannelPerPage",
					selectedChannelPerPage);
			int startChannel = Integer.parseInt(startChannelNo);
            session.setAttribute("lastChannelStatus", startChannel);

			List<LiveChannel> channels = liveChannelService
					.returnAllLiveChannel(startChannel, selectedChannelPerPage);
			int totalChannel = liveChannelService.returnNumberOfLiveChannel();

			int lastChannelStatus = startChannel;

			model.put("channels", channels);
			float totalPage = (float) totalChannel / selectedChannelPerPage;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastChannelStatus", lastChannelStatus);
			model.put("selectedChannelPerPage", selectedChannelPerPage);
			model.put("show", "all");

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("context", context.getContextPath());

			model.put("username", session.getAttribute("username"));
			if (i.equals("e")) {
				model.put("indicator", "error");
			} else if (i.equals("s")) {
				model.put("indicator", "success");
			} else {
				model.put("indicator", "");
			}
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "allChannel";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/channel/alll/{pageNo}", method = RequestMethod.GET)
	public String showAllChannel(@PathVariable String pageNo,
			final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int lastChannelStatus = 0;
			int pageNumber = Integer.parseInt(pageNo);
			int selectedChannelPerPage = (Integer) session
					.getAttribute("selectedChannelPerPage");
			int first = (pageNumber * selectedChannelPerPage)
					- (selectedChannelPerPage - 1);
			first--;

			lastChannelStatus = first;

			List<LiveChannel> channels = liveChannelService
					.returnAllLiveChannel(first, selectedChannelPerPage);
			Integer totalChannel = liveChannelService
					.returnNumberOfLiveChannel();

			model.put("channels", channels);
			float totalPage = (float) totalChannel / 10;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastChannelStatus", lastChannelStatus);
			model.put("selectedChannelPerPage", selectedChannelPerPage);
			model.put("show", "all");
			model.put("context", context.getContextPath());

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);
			
			return "allChannel";
		} else {

			return "redirect:" + "/page/index";
		}

	}
	
	@RequestMapping(value = "/ownchannel/all/{channelPerPage}/{startChannelNo}", method = RequestMethod.GET)
	public String showOwnAllChannel(@PathVariable String channelPerPage,
			@PathVariable String startChannelNo, @RequestParam String i,
			final ModelMap model, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int selectedChannelPerPage = Integer.parseInt(channelPerPage);
			session.setAttribute("selectedChannelPerPage",
					selectedChannelPerPage);
			int startChannel = Integer.parseInt(startChannelNo);
            session.setAttribute("lastChannelStatus", startChannel);

			List<LiveChannel> channels = liveChannelService
					.returnAllLiveChannelByUserId(startChannel, selectedChannelPerPage,Integer.parseInt(""
							+ session.getAttribute("userId")));
			int totalChannel = liveChannelService
					.returnNumberOfLiveChannelByUserId(Integer.parseInt(""
							+ session.getAttribute("userId")));

			int lastChannelStatus = startChannel;

			model.put("channels", channels);
			float totalPage = (float) totalChannel / selectedChannelPerPage;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastChannelStatus", lastChannelStatus);
			model.put("selectedChannelPerPage", selectedChannelPerPage);
			model.put("show", "own");

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("context", context.getContextPath());

			model.put("username", session.getAttribute("username"));
			if (i.equals("e")) {
				model.put("indicator", "error");
			} else if (i.equals("s")) {
				model.put("indicator", "success");
			} else {
				model.put("indicator", "");
			}
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "allChannel";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/ownchannel/alll/{pageNo}", method = RequestMethod.GET)
	public String showAllOwnChannel(@PathVariable String pageNo,
			final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int lastChannelStatus = 0;
			int pageNumber = Integer.parseInt(pageNo);
			int selectedChannelPerPage = 10;
			int first = (pageNumber * selectedChannelPerPage)
					- (selectedChannelPerPage - 1);
			first--;

			lastChannelStatus = first;
			
			session.setAttribute("lastChannelStatus", lastChannelStatus);

			List<LiveChannel> channels = liveChannelService
					.returnAllLiveChannelByUserId(
							first,
							selectedChannelPerPage,
							Integer.parseInt(""
									+ session.getAttribute("userId")));
			Integer totalChannel = liveChannelService
					.returnNumberOfLiveChannelByUserId(Integer.parseInt(""
							+ session.getAttribute("userId")));

			model.put("channels", channels);
			float totalPage = (float) totalChannel / 10;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastChannelStatus", lastChannelStatus);
			model.put("selectedChannelPerPage", selectedChannelPerPage);
			model.put("show", "own");
			model.put("context", context.getContextPath());

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);
			
			return "allChannel";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/channellog/{channelId}", method = RequestMethod.GET)
	public String showChannelLog(@PathVariable String channelId,
			final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {

			List<ChannelLog> channelLogs = channelLogService
					.returnChannelLogByChannelId(Integer.parseInt(channelId));
			LiveChannel channel = liveChannelService
					.returnLiveChannelById(channelId);

			model.put("logs", channelLogs);
			model.put("channelName", channel.getChannelName());
			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);
			
			return "channelLogs";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/channelPlay/{channelId}", method = RequestMethod.GET)
	public String channelPlay(@PathVariable String channelId,
			final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {

			LiveChannel channel = liveChannelService
					.returnLiveChannelById(channelId);
			ChannelLink channelLink = channelLinkService
					.returnChannelLinkByChannelId(Integer.parseInt(channelId));

			model.put("channel", channel);
			model.put("channelLink", channelLink);
			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);
			
			return "channelPlay";
		} else {

			LiveChannel channel = liveChannelService
					.returnLiveChannelById(channelId);
			ChannelLink channelLink = channelLinkService
					.returnChannelLinkByChannelId(Integer.parseInt(channelId));

			model.put("channel", channel);
			model.put("channelLink", channelLink);
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);
			
			return "channelPlay";
		}

	}

	@RequestMapping(value = "/channelMonitor", method = RequestMethod.GET)
	public String channelMonitor(final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {

			List<LiveChannel> upChannelList = liveChannelService
					.returnUpChannelsByUserId(Integer.parseInt(""
							+ session.getAttribute("userId")));
			List<LiveChannel> downChannelList = liveChannelService
					.returnDownChannelsByUserId(Integer.parseInt(""
							+ session.getAttribute("userId")));

			model.put("upChannels", upChannelList);
			model.put("downChannels", downChannelList);

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			return "channelMonitor";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/channel/save", method = RequestMethod.POST)
	public String saveChannel(LiveChannel channel, @RequestParam String oldId,
			ModelMap model, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {
			/**
			 * File upload code. Here icon for channel is uploaded and file is
			 * renamed with unique name.
			 * **/
			if (oldId.equals("new")) {
				
				String newChannelName = channel.getChannelName().replaceAll("\\s", "");
				channel.setChannelName(newChannelName);
				String originalFileName = channel.getFile()
						.getOriginalFilename();
				int firstIndex = originalFileName.lastIndexOf(".");
				String fileExtension = originalFileName.substring(firstIndex,
						originalFileName.length());
				long newFileName = System.currentTimeMillis();
				String rootPath = System.getProperty("catalina.base")
						+ "/webapps/images";
				String path = rootPath + "/" + newFileName + fileExtension;
				System.out.println(rootPath);
				File uploadedFile = new File(path);
				if (!uploadedFile.exists())
					try {
						uploadedFile.getParentFile().mkdirs();
						uploadedFile.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				try {
					byte[] bytes = channel.getFile().getBytes();
					BufferedOutputStream stream = new BufferedOutputStream(
							new FileOutputStream(path));
					stream.write(bytes);
					stream.close();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(path);
				/**
				 * File upload code ends here.
				 * **/

				/**
				 * 
				 * Here is the code for getting least cpu usage transcoder and
				 * streamer server IP
				 */

				List<Server> transcoderServerList = new ArrayList<Server>();
				List<Server> streamerServerList = new ArrayList<Server>();

				transcoderServerList = serverService
						.returnAllTranscoderServerOrderByIdleCpu();
				streamerServerList = serverService
						.returnAllStreamerServerOrderByTotalNumberOfStream();
				Server transcoderServer = null;
				Server streamerServer = null;

				if (transcoderServerList.size() > 0) {
					for (Server server : transcoderServerList) {
						if (server.getCpuUsage() > 20) {
							transcoderServer = server;
							break;
						}
					}
				}
				if (streamerServerList.size() > 0) {
					 streamerServer = streamerServerList.get(0);
				}

				if (transcoderServer == null || streamerServer == null) {
					return "redirect:" + "/page/channel/all/10/0?i=e";
				}

				String transcoderIp = "";
				String streamerIp = "";
				String streamingServersEdgeApplication = "";
				int totalStream = 0;

				if (transcoderServer != null) {
					transcoderIp = transcoderServer.getPrivateIp();
				}
				if (streamerServer != null) {
					streamerIp = streamerServer.getPrivateIp();
					streamingServersEdgeApplication = streamerServer.getEdge();
					totalStream = streamerServer.getTotalNumberOfStream();
				}

				System.out.println(streamerIp);
				System.out.println(transcoderIp);

				/**
				 * 
				 * ffmpeg command for getting video heights
				 */

				FFMPEGCommands ffmpegCommands = new FFMPEGCommands();
				//
				// //int videoHeight =
				// ffmpegCommands.getSourceHeight("/home/shams/bin/ffprobe -i "+channel.getLink());
				//
				int videoHeight = 0;
				
				try {
					videoHeight = Integer
							.parseInt(ffmpegCommands
									.getSourceHeight("/home/support/bin/ffprobe -v error -show_streams "
											+ channel.getLink()
											+ " | grep height"));
				} catch (Exception e) {
					e.printStackTrace();
				}

				String cmd = "";
				int numberOfStream = 4;

				/*				 *//**
				 * ChannelLinks's data stored in database here.
				 * **/
				/*
				 * 
				 * ChannelLink channelLink = new ChannelLink();
				 * channelLink.setChannelId(channel.getChannelId());
				 */
				/**
				 * command to execute ffmpeg channel publish
				 */
				long key = System.currentTimeMillis();
				String userName = "" + session.getAttribute("username");
				String channelName = channel.getChannelName();
				
				String publishName = userName+channelName+key;
				
				channel.setPublishName(publishName);
				
				if (videoHeight >= 500) {
					cmd = "ssh support@"
							+ transcoderIp
							+ " /home/support/bin/ffmpeg -i "
							+ channel.getLink()
							+ " -c:v copy -c:a libfdk_aac -f flv rtmp://"
							+ streamerIp
							+ ":1935/"
							+ streamerServer.getServerName()
							+ "/"
							+ publishName
							+ "_720"
							+ " -vf scale=640:480 -minrate 800k -maxrate 800k -bufsize 800k -c:v h264 -ab 64k -ar 44100 -c:a libfdk_aac -f flv rtmp://"
							+ streamerIp
							+ ":1935/"
							+ streamerServer.getServerName()
							+ "/"
							+ publishName
							+ "_480 -vf scale=480:360 -minrate 400k -maxrate 400k -bufsize 400k -c:v h264 -ab 32k -ar 22050 -c:a libfdk_aac -f flv rtmp://"
							+ streamerIp + ":1935/"
							+ streamerServer.getServerName() + "/" + publishName + "_360 -vf scale=240:180 -minrate 200k -maxrate 200k -bufsize 200k -c:v h264 -ab 16k -ar 11025 -c:a libfdk_aac -f flv rtmp://"
							+ streamerIp + ":1935/"
							+ streamerServer.getServerName() + "/" + publishName + "_180 &";
					numberOfStream = 4;
				} else if (videoHeight < 500 && videoHeight >= 420) {
					cmd = "ssh support@"
							+ transcoderIp
							+ " /home/support/bin/ffmpeg -i "
							+ channel.getLink()
							+ " -c:v copy -c:a libfdk_aac -f flv rtmp://"
							+ streamerIp
							+ ":1935/"
							+ streamerServer.getServerName()
							+ "/"
							+ publishName
							+ "_480"
							+ " -vf scale=480:360 -minrate 400k -maxrate 400k -bufsize 400k -c:v h264 -ab 32k -ar 22050 -c:a libfdk_aac -f flv rtmp://"
							+ streamerIp + ":1935/"
							+ streamerServer.getServerName() + "/" + publishName+ "_360 -vf scale=240:180 -minrate 200k -maxrate 200k -bufsize 200k -c:v h264 -ab 16k -ar 11025 -c:a libfdk_aac -f flv rtmp://"
							+ streamerIp + ":1935/"
							+ streamerServer.getServerName() + "/" + publishName + "_180 &";
					numberOfStream = 3;
				} else if (videoHeight < 420 && videoHeight >= 270) {
					cmd = "ssh support@" + transcoderIp
							+ " /home/support/bin/ffmpeg -i "
							+ channel.getLink()
							+ " -c:v copy -c:a libfdk_aac -f flv rtmp://"
							+ streamerIp + ":1935/"
							+ streamerServer.getServerName() + "/" + publishName + "_360 -vf scale=240:180 -minrate 200k -maxrate 200k -bufsize 200k -c:v h264 -ab 16k -ar 11025 -c:a libfdk_aac -f flv rtmp://"
							+ streamerIp + ":1935/"
							+ streamerServer.getServerName() + "/" + publishName + "_180 &";
					numberOfStream = 2;
				} else if (videoHeight < 270 && videoHeight > 0) {
					cmd = "ssh support@" + transcoderIp
							+ " /home/support/bin/ffmpeg -i "
							+ channel.getLink()
							+ " -c:v copy -c:a libfdk_aac -f flv rtmp://"
							+ streamerIp + ":1935/"
							+ streamerServer.getServerName() + "/" + publishName + "_180 &";
					numberOfStream = 1;
				}else {
					return "redirect:" + "/page/channel/all/10/0?i=e";
				}
				System.out.println(cmd);

				// try{
				// Socket socket = new
				// Socket(InetAddress.getByName(transcoderIp), 5000);
				// PrintWriter out = new
				// PrintWriter(socket.getOutputStream(),true);
				// out.println(cmd);
				// socket.close();
				// out.close();
				// }catch(Exception e){
				// e.printStackTrace();
				// }

				commandService.execute(cmd);

				System.out.println("command executed..");

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				/**
				 * command for grabbing idle cpu and memory
				 */

				String command2 = "ssh -tt support@" + transcoderIp
						+ " TERM=vt100 ./getinfo";
				String[] cpuAndMemory = commandService
						.getCpuAndMemory(command2);

				// String command2 = "getcpumemory";
				// String [] cpuAndMemory = null;
				// try{
				// System.out.println("Transcoder Ip : "+transcoderIp);
				// Socket socket = new
				// Socket(InetAddress.getByName(transcoderIp), 5000);
				//
				// PrintWriter out = new
				// PrintWriter(socket.getOutputStream(),true);
				// out.println(command2);
				//
				// BufferedReader reader = new BufferedReader(new
				// InputStreamReader(
				// socket.getInputStream()));
				// StringBuffer result = new StringBuffer();
				// String line = "";
				// while ((line = reader.readLine()) != null) {
				// result.append(line + "\n");
				// }
				// System.out.println("Response from server : cpu and memory : "
				// + result.toString());
				// cpuAndMemory = result.toString().split(",");
				// socket.close();
				// out.close();
				// }catch(Exception e){
				// e.printStackTrace();
				// }
				// System.out.println("PGREP ................:    "+command2);
				System.out.println("Streamer :.................." + streamerIp);
				System.out.println("transcoder :.................."
						+ transcoderIp);
				System.out.println("command : ------    " + cmd);

				double idleCpu = 0;
				double idleMemory = 0;

				try {
					if (cpuAndMemory.length == 2) {
						idleCpu = Double.parseDouble(cpuAndMemory[0]);
						idleMemory = Double.parseDouble(cpuAndMemory[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				/**
				 * get country data
				 * 
				 */
				Country country = countryService.returnCountryById(""
						+ channel.getCountryId());
				// set country to channel
				channel.setCountry(country);
				String countryParameter = country.getCountryName();

				/**
				 * get category data
				 * 
				 */
				Category category = categoryService.returnCategoryById(""
						+ channel.getCategoryId());
				// set category to channel
				channel.setCategory(category);
				String categoryParameter = category.getCategoryName();
				/**
				 * get language data
				 * 
				 */
				Language language = languageService.returnLanguageById(""
						+ channel.getLanguageId());
				// set language to channel
				channel.setLanguage(language);
				String languageParameter = language.getLanguageName();

				/**
				 * primary tag and secondary tag insertion to database
				 */
				try {
					Set<PrimaryTag> primaryTagSet = new HashSet<PrimaryTag>();
					String[] primaryTagsArray = {countryParameter, categoryParameter, languageParameter};
					List<PrimaryTag> primaryTagList = tagService.getSpecificPrimaryTags(primaryTagsArray);
					for (PrimaryTag primaryTag : primaryTagList) {
						primaryTagSet.add(primaryTag);
					}
					channel.setPrimaryTagSet(primaryTagSet);

					String tagString = channel.getTag() + "," + channel.getChannelName();
					String[] tagsArray = tagString.split(",");
					String filteredTagString = tagService.getFilteredTagsName(tagsArray);
					if (filteredTagString.length() > 0) {
						String[] filteredTagArray = filteredTagString.split(",");
						for (String tagName : filteredTagArray) {
							Tag tag = new Tag();
							tag.setTagName(tagName);
							tagService.saveTag(tag);
						}
					}

					List<Tag> tagList = tagService.getSpecificTags(tagsArray);
					Set<Tag> tagSet = new HashSet<Tag>();
					String tagNames = "";
					for (Tag tag : tagList) {
						if (!tag.getTagName().equalsIgnoreCase(channel.getChannelName())) {
							tagNames = tagNames + tag.getTagName() + ",";
						}
						tagSet.add(tag);
					}
					channel.setTagSet(tagSet);
					
					int last = tagNames.lastIndexOf(",");
					String finalTagNames = tagNames.substring(0, last);
					channel.setTag(finalTagNames);

				} catch (Exception ex) {
					ex.printStackTrace();
				}

				/**
				 * get user data
				 * 
				 */
				User user = userService.returnUserById(""
						+ session.getAttribute("userId"));

				/**
				 * Channel's data stored in database here.
				 * **/

				channel.setStreamerIp(streamerIp);
				channel.setTranscoderIp(transcoderIp);
				channel.setCommand(cmd);

				int liveChannelId = 0;

				if (numberOfStream == 4) {
					try {
						if (idleCpu > 0) {
							serverService.updateServerMemoryCpu(
									transcoderServer.getServerId(), idleCpu,
									idleMemory);
						}
						serverService.updateServerTotalStream(
								streamerServer.getServerId(), totalStream + 4);
						channel.setNumberOfStream(4);
						liveChannelId = liveChannelService.saveLiveChannel(
								channel, newFileName + fileExtension, user,
								"http://" + streamerIp + ":1935/"
										+ streamerServer.getServerName() + "/"
										+ publishName
										+ "_720/playlist.m3u8", "http://"
										+ streamerIp + ":1935/"
										+ streamerServer.getServerName() + "/"
										+ publishName
										+ "_480/playlist.m3u8", "http://"
										+ streamerIp + ":1935/"
										+ streamerServer.getServerName() + "/"
										+ publishName
										+ "_360/playlist.m3u8","http://"
												+ streamerIp + ":1935/"
												+ streamerServer.getServerName() + "/"
												+ publishName
												+ "_180/playlist.m3u8", "userlink",
								"up");
					} catch (Exception e) {
						e.printStackTrace();
						String command1 = "ssh support@"
								+ transcoderIp
								+ " \"ps -ef | grep "
								+ userName
								+ channelName
								+ key
								+ "  | grep -v grep | awk '{print $2}' | xargs kill -INT\"";
						System.out.println(command1);
						// try{
						// Socket socket = new
						// Socket(InetAddress.getByName(transcoderIp), 5000);
						// PrintWriter out = new
						// PrintWriter(socket.getOutputStream(),true);
						// out.println(command1);
						// socket.close();
						// out.close();
						// }catch(Exception e1){
						// e1.printStackTrace();
						// }

						commandService.execute(command1);

						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
				} else if (numberOfStream == 3) {
					try {
						if (idleCpu > 0) {
							serverService.updateServerMemoryCpu(
									transcoderServer.getServerId(), idleCpu,
									idleMemory);
						}
						serverService.updateServerTotalStream(
								streamerServer.getServerId(), totalStream + 3);
						channel.setNumberOfStream(3);
						liveChannelId = liveChannelService.saveLiveChannel(
								channel, newFileName + fileExtension, user, "",
								"http://" + streamerIp + ":1935/"
										+ streamerServer.getServerName() + "/"
										+ publishName
										+ "_480/playlist.m3u8", "http://"
										+ streamerIp + ":1935/"
										+ streamerServer.getServerName() + "/"
										+ publishName
										+ "_360/playlist.m3u8","http://"
												+ streamerIp + ":1935/"
												+ streamerServer.getServerName() + "/"
												+ publishName
												+ "_180/playlist.m3u8", "userlink",
								"up");
					} catch (Exception e) {
						e.printStackTrace();
						String command1 = "ssh support@"
								+ transcoderIp
								+ " \"ps -ef | grep "
								+ userName
								+ channelName
								+ key
								+ "  | grep -v grep | awk '{print $2}' | xargs kill -INT\"";
						System.out.println(command1);
						// try{
						// Socket socket = new
						// Socket(InetAddress.getByName(transcoderIp), 5000);
						// PrintWriter out = new
						// PrintWriter(socket.getOutputStream(),true);
						// out.println(command1);
						// socket.close();
						// out.close();
						// }catch(Exception e1){
						// e1.printStackTrace();
						// }

						commandService.execute(command1);

						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
				} else if (numberOfStream == 2) {
					try {
						if (idleCpu > 0) {
							serverService.updateServerMemoryCpu(
									transcoderServer.getServerId(), idleCpu,
									idleMemory);
						}
						serverService.updateServerTotalStream(
								streamerServer.getServerId(), totalStream + 2);
						channel.setNumberOfStream(2);
						liveChannelId = liveChannelService.saveLiveChannel(
								channel, newFileName + fileExtension, user, "",
								"", "http://" + streamerIp + ":1935/"
										+ streamerServer.getServerName() + "/"
										+ publishName
										+ "_360/playlist.m3u8","http://"
												+ streamerIp + ":1935/"
												+ streamerServer.getServerName() + "/"
												+ publishName
												+ "_180/playlist.m3u8", "userlink",
								"up");
					} catch (Exception e) {
						e.printStackTrace();
						String command1 = "ssh support@"
								+ transcoderIp
								+ " \"ps -ef | grep "
								+ userName
								+ channelName
								+ key
								+ "  | grep -v grep | awk '{print $2}' | xargs kill -INT\"";
						System.out.println(command1);
						// try{
						// Socket socket = new
						// Socket(InetAddress.getByName(transcoderIp), 5000);
						// PrintWriter out = new
						// PrintWriter(socket.getOutputStream(),true);
						// out.println(command1);
						// socket.close();
						// out.close();
						// }catch(Exception e1){
						// e1.printStackTrace();
						// }

						commandService.execute(command1);

						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
				}else if (numberOfStream == 1) {
					try {
						if (idleCpu > 0) {
							serverService.updateServerMemoryCpu(
									transcoderServer.getServerId(), idleCpu,
									idleMemory);
						}
						serverService.updateServerTotalStream(
								streamerServer.getServerId(), totalStream + 1);
						channel.setNumberOfStream(1);
						liveChannelId = liveChannelService.saveLiveChannel(
								channel, newFileName + fileExtension, user, "",
								"","", "http://" + streamerIp + ":1935/"
										+ streamerServer.getServerName() + "/"
										+ publishName
										+ "_180/playlist.m3u8", "userlink",
								"up");
					} catch (Exception e) {
						e.printStackTrace();
						String command1 = "ssh support@"
								+ transcoderIp
								+ " \"ps -ef | grep "
								+ userName
								+ channelName
								+ key
								+ "  | grep -v grep | awk '{print $2}' | xargs kill -INT\"";
						System.out.println(command1);
						// try{
						// Socket socket = new
						// Socket(InetAddress.getByName(transcoderIp), 5000);
						// PrintWriter out = new
						// PrintWriter(socket.getOutputStream(),true);
						// out.println(command1);
						// socket.close();
						// out.close();
						// }catch(Exception e1){
						// e1.printStackTrace();
						// }

						commandService.execute(command1);

						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
				}

				/**
				 * Log for channel log table that channel gone up
				 */
				if (liveChannelId > 0) {
					ChannelLog channelLog = new ChannelLog();
					channelLog.setChannelId(liveChannelId);
					channelLog.setDate(new Date());
					channelLog.setLog("Channel state gone up..");
					try {
						channelLogService.saveChannelLog(channelLog);
					} catch (Exception e) {
						e.printStackTrace();
						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
				}

				ChannelLink channelLink = new ChannelLink();
				if (numberOfStream == 4) {
					try {
						channelLinkService.saveChannelLink(channelLink,
								channel.getChannelId(), "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_720/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_480/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_360/playlist.m3u8","http://"
												+ streamingServersEdgeApplication
												+ ".livestreamer.com:1935/"
												+ streamingServersEdgeApplication + "/"
												+ publishName
												+ "_180/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_720/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_480/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_360/playlist.m3u8","http://"
												+ streamingServersEdgeApplication
												+ ".livestreamer.com:1935/"
												+ streamingServersEdgeApplication + "/"
												+ publishName
												+ "_180/playlist.m3u8");
					} catch (Exception e) {
						e.printStackTrace();
						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
					
					liveChannelService.createPlayList(channel.getChannelId()
							+ "_" + channelName, "http://"
							+ streamingServersEdgeApplication
							+ ".livestreamer.com:1935/"
							+ streamingServersEdgeApplication + "/" + publishName + "_720/chunklist.m3u8",
							"http://" + streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ publishName
									+ "_480/chunklist.m3u8", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ publishName
									+ "_360/chunklist.m3u8", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ publishName
									+ "_180/chunklist.m3u8");

				} else if (numberOfStream == 3) {
					try {
						channelLinkService.saveChannelLink(channelLink,
								channel.getChannelId(), "", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_480/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_360/playlist.m3u8","http://"
												+ streamingServersEdgeApplication
												+ ".livestreamer.com:1935/"
												+ streamingServersEdgeApplication + "/"
												+ publishName
												+ "_180/playlist.m3u8", "",
								"http://" + streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_480/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_360/playlist.m3u8","http://"
												+ streamingServersEdgeApplication
												+ ".livestreamer.com:1935/"
												+ streamingServersEdgeApplication + "/"
												+ publishName
												+ "_180/playlist.m3u8");
					} catch (Exception e) {
						e.printStackTrace();
						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
					
					liveChannelService.createPlayList(channel.getChannelId()
							+ "_" + channelName, "",
							"http://" + streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ publishName
									+ "_480/chunklist.m3u8", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ publishName
									+ "_360/chunklist.m3u8", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ publishName
									+ "_180/chunklist.m3u8");
					
				} else if (numberOfStream == 2) {
					try {
						channelLinkService.saveChannelLink(channelLink,
								channel.getChannelId(), "", "", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_360/playlist.m3u8","http://"
												+ streamingServersEdgeApplication
												+ ".livestreamer.com:1935/"
												+ streamingServersEdgeApplication + "/"
												+ publishName
												+ "_180/playlist.m3u8", "", "",
								"http://" + streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_360/playlist.m3u8","http://" + streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_180/playlist.m3u8");
					} catch (Exception e) {
						e.printStackTrace();
						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
					
					liveChannelService.createPlayList(channel.getChannelId()
							+ "_" + channelName, "",
							"", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ publishName
									+ "_360/chunklist.m3u8", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ publishName
									+ "_180/chunklist.m3u8");
					
				}else if (numberOfStream == 1) {
					try {
						channelLinkService.saveChannelLink(channelLink,
								channel.getChannelId(), "", "","", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_180/playlist.m3u8", "", "","",
								"http://" + streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ publishName
										+ "_180/playlist.m3u8");
					} catch (Exception e) {
						e.printStackTrace();
						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
					
					liveChannelService.createPlayList(channel.getChannelId()
							+ "_" + channelName, "",
							"", "", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ publishName
									+ "_180/chunklist.m3u8");
					
				}

				model.put("Admin", session.getAttribute("Admin"));
				model.put("Publisher", session.getAttribute("Publisher"));

				return "redirect:" + "/page/channel/all/10/0?i=s";
			} else {
				
				String newChannelName = channel.getChannelName().replaceAll("\\s", "");
				channel.setChannelName(newChannelName);
				
				String publishName = channel.getPublishName();

				/**
				 * 
				 * Here is the code for getting least cpu usage transcoder and
				 * streamer server IP
				 */
				int localPort = 0;
				List<Server> transcoderServerList = new ArrayList<Server>();
				List<Server> streamerServerList = new ArrayList<Server>();

				

				Server transcoderServer = null;
				Server streamerServer = null;

				

				String transcoderIp = "";
				String streamerIp = "";
				String streamingServersEdgeApplication = "";
				int totalStream = 0;

				
				/**
				 * 
				 * ffmpeg command for getting video heights
				 */

				FFMPEGCommands ffmpegCommands = new FFMPEGCommands();
				//
				// //int videoHeight =
				// ffmpegCommands.getSourceHeight("/home/shams/bin/ffprobe -i "+channel.getLink());
				int videoHeight = 0;
				
				try {
					videoHeight = Integer.parseInt(ffmpegCommands.getSourceHeight("/home/support/bin/ffprobe -v error -show_streams " + channel.getLink() + " | grep height"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				String cmd = "";

				int numberOfStream = 0;

				/**
				 * get country data
				 * 
				 */
				Country country = countryService.returnCountryById(""
						+ channel.getCountryId());
				String countryParameter = country.getCountryName();
				/**
				 * get category data
				 * 
				 */
				Category category = categoryService.returnCategoryById(""
						+ channel.getCategoryId());
				String categoryParameter = category.getCategoryName();
				/**
				 * get Language data
				 * 
				 */
				Language language = languageService.returnLanguageById(""
						+ channel.getLanguageId());
				String languageParameter = language.getLanguageName();

				String channelNameForOutput = "";
				/**
				 * Channel's data updated in database here.
				 * **/
				LiveChannel editChannel = null;
				Session channelDbSsession = sessionFactory.openSession();
				Transaction channelTx = null;
				try {
					channelTx = channelDbSsession.beginTransaction();
					editChannel = (LiveChannel) channelDbSsession.get(
							LiveChannel.class, Integer.parseInt(oldId));
					/**
					 * tag replacing of channel name
					 */
					try {
						tagService.updatePrimaryTag(editChannel.getChannelName(), channel.getChannelName());
					} catch (Exception e2) {
						e2.printStackTrace();
					}
					editChannel.setChannelName(channel.getChannelName());
					editChannel.setAbout(channel.getAbout());
					editChannel.setCategory(category);
					editChannel.setCountry(country);
					editChannel.setLanguage(language);
					editChannel.setLink(channel.getLink());
					editChannel.setPublishName(publishName);

					String rootPath = System.getProperty("catalina.base")
							+ "/webapps/images";

					System.out.println("............."
							+ channel.getFile().getOriginalFilename().replaceAll("\\s", "")
									.equals(""));
					if (channel.getFile().getOriginalFilename().trim()
							.equals("")) {
						editChannel.setLogo(channel.getLogo());
					} else {
						try {
							File deleteFile = new File(rootPath + "/"
									+ channel.getLogo());
							deleteFile.delete();
						} catch (Exception e) {
							e.printStackTrace();
						}
						String originalFileName = channel.getFile()
								.getOriginalFilename();
						int firstIndex = originalFileName.lastIndexOf(".");
						String fileExtension = originalFileName.substring(
								firstIndex, originalFileName.length());
						long newFileName = System.currentTimeMillis();
						String path = rootPath + "/" + newFileName
								+ fileExtension;
						System.out.println(rootPath);
						File uploadedFile = new File(path);
						if (!uploadedFile.exists())
							try {
								uploadedFile.getParentFile().mkdirs();
								uploadedFile.createNewFile();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						try {
							byte[] bytes = channel.getFile().getBytes();
							BufferedOutputStream stream = new BufferedOutputStream(
									new FileOutputStream(path));
							stream.write(bytes);
							stream.close();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						editChannel.setLogo("" + newFileName + fileExtension);
					}

					/**
					 * command to execute ffmpeg channel publish
					 */
					int indicator = 0;

					String command1 = "ssh support@"
							+ editChannel.getTranscoderIp()
							+ " \"ps -ef | grep "
							+ publishName
							+ "  | grep -v grep | awk '{print $2}' | xargs kill -INT\"";
					System.out.println(command1);

					commandService.execute(command1);
					System.out.println("killed..");
					
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					
					/**
					 * command for grabbing idle cpu and memory
					 */
					String command2 = "ssh -tt support@" + editChannel.getTranscoderIp()
							+ " TERM=vt100 ./getinfo";
					System.out.println("comman for cpu : " + command2);
					String[] cpuAndMemory = commandService
							.getCpuAndMemory(command2);
					
					double idleCpu = 0;
					double idleMemory = 0;

					try {
						if (cpuAndMemory.length == 2) {
							idleCpu = Double.parseDouble(cpuAndMemory[0]);
							idleMemory = Double.parseDouble(cpuAndMemory[1]);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					try {
						if (idleCpu > 0) {
							serverService.updateServerMemoryCpu(serverService.returnServerByIp(editChannel.getTranscoderIp()).getServerId(),
									idleCpu, idleMemory);
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					transcoderServerList = serverService
							.returnAllTranscoderServerOrderByIdleCpu();
					
					if (transcoderServerList.size() > 0) {
						for (Server server : transcoderServerList) {
							if (server.getCpuUsage() > 20) {
								transcoderServer = server;
								break;
							}
						}
					}
					
					if (transcoderServer == null) {
						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
					
					if (transcoderServer != null) {
						transcoderIp = transcoderServer.getPrivateIp();
					}
					
					// try{
					// Socket socket = new
					// Socket(InetAddress.getByName(editChannel.getTranscoderIp()),
					// 5000);
					// PrintWriter out = new
					// PrintWriter(socket.getOutputStream(),true);
					// out.println(command1);
					// socket.close();
					// out.close();
					// indicator = 1;
					// }catch(Exception e){
					// System.out.println("Error creating connection with : "+editChannel.getTranscoderIp());
					// e.printStackTrace();
					// return "error";
					// }

					Server server = serverService.returnServerByIp(editChannel
							.getStreamerIp());
					int totalNumberOfStream = server.getTotalNumberOfStream();
					int newTotalStream = totalNumberOfStream
							- editChannel.getNumberOfStream();
					try {
						if (newTotalStream >= 0) {
							serverService.updateServerTotalStream(
									server.getServerId(), newTotalStream);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
					/**
					 * Stream server info here
					 */
					streamerServerList = serverService
							.returnAllStreamerServerOrderByTotalNumberOfStream();
					if (streamerServerList.size() > 0) {
						streamerServer = streamerServerList.get(0);
					}

					if (streamerServer == null) {
						return "redirect:" + "/page/channel/all/10/0?i=e";
					}

					if (streamerServer != null) {
						streamerIp = streamerServer.getPrivateIp();
						streamingServersEdgeApplication = streamerServer
								.getEdge();
						totalStream = streamerServer.getTotalNumberOfStream();
					}
					
					System.out.println(streamerIp);
					System.out.println(transcoderIp);

					long key = System.currentTimeMillis();
					String userName = "" + session.getAttribute("username");
					String channelName = channel.getChannelName();
					channelNameForOutput = publishName;

					if (videoHeight >= 500) {
						cmd = "ssh support@"
								+ transcoderIp
								+ " /home/support/bin/ffmpeg -i "
								+ channel.getLink()
								+ " -c:v copy -c:a libfdk_aac -f flv rtmp://"
								+ streamerIp
								+ ":1935/"
								+ streamerServer.getServerName()
								+ "/"
								+ publishName
								+ "_720"
								+ " -vf scale=640:480 -minrate 800k -maxrate 800k -bufsize 800k -c:v h264 -ab 64k -ar 44100 -c:a libfdk_aac -f flv rtmp://"
								+ streamerIp
								+ ":1935/"
								+ streamerServer.getServerName()
								+ "/"
								+ publishName
								+ "_480 -vf scale=480:360 -minrate 400k -maxrate 400k -bufsize 400k -c:v h264 -ab 32k -ar 22050 -c:a libfdk_aac -f flv rtmp://"
								+ streamerIp + ":1935/"
								+ streamerServer.getServerName() + "/" + publishName + "_360 -vf scale=240:180 -minrate 200k -maxrate 200k -bufsize 200k -c:v h264 -ab 16k -ar 11025 -c:a libfdk_aac -f flv rtmp://"
								+ streamerIp + ":1935/"
								+ streamerServer.getServerName() + "/" + publishName + "_180 &";
						numberOfStream = 4;
					} else if (videoHeight < 500 && videoHeight >= 420) {
						cmd = "ssh support@"
								+ transcoderIp
								+ " /home/support/bin/ffmpeg -i "
								+ channel.getLink()
								+ " -c:v copy -c:a libfdk_aac -f flv rtmp://"
								+ streamerIp
								+ ":1935/"
								+ streamerServer.getServerName()
								+ "/"
								+ publishName
								+ "_480"
								+ " -vf scale=480:360 -minrate 400k -maxrate 400k -bufsize 400k -c:v h264 -ab 32k -ar 22050 -c:a libfdk_aac -f flv rtmp://"
								+ streamerIp + ":1935/"
								+ streamerServer.getServerName() + "/" + publishName + "_360 -vf scale=240:180 -minrate 200k -maxrate 200k -bufsize 200k -c:v h264 -ab 16k -ar 11025 -c:a libfdk_aac -f flv rtmp://"
								+ streamerIp + ":1935/"
								+ streamerServer.getServerName() + "/" + publishName + "_180 &";
						numberOfStream = 3;
					} else if (videoHeight < 420 && videoHeight >= 270) {
						cmd = "ssh support@" + transcoderIp
								+ " /home/support/bin/ffmpeg -i "
								+ channel.getLink()
								+ " -c:v copy -c:a libfdk_aac -f flv rtmp://"
								+ streamerIp + ":1935/"
								+ streamerServer.getServerName() + "/" + publishName + "_360 -vf scale=240:180 -minrate 200k -maxrate 200k -bufsize 200k -c:v h264 -ab 16k -ar 11025 -c:a libfdk_aac -f flv rtmp://"
								+ streamerIp + ":1935/"
								+ streamerServer.getServerName() + "/" + publishName + "_180 &";
						numberOfStream = 2;
					} else if (videoHeight < 270 && videoHeight > 0) {
						cmd = "ssh support@" + transcoderIp
								+ " /home/support/bin/ffmpeg -i "
								+ channel.getLink()
								+ " -c:v copy -c:a libfdk_aac -f flv rtmp://"
								+ streamerIp + ":1935/"
								+ streamerServer.getServerName() + "/" + publishName + "_180 &";
						numberOfStream = 1;
					}else {
						return "redirect:" + "/page/channel/all/10/0?i=e";
					}

					// String command =
					// "ffmpeg -i "+channel.getLink()+" -bsf:a aac_adtstoasc -codec copy -f flv rtmp://127.0.0.1:1935/test/"+userName+channelName+key+"";
					// try{
					// Socket socket = new
					// Socket(InetAddress.getByName(transcoderIp), 5000);
					// PrintWriter out = new
					// PrintWriter(socket.getOutputStream(),true);
					// out.println(cmd);
					// socket.close();
					// out.close();
					// }catch(Exception e){
					// System.out.println("Error creating connection with : "+transcoderIp);
					// e.printStackTrace();
					// return "error";
					// }
					System.out.println(cmd);

					commandService.execute(cmd);

					System.out.println("FFMPEG executed..");
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					System.out.println("AFter sleep...");
					/**
					 * command for grabbing idle cpu and memory
					 */
					String command3 = "ssh -tt support@" + transcoderIp
							+ " TERM=vt100 ./getinfo";
					System.out.println("comman for cpu : " + command2);
					String[] cpuAndMemory2 = commandService
							.getCpuAndMemory(command3);
					System.out.println("After command...");
					// try{
					// Socket socket = new
					// Socket(InetAddress.getByName(transcoderIp), 5000);
					// PrintWriter out = new
					// PrintWriter(socket.getOutputStream(),true);
					// out.println(command2);
					//
					// BufferedReader reader = new BufferedReader(new
					// InputStreamReader(
					// socket.getInputStream()));
					// StringBuffer result = new StringBuffer();
					// String line = "";
					// while ((line = reader.readLine()) != null) {
					// result.append(line + "\n");
					// }
					// System.out.println("Response from server : cpu and memory : "
					// + result.toString());
					// cpuAndMemory = result.toString().split(",");
					// socket.close();
					// out.close();
					// }catch(Exception e){
					// System.out.println("Error creating connection with : "+transcoderIp);
					// e.printStackTrace();
					// return "error";
					// }
					// System.out.println("PGREP ................:    "+command2);
					System.out.println("Streamer :.................."
							+ streamerIp);
					System.out.println("transcoder :.................."
							+ transcoderIp);
					System.out.println("command : ------    " + cmd);

					double idleCpu2 = 0;
					double idleMemory2 = 0;

					try {
						if (cpuAndMemory2.length == 2) {
							idleCpu2 = Double.parseDouble(cpuAndMemory2[0]);
							idleMemory2 = Double.parseDouble(cpuAndMemory2[1]);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					// editChannel.setUserId(Integer.parseInt(""+session.getAttribute("userId")));
					editChannel.setTranscoderIp(transcoderIp);
					editChannel.setStreamerIp(streamerIp);
					editChannel.setCommand(cmd);
					if (numberOfStream == 4) {

						try {
							if (idleCpu2 > 0) {
								serverService.updateServerMemoryCpu(
										transcoderServer.getServerId(),
										idleCpu2, idleMemory2);
							}
							serverService.updateServerTotalStream(
									streamerServer.getServerId(),
									totalStream + 4);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						editChannel.setNumberOfStream(4);
						editChannel.setLink720("http://" + streamerIp + ":1935/"
								+ streamerServer.getServerName() + "/"
								+ publishName
								+ "_720/playlist.m3u8");
						editChannel.setLink480("http://" + streamerIp + ":1935/"
								+ streamerServer.getServerName() + "/"
								+ publishName
								+ "_480/playlist.m3u8");
						editChannel.setLink360("http://" + streamerIp
								+ ":1935/" + streamerServer.getServerName()
								+ "/" + publishName
								+ "_360/playlist.m3u8");
						editChannel.setLink180("http://" + streamerIp
								+ ":1935/" + streamerServer.getServerName()
								+ "/" + publishName
								+ "_180/playlist.m3u8");
					} else if (numberOfStream == 3) {

						try {
							if (idleCpu2 > 0) {
								serverService.updateServerMemoryCpu(
										transcoderServer.getServerId(),
										idleCpu2, idleMemory2);
							}
							serverService.updateServerTotalStream(
									streamerServer.getServerId(),
									totalStream + 3);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						editChannel.setNumberOfStream(3);
						editChannel.setLink720("");
						editChannel.setLink480("http://" + streamerIp + ":1935/"
								+ streamerServer.getServerName() + "/"
								+ publishName
								+ "_480/playlist.m3u8");
						editChannel.setLink360("http://" + streamerIp
								+ ":1935/" + streamerServer.getServerName()
								+ "/" + publishName
								+ "_360/playlist.m3u8");
						editChannel.setLink180("http://" + streamerIp
								+ ":1935/" + streamerServer.getServerName()
								+ "/" + publishName
								+ "_180/playlist.m3u8");
					} else if (numberOfStream == 2) {

						try {
							if (idleCpu2 > 0) {
								serverService.updateServerMemoryCpu(
										transcoderServer.getServerId(),
										idleCpu2, idleMemory2);
							}
							serverService.updateServerTotalStream(
									streamerServer.getServerId(),
									totalStream + 2);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						editChannel.setNumberOfStream(2);
						editChannel.setLink720("");
						editChannel.setLink480("");
						editChannel.setLink360("http://" + streamerIp
								+ ":1935/" + streamerServer.getServerName()
								+ "/" + publishName
								+ "_360/playlist.m3u8");
						editChannel.setLink180("http://" + streamerIp
								+ ":1935/" + streamerServer.getServerName()
								+ "/" + publishName
								+ "_180/playlist.m3u8");
					}else if (numberOfStream == 1) {

						try {
							if (idleCpu2 > 0) {
								serverService.updateServerMemoryCpu(
										transcoderServer.getServerId(),
										idleCpu2, idleMemory2);
							}
							serverService.updateServerTotalStream(
									streamerServer.getServerId(),
									totalStream + 1);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						editChannel.setNumberOfStream(1);
						editChannel.setLink720("");
						editChannel.setLink480("");
						editChannel.setLink360("");
						editChannel.setLink180("http://" + streamerIp
								+ ":1935/" + streamerServer.getServerName()
								+ "/" + publishName
								+ "_180/playlist.m3u8");
					}
					editChannel.setLinkReturnToUser("userlink");
					editChannel.setState("up");

					/**
					 * tag update for edit channel
					 */
					try {
						Set<PrimaryTag> primaryTagSet = new HashSet<PrimaryTag>();
						String[] primaryTagsArray = {countryParameter, categoryParameter, languageParameter };
						List<PrimaryTag> primaryTagList = tagService.getSpecificPrimaryTags(primaryTagsArray);
						for (PrimaryTag primaryTag : primaryTagList) {
							primaryTagSet.add(primaryTag);
						}
						editChannel.setPrimaryTagSet(primaryTagSet);

						String tagString = channel.getTag() + "," +channel.getChannelName();

						String[] tagsArray = tagString.split(",");
						String filteredTagString = tagService.getFilteredTagsName(tagsArray);
						if (filteredTagString.length() > 0) {
							String[] filteredTagArray = filteredTagString.split(",");
							for (String tagName : filteredTagArray) {
								Tag tag = new Tag();
								tag.setTagName(tagName);
								tagService.saveTag(tag);
							}
						}

						List<Tag> tagList = tagService.getSpecificTags(tagsArray);
						Set<Tag> tagSet = new HashSet<Tag>();
						String tagNames = "";
						for (Tag tag : tagList) {
							if (!tag.getTagName().equalsIgnoreCase(channel.getChannelName())) {
								tagNames = tagNames + tag.getTagName() + ",";
							}
							tagSet.add(tag);
						}
						editChannel.setTagSet(tagSet);
						
						int last = tagNames.lastIndexOf(",");
						String finalTagNames = tagNames.substring(0, last);
						editChannel.setTag(finalTagNames);

					} catch (Exception ex) {
						ex.printStackTrace();
					}


					channelDbSsession.update(editChannel);
					channelTx.commit();

				} catch (HibernateException e) {
					if (channelTx != null)
						channelTx.rollback();
					e.printStackTrace();
					return "redirect:" + "/page/channel/all/10/0?i=e";

				} finally {
					channelDbSsession.close();
				}
				/**
				 * Log for channel log table that channel gone up
				 */
				ChannelLog channelLog = new ChannelLog();
				channelLog.setChannelId(editChannel.getChannelId());
				channelLog.setDate(new Date());
				channelLog.setLog("Channel state gone up..");
				try {
					channelLogService.saveChannelLog(channelLog);
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/channel/all/10/0?i=e";
				}

				if (numberOfStream == 4) {
					try {
						channelLinkService.updateChannelLink(
								Integer.parseInt(oldId), "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_720/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_480/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_360/playlist.m3u8","http://"
												+ streamingServersEdgeApplication
												+ ".livestreamer.com:1935/"
												+ streamingServersEdgeApplication + "/"
												+ channelNameForOutput
												+ "_180/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_720/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_480/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_360/playlist.m3u8","http://"
												+ streamingServersEdgeApplication
												+ ".livestreamer.com:1935/"
												+ streamingServersEdgeApplication + "/"
												+ channelNameForOutput
												+ "_180/playlist.m3u8");
					} catch (Exception e) {
						e.printStackTrace();
						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
					
					liveChannelService.createPlayList(channel.getChannelId()
							+ "_" + channel.getChannelName(), "http://"
							+ streamingServersEdgeApplication
							+ ".livestreamer.com:1935/"
							+ streamingServersEdgeApplication + "/"
							+ channelNameForOutput + "_720/chunklist.m3u8",
							"http://" + streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ channelNameForOutput
									+ "_480/chunklist.m3u8", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ channelNameForOutput
									+ "_360/chunklist.m3u8", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ channelNameForOutput
									+ "_180/chunklist.m3u8");
					
				} else if (numberOfStream == 3) {
					try {
						channelLinkService.updateChannelLink(
								Integer.parseInt(oldId), "", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_480/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_360/playlist.m3u8","http://"
												+ streamingServersEdgeApplication
												+ ".livestreamer.com:1935/"
												+ streamingServersEdgeApplication + "/"
												+ channelNameForOutput
												+ "_180/playlist.m3u8", "",
								"http://" + streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_480/playlist.m3u8", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_360/playlist.m3u8","http://"
												+ streamingServersEdgeApplication
												+ ".livestreamer.com:1935/"
												+ streamingServersEdgeApplication + "/"
												+ channelNameForOutput
												+ "_180/playlist.m3u8");
					} catch (Exception e) {
						e.printStackTrace();
						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
					
					liveChannelService.createPlayList(channel.getChannelId()
							+ "_" + channel.getChannelName(), "",
							"http://" + streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ channelNameForOutput
									+ "_480/chunklist.m3u8", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ channelNameForOutput
									+ "_360/chunklist.m3u8", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ channelNameForOutput
									+ "_180/chunklist.m3u8");
					
					
				} else if (numberOfStream == 2) {
					try {
						channelLinkService.updateChannelLink(
								Integer.parseInt(oldId), "", "", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_360/playlist.m3u8","http://"
												+ streamingServersEdgeApplication
												+ ".livestreamer.com:1935/"
												+ streamingServersEdgeApplication + "/"
												+ channelNameForOutput
												+ "_180/playlist.m3u8", "", "",
								"http://" + streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_360/playlist.m3u8","http://" + streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_180/playlist.m3u8");
					} catch (Exception e) {
						e.printStackTrace();
						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
					
					liveChannelService.createPlayList(channel.getChannelId()
							+ "_" + channel.getChannelName(), "",
							"", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ channelNameForOutput
									+ "_360/chunklist.m3u8", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ channelNameForOutput
									+ "_180/chunklist.m3u8");
					
				}else if (numberOfStream == 1) {
					try {
						channelLinkService.updateChannelLink(
								Integer.parseInt(oldId), "", "","", "http://"
										+ streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_180/playlist.m3u8", "", "","",
								"http://" + streamingServersEdgeApplication
										+ ".livestreamer.com:1935/"
										+ streamingServersEdgeApplication + "/"
										+ channelNameForOutput
										+ "_180/playlist.m3u8");
					} catch (Exception e) {
						e.printStackTrace();
						return "redirect:" + "/page/channel/all/10/0?i=e";
					}
					
					liveChannelService.createPlayList(channel.getChannelId()
							+ "_" + channel.getChannelName(), "",
							"", "", "http://"
									+ streamingServersEdgeApplication
									+ ".livestreamer.com:1935/"
									+ streamingServersEdgeApplication + "/"
									+ channelNameForOutput
									+ "_180/chunklist.m3u8");
					
				}

//				model.put("Admin", session.getAttribute("Admin"));
//				model.put("Publisher", session.getAttribute("Publisher"));

				return "redirect:" + "/page/channel/all/10/0?i=s";
			}
		} else {

			return "redirect:" + "/page/index";
		}

	}
	
	@RequestMapping(value = "/channel/delete", method = RequestMethod.POST)
	public String deleteChannel(LiveChannel channel, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {
            int start = 0;
			try {
				start = Integer.parseInt(""+session.getAttribute("lastChannelStatus")) ;
				LiveChannel ch = liveChannelService.returnLiveChannelById(Integer.toString(channel.getChannelId()));
				
				//kill command
				
				String name = ch.getPublishName();

				String command = "ssh support@"
						+ ch.getTranscoderIp()
						+ " \"ps -ef | grep "
						+ name
						+ "  | grep -v grep | awk '{print $2}' | xargs kill -INT\"";
				System.out.println(command);

				commandService.execute(command);
				System.out.println("killed..");
				
				Thread.sleep(10000);
				
				/**
				 * command for grabbing idle cpu and memory
				 */
				String command2 = "ssh -tt support@" + ch.getTranscoderIp()
						+ " TERM=vt100 ./getinfo";
				System.out.println("comman for cpu : " + command2);
				String[] cpuAndMemory = commandService
						.getCpuAndMemory(command2);
				
				double idleCpu = 0;
				double idleMemory = 0;

				try {
					if (cpuAndMemory.length == 2) {
						idleCpu = Double.parseDouble(cpuAndMemory[0]);
						idleMemory = Double.parseDouble(cpuAndMemory[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				Server transcoderServer = serverService.returnServerByIp(ch.getTranscoderIp());
				
				Server streamerServer = serverService.returnServerByIp(ch.getStreamerIp());
				
				if (idleCpu > 0) {
					serverService.updateServerMemoryCpu(
							transcoderServer.getServerId(),
							idleCpu, idleMemory);
				}
				
				int totalStream = serverService.returnNumberOfTotalStream(streamerServer.getServerId());
				if(totalStream - ch.getNumberOfStream()>0){
					serverService.updateServerTotalStream(
							streamerServer.getServerId(),
							totalStream - ch.getNumberOfStream());
				}
				
				
				//delete live channel
				liveChannelService.deleteChannel(ch);
				String[] tagsArray = {ch.getChannelName()};
				PrimaryTag  pTag = tagService.getSpecificPrimaryTags(tagsArray).get(0);
				tagService.deletePrimaryTag(pTag);
				ChannelLink channelLink = channelLinkService.returnChannelLinkByChannelId(ch.getChannelId());
				channelLinkService.deleteChannelLink(channelLink);
			} catch (Exception e) {
				e.printStackTrace();
				return "redirect:" + "/page/channel/all/10/" + start + "?i=e";
			}
			
			try{
					String rootPath = System.getProperty("catalina.base")
							+ "/webapps/images";
				    String path = rootPath + "/"+ channel.getChannelId()+"_"+ channel.getChannelName() + ".m3u8";
					File deleteFile = new File(path);
					deleteFile.delete();
			}catch(Exception e){
				return "redirect:" + "/page/channel/all/10/" + start + "?i=e";
			}
			
			try{
				String rootPath = System.getProperty("catalina.base")
						+ "/webapps/images";
			    String path = rootPath + "/"+ channel.getLogo();
				File deleteFile = new File(path);
				deleteFile.delete();
		}catch(Exception e){
			return "redirect:" + "/page/channel/all/10/" + start + "?i=e";
		}

			return "redirect:" + "/page/channel/all/10/" + start + "?i=s";

		} else {

			return "redirect:" + "/page/index";
		}

	}
	
	@RequestMapping(value = "/channel/deleteOwn", method = RequestMethod.POST)
	public String deleteOwnChannel(LiveChannel channel, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {
            int start = 0;
			try {
				start = Integer.parseInt(""+session.getAttribute("lastChannelStatus")) ;
				LiveChannel ch = liveChannelService.returnLiveChannelById(Integer.toString(channel.getChannelId()));
				
				//kill command
				
				String name = ch.getPublishName();

				String command = "ssh support@"
						+ ch.getTranscoderIp()
						+ " \"ps -ef | grep "
						+ name
						+ "  | grep -v grep | awk '{print $2}' | xargs kill -INT\"";
				System.out.println(command);

				commandService.execute(command);
				System.out.println("killed..");
				
				Thread.sleep(10000);
				
				/**
				 * command for grabbing idle cpu and memory
				 */
				String command2 = "ssh -tt support@" + ch.getTranscoderIp()
						+ " TERM=vt100 ./getinfo";
				System.out.println("comman for cpu : " + command2);
				String[] cpuAndMemory = commandService
						.getCpuAndMemory(command2);
				
				double idleCpu = 0;
				double idleMemory = 0;

				try {
					if (cpuAndMemory.length == 2) {
						idleCpu = Double.parseDouble(cpuAndMemory[0]);
						idleMemory = Double.parseDouble(cpuAndMemory[1]);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				Server transcoderServer = serverService.returnServerByIp(ch.getTranscoderIp());
				
				Server streamerServer = serverService.returnServerByIp(ch.getStreamerIp());
				
				if (idleCpu > 0) {
					serverService.updateServerMemoryCpu(
							transcoderServer.getServerId(),
							idleCpu, idleMemory);
				}
				
				int totalStream = serverService.returnNumberOfTotalStream(streamerServer.getServerId());
				if(totalStream - ch.getNumberOfStream()>0){
					serverService.updateServerTotalStream(
							streamerServer.getServerId(),
							totalStream - ch.getNumberOfStream());
				}
				
				
				//delete live channel
				liveChannelService.deleteChannel(ch);
				String[] tagsArray = {ch.getChannelName()};
				PrimaryTag  pTag = tagService.getSpecificPrimaryTags(tagsArray).get(0);
				tagService.deletePrimaryTag(pTag);
				ChannelLink channelLink = channelLinkService.returnChannelLinkByChannelId(ch.getChannelId());
				channelLinkService.deleteChannelLink(channelLink);
			} catch (Exception e) {
				e.printStackTrace();
				return "redirect:" + "/page/ownchannel/all/10/" + start + "?i=e";
			}
			
			try{
					String rootPath = System.getProperty("catalina.base")
							+ "/webapps/images";
				    String path = rootPath + "/"+ channel.getChannelId()+"_"+ channel.getChannelName() + ".m3u8";
					File deleteFile = new File(path);
					deleteFile.delete();
			}catch(Exception e){
				return "redirect:" + "/page/ownchannel/all/10/" + start + "?i=e";
			}
			
			try{
				String rootPath = System.getProperty("catalina.base")
						+ "/webapps/images";
			    String path = rootPath + "/"+ channel.getLogo();
				File deleteFile = new File(path);
				deleteFile.delete();
		}catch(Exception e){
			return "redirect:" + "/page/channel/all/10/" + start + "?i=e";
		}

			return "redirect:" + "/page/ownchannel/all/10/" + start + "?i=s";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/vod/{id}", method = RequestMethod.GET)
	public String showVod(@PathVariable String id, VOD vod,
			final ModelMap model, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {
			
			System.out.println("before category load:" +System.currentTimeMillis());

			Map<Integer, String> categoryMap = new HashMap<Integer, String>();
			List categories = categoryService.returnAllCategory();
			for (Iterator iterator = categories.iterator(); iterator.hasNext();) {
				Category category = (Category) iterator.next();
				categoryMap.put(category.getCategoryId(),
						category.getCategoryName());
			}
			
			System.out.println("after category load:" +System.currentTimeMillis());

			
			System.out.println("before country load:" +System.currentTimeMillis());
			
			Map<Integer, String> countryMap = new HashMap<Integer, String>();
			List countries = countryService.returnAllCountry();
			for (Iterator iterator = countries.iterator(); iterator.hasNext();) {
				Country country = (Country) iterator.next();
				countryMap
						.put(country.getCountryId(), country.getCountryName());
			}
			
			System.out.println("after country load:" +System.currentTimeMillis());
			
			System.out.println("before language load:" +System.currentTimeMillis());

			Map<Integer, String> languageMap = new HashMap<Integer, String>();
			List languages = languageService.returnAllLanguage();
			for (Iterator iterator = languages.iterator(); iterator.hasNext();) {
				Language language = (Language) iterator.next();
				languageMap.put(language.getLanguageId(),
						language.getLanguageName());
			}
			
			System.out.println("after language load:" +System.currentTimeMillis());
			
			System.out.println("before tag load:" +System.currentTimeMillis());
			Map<Integer, String> tagMap = new HashMap<Integer, String>();
			tagMap = tagService.getTagMap();
			
			System.out.println("after tag load:" +System.currentTimeMillis());

			model.put("categoryMap", categoryMap);
			model.put("countryMap", countryMap);
			model.put("languageMap", languageMap);
			model.put("tagMap", tagMap);

			if (id == null || id.equalsIgnoreCase("new")) {
				model.put("vod", vod);
				model.put("id", id);
			} else if (!id.equals("new")) {
				model.put("id", id);
				System.out.println("before editvod load:" +System.currentTimeMillis());
				VOD editVod = vodService.returnVODById(id);
				System.out.println("before editvod load:" +System.currentTimeMillis());
				if (editVod != null) {
					model.put("vod", editVod);

					model.put("countryName", editVod.getCountry().getCountryName());
					model.put("countryId", editVod.getCountry().getCountryId());
					model.put("categoryName", editVod.getCategory().getCategoryName());
					model.put("categoryId", editVod.getCategory().getCategoryId());
					model.put("languageName", editVod.getLanguage().getLanguageName());
					model.put("languageId", editVod.getLanguage().getLanguageId());
				}
			}

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));

			System.out.println("before latest vod list load:" +System.currentTimeMillis());
			List<VOD> latestVodList = new ArrayList<VOD>();
			latestVodList = vodService.getLatestFiveVods();
			model.put("latestVodList", latestVodList);
			System.out.println("after latest vod list load:" +System.currentTimeMillis());
			return "vod";
		} else {

			return "redirect:" + "/page/index";
		}

	}
		

	@RequestMapping(value = "/vod/all/{vodPerPage}/{startVodNo}", method = RequestMethod.GET)
	public String showAllVod(@PathVariable String vodPerPage, @PathVariable String startVodNo, @RequestParam String i, final ModelMap model, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int selectedVodPerPage = Integer.parseInt(vodPerPage);
			session.setAttribute("selectedVodPerPage", selectedVodPerPage);
			int startVod = Integer.parseInt(startVodNo);
            session.setAttribute("lastVodStatus", startVod);

			List<VOD> vods = vodService.returnAllVOD(startVodNo, selectedVodPerPage);
			int totalVod = vodService.returnNumberOfVOD();


			model.put("vods", vods);
			float totalPage = (float) totalVod / selectedVodPerPage;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastVodStatus", startVod);
			model.put("selectedVodPerPage", selectedVodPerPage);
			model.put("show", "all");

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("context", context.getContextPath());

			model.put("username", session.getAttribute("username"));
			if (i.equals("e")) {
				model.put("indicator", "error");
			} else if (i.equals("s")) {
				model.put("indicator", "success");
			} else {
				model.put("indicator", "");
			}
			
			List<VOD> latestVodList = new ArrayList<VOD>();
			latestVodList = vodService.getLatestFiveVods();
			model.put("latestVodList", latestVodList);

			return "allVod";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/vod/alll/{pageNo}", method = RequestMethod.GET)
	public String showAllVod(@PathVariable String pageNo, final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int pageNumber = Integer.parseInt(pageNo);
			int selectedVodPerPage = (Integer) session.getAttribute("selectedVodPerPage");
			int first = (pageNumber * selectedVodPerPage) - (selectedVodPerPage - 1);
			first--;

			List<VOD> vods = vodService.returnAllVOD(Integer.toString(first), selectedVodPerPage);
			Integer totalVod = vodService.returnNumberOfVOD();

			model.put("vods", vods);
			float totalPage = (float) totalVod / 10;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastVodStatus", first);
			model.put("selectedVodPerPage", selectedVodPerPage);
			model.put("show", "all");
			model.put("context", context.getContextPath());

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			
			List<VOD> latestVodList = new ArrayList<VOD>();
			latestVodList = vodService.getLatestFiveVods();
			model.put("latestVodList", latestVodList);
			
			return "allVod";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/vod/save", method = RequestMethod.POST)
	public String saveVod(VOD vod, @RequestParam String oldId, ModelMap model,
			HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {
			/**
			 * File upload code. Here poster for VOD is uploaded and file is
			 * renamed with unique name.
			 * **/
			if (oldId.equals("new")) {

				String newVodName = vod.getVideoName().replaceAll("\\s", "");
				vod.setVideoName(newVodName);
				String originalFileName = vod.getFile().getOriginalFilename();
				int firstIndex = originalFileName.lastIndexOf(".");
				String fileExtension = originalFileName.substring(firstIndex, originalFileName.length());
				long newFileName = System.currentTimeMillis();
				String rootPath = System.getProperty("catalina.base") + "/webapps/images";
				String path = rootPath + "/" + newFileName + fileExtension;
				File uploadedFile = new File(path);
				if (!uploadedFile.exists())
					try {
						uploadedFile.getParentFile().mkdirs();
						uploadedFile.createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				try {
					byte[] bytes = vod.getFile().getBytes();
					BufferedOutputStream stream = new BufferedOutputStream(
							new FileOutputStream(path));
					stream.write(bytes);
					stream.close();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				/**
				 * File upload code ends here.
				 */
				
				
				/**
				 * get country data
				 * 
				 */
				Country country = countryService.returnCountryById("" + vod.getCountryId());
				vod.setCountry(country);
				String countryParameter = country.getCountryName();

				/**
				 * get category data
				 * 
				 */
				Category category = categoryService.returnCategoryById("" + vod.getCategoryId());
				vod.setCategory(category);
				String categoryParameter = category.getCategoryName();
				/**
				 * get language data
				 * 
				 */
				Language language = languageService.returnLanguageById("" + vod.getLanguageId());
				vod.setLanguage(language);
				String languageParameter = language.getLanguageName();
				
				
				/**
				 * primary tag and secondary tag insertion to database
				 */
				
				try {
					Set<PrimaryTag> primaryTagSet = new HashSet<PrimaryTag>();
					String[] primaryTagsArray = {countryParameter, categoryParameter, languageParameter};
					List<PrimaryTag> primaryTagList = tagService.getSpecificPrimaryTags(primaryTagsArray);
					for (PrimaryTag primaryTag : primaryTagList) {
						primaryTagSet.add(primaryTag);
					}
					vod.setVodPrimaryTagSet(primaryTagSet);

					String tagString = vod.getTag() + "," +vod.getVideoName();
					String[] tagsArray = tagString.split(",");
					String filteredTagString = tagService.getFilteredTagsName(tagsArray);
					if (filteredTagString.length() > 0) {
						String[] filteredTagArray = filteredTagString.split(",");
						for (String tagName : filteredTagArray) {
							Tag tag = new Tag();
							tag.setTagName(tagName);
							tagService.saveTag(tag);
						}
					}

					List<Tag> tagList = tagService.getSpecificTags(tagsArray);
					Set<Tag> tagSet = new HashSet<Tag>();
					String tagNames = "";
					for (Tag tag : tagList) {
						if(!tag.getTagName().equalsIgnoreCase(vod.getVideoName())){
							tagNames = tagNames + tag.getTagName() + ",";
						}
						tagSet.add(tag);
					}
					vod.setVodTagSet(tagSet);
					
					int last = tagNames.lastIndexOf(",");
					String finalTagNames = tagNames.substring(0, last);
					vod.setTag(finalTagNames);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				/**
				 * get user data
				 */
				User user = userService.returnUserById("" + session.getAttribute("userId"));
				
				/**
				 * VOD's data stored in database here.
				 * **/
				
				vod.setPoster(newFileName + fileExtension);
				try {
					vodService.saveVOD(vod);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return "redirect:" + "/page/vod/all/10/0?i=s";
			} else {
				
				String newVodName = vod.getVideoName().replaceAll("\\s", "");
				vod.setVideoName(newVodName);
				
				/**
				 * get country data
				 * 
				 */
				Country country = countryService.returnCountryById("" + vod.getCountryId());
				String countryParameter = country.getCountryName();
				/**
				 * get category data
				 * 
				 */
				Category category = categoryService.returnCategoryById("" + vod.getCategoryId());
				String categoryParameter = category.getCategoryName();
				/**
				 * get Language data
				 * 
				 */
				Language language = languageService.returnLanguageById("" + vod.getLanguageId());
				String languageParameter = language.getLanguageName();
				
				/**
				 * Vod's data updated in database here.
				 * **/
				VOD editVod = null;
				Session vodDbSsession = sessionFactory.openSession();
				Transaction vodTx = null;
				
				try {
					
					vodTx = vodDbSsession.beginTransaction();
					editVod = (VOD) vodDbSsession.get(VOD.class, Integer.parseInt(oldId));
					
					try {
						if(!editVod.getVideoName().equalsIgnoreCase(vod.getVideoName())){
							tagService.replaceTag(editVod.getVideoName(), vod.getVideoName());
						}
						
					} catch (Exception e2) {
						e2.printStackTrace();
					}
					/**
					 * vod name update for edit vod
					 */
					editVod.setVideoName(vod.getVideoName());
					editVod.setAbout(vod.getAbout());
					editVod.setCategory(category);
					editVod.setCountry(country);
					editVod.setLanguage(language);
					
					String rootPath = System.getProperty("catalina.base")+ "/webapps/images";

					System.out.println("............." + vod.getFile().getOriginalFilename().trim().equals(""));
					if (vod.getFile().getOriginalFilename().trim().equals("")) {
						editVod.setPoster(vod.getPoster());
					} else {
						try {
							File deleteFile = new File(rootPath + "/" + vod.getPoster());
							deleteFile.delete();
						} catch (Exception e) {
							e.printStackTrace();
						}
						String originalFileName = vod.getFile().getOriginalFilename();
						int firstIndex = originalFileName.lastIndexOf(".");
						String fileExtension = originalFileName.substring(firstIndex, originalFileName.length());
						long newFileName = System.currentTimeMillis();
						String path = rootPath + "/" + newFileName + fileExtension;
						File uploadedFile = new File(path);
						if (!uploadedFile.exists())
							try {
								uploadedFile.getParentFile().mkdirs();
								uploadedFile.createNewFile();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						try {
							byte[] bytes = vod.getFile().getBytes();
							BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(path));
							stream.write(bytes);
							stream.close();
						} catch (IllegalStateException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

						editVod.setPoster("" + newFileName + fileExtension);
					}
					
					/**
					 * tag update for edit vod
					 */
					try {
						Set<PrimaryTag> primaryTagSet = new HashSet<PrimaryTag>();
						String[] primaryTagsArray = {countryParameter, categoryParameter, languageParameter };
						List<PrimaryTag> primaryTagList = tagService.getSpecificPrimaryTags(primaryTagsArray);
						for (PrimaryTag primaryTag : primaryTagList) {
							primaryTagSet.add(primaryTag);
						}
						editVod.setVodPrimaryTagSet(primaryTagSet);

						String tagString = vod.getTag() + "," +vod.getVideoName();

						String[] tagsArray = tagString.split(",");
						String filteredTagString = tagService.getFilteredTagsName(tagsArray);
						if (filteredTagString.length() > 0) {
							String[] filteredTagArray = filteredTagString.split(",");
							for (String tagName : filteredTagArray) {
								Tag tag = new Tag();
								tag.setTagName(tagName);
								tagService.saveTag(tag);
							}
						}

						List<Tag> tagList = tagService.getSpecificTags(tagsArray);
						Set<Tag> tagSet = new HashSet<Tag>();
						String tagNames = "";
						for (Tag tag : tagList) {
							if (!tag.getTagName().equalsIgnoreCase(vod.getVideoName())) {
								tagNames = tagNames + tag.getTagName() + ",";
							}
							tagSet.add(tag);
						}
						editVod.setVodTagSet(tagSet);
						
						int last = tagNames.lastIndexOf(",");
						String finalTagNames = tagNames.substring(0, last);
						editVod.setTag(finalTagNames);

					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					
					vodDbSsession.update(editVod);
					vodTx.commit();
					
				} catch (HibernateException e) {
					if (vodTx != null)
						vodTx.rollback();
					e.printStackTrace();
					return "redirect:" + "/page/vod/all/10/0?i=e";

				} finally {
					vodDbSsession.close();
				}
				
				return "redirect:" + "/page/vod/all/10/0?i=s";
			}
		} else {

			return "redirect:" + "/page/index";
		}

	}
	
	@RequestMapping(value = "/vod/delete", method = RequestMethod.POST)
	public String deleteVod(VOD vod, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			int start = 0;
			VOD tempVod;
			try {
				start = (Integer) session.getAttribute("lastVodStatus");
				tempVod = vodService.returnVODById(Integer.toString(vod.getVideoId()));				
				vodService.deleteVOD(tempVod);
                //tag delete part here (upcoming)
			} catch (Exception e) {
				e.printStackTrace();
				return "redirect:" + "/page/vod/all/10/" + start + "?i=e";
			}
			
			try{
				String rootPath = System.getProperty("catalina.base") + "/webapps/images";
			    String path = rootPath + "/"+ vod.getPoster();
				File deleteFile = new File(path);
				deleteFile.delete();
		}catch(Exception e){
			return "redirect:" + "/page/vod/all/10/" + start + "?i=e";
		}

			return "redirect:" + "/page/vod/all/10/" + start + "?i=s";

		} else {

			return "redirect:" + "/page/index";
		}

	}
	
	@RequestMapping(value = "/vodPlay/{videoId}", method = RequestMethod.GET)
	public String vodPlay(@PathVariable String videoId, final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			VOD vod = vodService.returnVODById(videoId);

			model.put("vod", vod);
			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			
			List<VOD> latestVodList = new ArrayList<VOD>();
			latestVodList = vodService.getLatestFiveVods();
			model.put("latestVodList", latestVodList);
			
			return "vodPlay";
		} else {
			
			VOD vod = vodService.returnVODById(videoId);
			model.put("vod", vod);
			
			List<VOD> latestVodList = new ArrayList<VOD>();
			latestVodList = vodService.getLatestFiveVods();
			model.put("latestVodList", latestVodList);
			
			return "vodPlay";
		}

	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
	public String user(@PathVariable String id,@RequestParam String i, User user,
			final ModelMap model, HttpSession session) {

		Map<Integer, String> countryMap = new HashMap<Integer, String>();
		Map<Integer, String> roleMap = new HashMap<Integer, String>();
		List<UserRole> list = new ArrayList<UserRole>();

		List countries = countryService.returnAllCountry();
		for (Iterator iterator = countries.iterator(); iterator.hasNext();) {
			Country country = (Country) iterator.next();
			countryMap.put(country.getCountryId(), country.getCountryName());
		}

		model.put("countryMap", countryMap);

		List roles = roleService.returnAllRole();
		for (Iterator iterator = roles.iterator(); iterator.hasNext();) {
			Role role = (Role) iterator.next();
			roleMap.put(role.getRoleId(), role.getRoleName());
		}

		model.put("roleMap", roleMap);

		if (id == null || id.equalsIgnoreCase("new")) {
			model.put("user", user);
			model.put("id", id);
		} else if (!id.equals("new")) {
			if (("" + session.getAttribute("loggedin")).equals("true")) {
				model.put("id", id);

				User usr = userService.returnUserById(id);

				if (usr != null) {
					model.put("user", usr);
					List rolesList = userRoleService
							.returnUserRolesListByUserId(usr.getUserId());
					list = rolesList;
				}
				model.put("rolesList", list);

				model.put("Admin", session.getAttribute("Admin"));
				model.put("Publisher", session.getAttribute("Publisher"));
				model.put("username", session.getAttribute("username"));
				
				if (i.equals("e")) {
					model.put("indicator", "error");
				} else if (i.equals("s")) {
					model.put("indicator", "success");
				} else {
					model.put("indicator", "");
				}
				
				List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
				latestChannelList = liveChannelService.getLatestFiveChannels();
				model.put("latestChannelList", latestChannelList);
				
				return "user";
			} else {

				return "redirect:" + "/page/index";
			}

		}
		model.put("username", session.getAttribute("username"));
		return "user";
	}

	@RequestMapping(value = "/user/save", method = RequestMethod.POST)
	public String saveUser(User user, @RequestParam String oldId,
			ModelMap model, HttpSession session) {
		if (oldId.equals("new")) {

			try {
				userService.saveUser(user);
				
				UserRole userRole = new UserRole();
				userRole.setUserRoleName("Publisher");
				userRole.setUser(user);
				
				userRoleService.saveUserRole(userRole);
			} catch (Exception e) {
				e.printStackTrace();
				return "redirect:" + "/page/user/all/10/0?i=e";
			}

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));

			return "redirect:" + "/page/user/all/10/0?i=s";
		} else {
			if (("" + session.getAttribute("loggedin")).equals("true")) {

				try {
					userService.updateUser(user, oldId);
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/user/all/10/0?i=e";
				}

				model.put("Admin", session.getAttribute("Admin"));
				model.put("Publisher", session.getAttribute("Publisher"));

				return "redirect:" + "/page/user/all/10/0?i=s";
			} else {

				return "redirect:" + "/page/index";
			}

		}
	}
	
	@RequestMapping(value = "/user/delete", method = RequestMethod.POST)
	public String deleteUser(User user, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {
            int start = (Integer) session.getAttribute("lastUserStatus");
			try {
				//System.out.println("country.getCountryId():" +country.getCountryId());
				User usr = userService.returnUserById(Integer.toString(user.getUserId()));
				userService.deleteUser(usr);
				
			} catch (Exception e) {
				e.printStackTrace();
				return "redirect:" + "/page/user/all/10/" + start + "?i=e";
			}

			return "redirect:" + "/page/user/all/10/" + start + "?i=s";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/userRole/{userId}/save", method = RequestMethod.POST)
	public String saveUserRole(@PathVariable String userId, UserRole userRole,
			ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {

			User user = userService.returnUserById(userId);
			userRole.setUser(user);
			try {
				userRoleService.saveUserRole(userRole);
			} catch (Exception e) {
				e.printStackTrace();
				model.put("username", session.getAttribute("username"));
				return "redirect:" + "/page/user/"+userId+"?i=e";
			}

			return "redirect:" + "/page/user/"+userId+"?i=s";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/userRole/{userId}/delete", method = RequestMethod.GET)
	public String deleteUserRole(@PathVariable String userId,
			@RequestParam int userRoleId, ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {

			userRoleService.deleteUserRoleByUserRoleId(userRoleId);

			return "redirect:" + "/page/user/"+userId+"?i=s";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/user/all/{userPerPage}/{startUserNo}", method = RequestMethod.GET)
	public String showAllUser(@PathVariable String userPerPage,
			@PathVariable String startUserNo, @RequestParam String i,
			final ModelMap model, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int selectedUserPerPage = Integer.parseInt(userPerPage);
			session.setAttribute("selectedUserPerPage", selectedUserPerPage);
			int startUser = Integer.parseInt(startUserNo);
			session.setAttribute("lastUserStatus", startUser);

			List<User> users = userService.returnAllUser(startUser,
					selectedUserPerPage);
			int totalUser = userService.returnNumberOfUser();

			int lastUserStatus = startUser;

			model.put("users", users);
			float totalPage = (float) totalUser / selectedUserPerPage;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastUserStatus", lastUserStatus);
			model.put("selectedUserPerPage", selectedUserPerPage);

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("context", context.getContextPath());
			model.put("username", session.getAttribute("username"));
			if (i.equals("e")) {
				model.put("indicator", "error");
			} else if (i.equals("s")) {
				model.put("indicator", "success");
			} else {
				model.put("indicator", "");
			}
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "allUsers";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/user/alll/{pageNo}", method = RequestMethod.GET)
	public String showAllUsers(@PathVariable String pageNo,
			final ModelMap model, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {

			int lastUserStatus = 0;
			int pageNumber = Integer.parseInt(pageNo);
			int selectedUserPerPage = (Integer) session
					.getAttribute("selectedUserPerPage");
			int first = (pageNumber * selectedUserPerPage)
					- (selectedUserPerPage - 1);
			first--;

			List<User> users = userService.returnAllUser(first,
					selectedUserPerPage);
			Integer totalUsers = userService.returnNumberOfUser();

			lastUserStatus = first;

			model.put("users", users);
			float totalPage = (float) totalUsers / 10;
			model.put("totalPage", Math.ceil(totalPage));
			model.put("lastUserStatus", lastUserStatus);
			model.put("selectedUserPerPage", selectedUserPerPage);
			model.put("context", context.getContextPath());

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);
			
			return "allUsers";
		} else {

			return "redirect:" + "/page/index";
		}
	}

	@RequestMapping(value = "/role/{id}", method = RequestMethod.GET)
	public String showRole(@PathVariable String id, Role role,
			final ModelMap model, HttpSession session) {
		System.out.println(session.getAttribute("loggedin"));
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			Role rl = null;
			if (id == null || id.equalsIgnoreCase("new")) {
				model.put("role", role);
				model.put("id", id);
			} else if (!id.equals("new")) {
				model.put("id", id);

				rl = roleService.returnRoleById(id);

				if (rl != null) {
					model.put("role", rl);
				}
			}

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);
			
			return "role";
		} else {

			return "redirect:" + "/page/index";
		}
	}

	@RequestMapping(value = "/role/save", method = RequestMethod.POST)
	public String saveRole(Role role, @RequestParam String oldId,
			ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			if (oldId.equals("new")) {

				try {
					roleService.saveRole(role);
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/role/all/1?i=e";
				}

				return "redirect:" + "/page/role/all/1?i=s";
			} else {

				try {
					roleService.updateRole(role, oldId);
					;
				} catch (Exception e) {
					e.printStackTrace();
					return "redirect:" + "/page/role/all/1?i=e";
				}

				return "redirect:" + "/page/role/all/1?i=s";
			}
		} else {

			return "redirect:" + "/page/index";
		}

	}
	
	@RequestMapping(value = "/role/delete", method = RequestMethod.POST)
	public String deleteRole(Role role, HttpSession session) {

		if (("" + session.getAttribute("loggedin")).equals("true")) {
            int start = (Integer) session.getAttribute("lastRoleStatus");
			try {
				//System.out.println("country.getCountryId():" +country.getCountryId());
				Role rol = roleService.returnRoleById(Integer.toString(role.getRoleId()));
				roleService.deleteRole(rol);
				
			} catch (Exception e) {
				e.printStackTrace();
				return "redirect:" + "/page/role/all/" + start + "?i=e";
			}

			return "redirect:" + "/page/role/all/" + start + "?i=s";

		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/role/all/{pageNumber}", method = RequestMethod.GET)
	public String showAllRole(@PathVariable String pageNumber,
			@RequestParam String i, final ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			int rolePerPage = 10;
			List<Role> roles = roleService.returnAllRole(pageNumber,
					rolePerPage);
			Integer totalRole = roleService.returnNumberOfRole();
			
			session.setAttribute("lastRoleStatus", Integer.parseInt(pageNumber));

			model.put("roles", roles);
			float totalPage = (float) totalRole / 10;
			model.put("totalRole", Math.ceil(totalPage));

			model.put("Admin", session.getAttribute("Admin"));
			model.put("Publisher", session.getAttribute("Publisher"));
			model.put("username", session.getAttribute("username"));
			if (i.equals("e")) {
				model.put("indicator", "error");
			} else if (i.equals("s")) {
				model.put("indicator", "success");
			} else {
				model.put("indicator", "");
			}
			
			List<LiveChannel> latestChannelList = new ArrayList<LiveChannel>();
			latestChannelList = liveChannelService.getLatestFiveChannels();
			model.put("latestChannelList", latestChannelList);

			return "allRole";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/channelSearch", method = RequestMethod.GET)
	public String channelSearch(Tag tag, ModelMap model, HttpSession session) {
		if (("" + session.getAttribute("loggedin")).equals("true")) {
			List<String> tags = new ArrayList<String>();
			try {
				tags = tagService.getAllTags();
				List<PrimaryTag> primarytags = tagService.getAllPrimaryTags();
				for (PrimaryTag pt : primarytags) {
					tags.add(pt.getPrimaryTagName());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			model.put("tags", tags);
			model.put("username", session.getAttribute("username"));
			return "channelSearch";
		} else {

			return "redirect:" + "/page/index";
		}

	}

	@RequestMapping(value = "/channelSearch/result", method = RequestMethod.POST)
	public String channelSearchResult(Tag tag, ModelMap model, HttpSession session) {
		
		String[] tagsArray = tag.getTagName().split(",");
		
		List<LiveChannel> searchedChannelList = liveChannelService.getSearchedChannelList(tagsArray);
		model.put("searchedChannelList", searchedChannelList);
		session.setAttribute("lastSearchedChannelList", searchedChannelList);
		
		List<VOD> searchedVodList = vodService.getSearchedVodList(tagsArray);
		model.put("searchedVodList", searchedVodList);
		session.setAttribute("lastSearchedVodList", searchedVodList);
		
		model.put("username", session.getAttribute("username"));
		model.put("contentType", tag.getContentType());
		session.setAttribute("lastContentType", tag.getContentType());
		
		Map<Integer, String> categoryMap = new HashMap<Integer, String>();

		List categories = categoryService.returnAllCategory();
		for (Iterator iterator = categories.iterator(); iterator.hasNext();) {
			Category category = (Category) iterator.next();
			categoryMap.put(category.getCategoryId(), category.getCategoryName());
		}

		Map<Integer, String> countryMap = new HashMap<Integer, String>();

		List countries = countryService.returnAllCountry();
		for (Iterator iterator = countries.iterator(); iterator.hasNext();) {
			Country country = (Country) iterator.next();
			countryMap.put(country.getCountryId(), country.getCountryName());
		}

		Map<Integer, String> languageMap = new HashMap<Integer, String>();

		List languages = languageService.returnAllLanguage();
		for (Iterator iterator = languages.iterator(); iterator.hasNext();) {
			Language language = (Language) iterator.next();
			languageMap.put(language.getLanguageId(), language.getLanguageName());
		}
		
		model.put("categoryMap", categoryMap);
		model.put("countryMap", countryMap);
		model.put("languageMap", languageMap);
		model.put("context", context.getContextPath());
		
		
		return "allSearchedChannel";
	}
	
	@RequestMapping(value = "/checkboxByCategory", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView checkboxSearchResult(@RequestParam Integer categoryId, ModelMap model, HttpSession session) {
		System.out.println("in checkboxbycategory");
		List<LiveChannel> searchedChannelList = new ArrayList<LiveChannel>();
		List<VOD> searchedVodList = new ArrayList<VOD>();
		try {
			 List<LiveChannel> tempSearchedChannelList = (List<LiveChannel>) session.getAttribute("lastSearchedChannelList");
		     List<VOD> tempSearchedVodList = (List<VOD>) session.getAttribute("lastSearchedVodList");
		    
		     if(tempSearchedChannelList.size() > 0){
		    	 for(LiveChannel ch : tempSearchedChannelList){
			    	 if(ch.getCategoryId() == categoryId){
			    		 searchedChannelList.add(ch);
			    	 }
			     } 
		    	 model.put("searchedChannelList", searchedChannelList);
			     session.setAttribute("lastSearchedChannelList", searchedChannelList);
		     }
	    
		    	     
		     if(tempSearchedVodList.size() > 0){
		    	 for(VOD vod : tempSearchedVodList){
			    	 if(vod.getCategory().getCategoryId() == categoryId){
			    		 searchedVodList.add(vod);
			    	 }
			     } 
		    	 model.put("searchedVodList", searchedVodList);
			     session.setAttribute("lastSearchedVodList", searchedVodList);
		     }

		    model.put("contentType", session.getAttribute("lastContentType"));
		     
		} catch (Exception e) {
			e.printStackTrace();
		}
       	
		return new ModelAndView("searchByCategory");
	}

}
