package com.projectkorra.projectkorra.ability.util;

import com.projectkorra.projectkorra.event.AbilityLoadEvent;
import com.projectkorra.projectkorra.util.FileExtensionFilter;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbilityLoader<T> implements Listener {

	private final Plugin plugin;
	private final File directory;
	private final Object[] parameters;
	private final Class<?>[] constructorParams;
	private final ArrayList<File> files;
	private final List<T> loadables;
	private ClassLoader loader;

	public AbilityLoader(Plugin plugin, File dir, Object... paramTypes) {
		this.plugin = plugin;
		this.directory = dir;
		this.parameters = paramTypes;
		this.files = new ArrayList<File>();
		this.loadables = new ArrayList<T>(0);

		for (File f : dir.listFiles(new FileExtensionFilter(".jar"))) {
			files.add(f);
		}

		List<Class<?>> constructorParams = new ArrayList<Class<?>>();
		for (Object paramType : paramTypes) {
			constructorParams.add(paramType.getClass());
		}

		this.constructorParams = constructorParams.toArray(new Class<?>[0]);
		List<URL> urls = new ArrayList<URL>();
		for (File file : files) {
			try {
				urls.add(file.toURI().toURL());
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		this.loader = URLClassLoader.newInstance(urls.toArray(new URL[0]), plugin.getClass().getClassLoader());
	}

	public Logger getLogger() {
		return plugin.getLogger();
	}

	@SuppressWarnings("unchecked")
	public final List<T> load(Class<?> classType) {		
		for (File file : files) {
			JarFile jarFile = null;
			try {
				jarFile = new JarFile(file);
				Enumeration<JarEntry> entries = jarFile.entries();
				
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if (!entry.getName().endsWith(".class")) {
						continue;
					}
					
			        String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
					Class<?> clazz = null;
					try {
						clazz = Class.forName(className, true, loader);
					} catch (Exception e) {
						continue;
					}

					if (!classType.isAssignableFrom(clazz) || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
						continue;
					}
					
					Class<?> loadableClass = clazz.asSubclass(classType);
					Constructor<?> ctor = loadableClass.getConstructor(constructorParams);
					T loadable = (T) ctor.newInstance(parameters);
					
					loadables.add(loadable);
					AbilityLoadEvent<T> event = new AbilityLoadEvent<T>(plugin, loadable, jarFile);
					plugin.getServer().getPluginManager().callEvent(event);
				}
		
			} catch (Exception e) {
				e.printStackTrace();
				getLogger().log(Level.WARNING, "Unknown cause");
				getLogger().log(Level.WARNING, "The JAR file " + file.getName() + " failed to load");
			} finally {
				if (jarFile != null) {
					try {
						jarFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return loadables;
	}

	public List<T> reload(Class<?> classType) {
		unload();

		List<URL> urls = new ArrayList<URL>();
		files.clear();
		for (String loadableFile : directory.list()) {
			if (loadableFile.endsWith(".jar")) {
				File file = new File(directory, loadableFile);
				files.add(file);
				try {
					urls.add(file.toURI().toURL());
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}

		this.loader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), plugin.getClass().getClassLoader());
		return load(classType);
	}

	public void unload() {
		loadables.clear();
	}
}
