package com.tisawesomeness.minecord.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.discordbots.api.client.DiscordBotListAPI;

import com.tisawesomeness.minecord.Bot;
import com.tisawesomeness.minecord.Config;

import net.dv8tion.jda.core.JDA;

public class RequestUtils {
	
	private static final String charset = StandardCharsets.UTF_8.name();
	private static final String contentType = "application/json";
	public static DiscordBotListAPI api = null;
	
	private static String get(URLConnection conn) throws IOException {
		InputStream response = conn.getInputStream();
		Scanner scanner = new Scanner(response);
		String responseBody = scanner.hasNext() ? scanner.useDelimiter("\\A").next() : "";
		scanner.close();
		return responseBody;
	}
	
	/**
	 * Performs an HTTP GET request.
	 * @param url The request URL.
	 * @return The response of the request in string form.
	 */
	public static String get(String url) {
		return get(url, null);
	}
	
	/**
	 * Performs an HTTP GET request.
	 * @param url The request URL.
	 * @param auth The content of the Authorization header.
	 * @return The response of the request in string form.
	 */
	public static String get(String url, String auth) {
		if (checkURL(url)) {
			try {
				URLConnection conn = open(url, auth);
				return get(conn);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Performs an HTTP POST request.
	 * @param url The request URL.
	 * @param query The request payload, in string form.
	 * @return The response of the request in string form.
	 */
	public static String post(String url, String query) {
		return post(url, query, null);
	}
	
	/**
	 * Performs an HTTP POST request.
	 * @param url The request URL.
	 * @param query The request payload, in string form.
	 * @param auth The content of the Authorization header.
	 * @return The response of the request in string form.
	 */
	public static String post(String url, String query, String auth) {
		try {
			URLConnection conn = open(url, auth);
			
			OutputStream output = conn.getOutputStream();
			output.write(query.getBytes(charset));
			output.close();
			
			return get(conn);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private static URLConnection open(String url, String auth) throws MalformedURLException, IOException {
		URLConnection conn = new URL(url).openConnection();
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept-Charset", charset);
		conn.setRequestProperty("Content-Type", contentType);
		if (auth != null) conn.setRequestProperty("Authorization", auth);
		return conn;
	}
	
	/**
	 * Checks if a URL exists and can respond to an HTTP request.
	 * @param url The URL to check.
	 * @return True if the URL exists, false if it doesn't or an error occured.
	 */
	public static boolean checkURL(String url) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Crafatar is being weird so this method decides whether or not to add a .png extension.
	 * @param url The URL to check. Do not include a .png extension.
	 */
	public static String checkPngExtension(String urlOrig) {
		String url = urlOrig;
		if (!checkURL(url)) {
			url += ".png";
			if (!checkURL(url)) {
				return null;
			}
		}
		return url;
	}
	
	/**
	 * Sends the guild count
	 */
	public static void sendGuilds() {
		if (Config.getSendServerCount()) {
			int servers = DiscordUtils.getGuilds().size();
			String id = Bot.id;
			
			String url = "https://bots.discord.pw/api/bots/" + id + "/stats";
			String query = "{\"server_count\": " + servers + "}";
			post(url, query, Config.getPwToken());
			
			url = "https://bots.discordlist.net/api";
			query = "{\"token\": \"" + Config.getNetToken() + "\",\"servers\": " + servers + "}";
			post(url, query);
			
			/*url = "https://discordbots.org/api/bots/" + id + "/stats";
			query = "{\"server_count\": " + servers + "}";
			post(url, query, Config.getOrgToken());*/
			
			List<Integer> serverCounts = new ArrayList<Integer>();
			for (JDA jda : Bot.shards) serverCounts.add(jda.getGuilds().size());
			api.setStats(id, serverCounts);
		}
	}

}
