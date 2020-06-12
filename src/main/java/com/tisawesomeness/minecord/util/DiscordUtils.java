package com.tisawesomeness.minecord.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tisawesomeness.minecord.Bot;
import com.tisawesomeness.minecord.Config;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class DiscordUtils {

	public static final String idRegex = "[0-9]{2,32}";
	
	public static void update() {
		Bot.shardManager.setActivity(Activity.playing(parseAll(Config.getGame())));
	}

	/**
	 * Replaces constants in the input string with their values
	 * This can be called during init, as long as Config is initialized
	 * @param input A string with {constants}
	 * @return The string with resolved constants, though variables such as {guilds} are unresolved
	 */
	public static String parseConstants(String input) {
		return input
			.replace("{author}", Bot.author)
			.replace("{author_tag}", Bot.authorTag)
			.replace("{help_server}", Bot.helpServer)
			.replace("{website}", Bot.website)
			.replace("{github}", Bot.github)
			.replace("{java_ver}", Bot.javaVersion)
			.replace("{jda_ver}", Bot.jdaVersion)
			.replace("{version}", Bot.getVersion())
			.replace("{invite}", Config.getInvite())
			.replace("{prefix}", Config.getPrefix());
	}

	/**
	 * Replaces variables in the input string with their values
	 * This must be called after init
	 * @param input A string with {variables}
	 * @return The string with resolved variables, though constants such as {version} are unresolved
	 */
	public static String parseVariables(String input) {
		return input.replace("{guilds}", String.valueOf(Bot.shardManager.getGuilds().size()));
	}

	/**
	 * Replaces variables and constants in the input string with their values
	 * This must be called after init
	 * @param input A string with {variables}
	 * @return The string with resolved variables
	 */
	public static String parseAll(String input) {
		return parseVariables(parseConstants(input));
	}
	
	public static User findUser(String search) {
		Matcher ma = Pattern.compile("(<@!?)?([0-9]{18})>?").matcher(search);
		return ma.matches() ? Bot.shardManager.getUserById(ma.group(2)) : null;
	}
	
	public static TextChannel findChannel(String search) {
		Matcher ma = Pattern.compile("(<#)?([0-9]{18})>?").matcher(search);
		return ma.matches() ? Bot.shardManager.getTextChannelById(ma.group(2)) : null;
	}

}