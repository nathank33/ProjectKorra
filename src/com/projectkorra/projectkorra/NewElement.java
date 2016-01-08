package com.projectkorra.projectkorra;

import org.bukkit.ChatColor;

public class NewElement {
	
	public static final NewElement AIR = new NewElement("Air");
	public static final NewElement WATER = new NewElement("Water");
	public static final NewElement EARTH = new NewElement("Earth");
	public static final NewElement FIRE = new NewElement("Fire");
	public static final NewElement CHI = new NewElement("Chi");
	public static final NewElement AVATAR = new NewElement("Avatar");
	public static final NewElement FLIGHT = new NewSubElement("Flight", AIR);
	public static final NewElement SPIRITUAL = new NewSubElement("Spiritual", AIR);
	public static final NewElement BLOOD = new NewSubElement("Blood", WATER);
	public static final NewElement HEALING = new NewSubElement("Healing", WATER);
	public static final NewElement ICE = new NewSubElement("Ice", WATER);
	public static final NewElement PLANT = new NewSubElement("Plant", WATER);
	public static final NewElement LAVA = new NewSubElement("Lava", EARTH);
	public static final NewElement METAL = new NewSubElement("Metal", EARTH);
	public static final NewElement SAND = new NewSubElement("Sand", EARTH);
	public static final NewElement LIGHTNING = new NewSubElement("Lightning", FIRE);
	public static final NewElement COMBUSTION = new NewSubElement("Combustion", FIRE);
	public static final NewElement[] MAIN_ELEMENTS = {AIR, WATER, EARTH, FIRE, CHI};
	
	private final String name;

	private NewElement(String name) {
		this.name = name;
	}
	
	public ChatColor getElementColor() {
		return ChatColor.valueOf(ProjectKorra.plugin.getConfig().getString("Properties.Chat.Colors." + name));
	}
	
	public String getName() {
		return name;
	}
	
	public static class NewSubElement extends NewElement {
		
		private NewElement parentElement;
		
		private NewSubElement(String name, NewElement parentElement) {
			super(name);
			this.parentElement = parentElement;
		}
		
		public ChatColor getElementColor() {
			return ChatColor.valueOf(ProjectKorra.plugin.getConfig().getString("Properties.Chat.Colors." + parentElement.name + "Sub"));
		}
		
		public NewElement getParentElement() {
			return this.parentElement;
		}
	}
}
