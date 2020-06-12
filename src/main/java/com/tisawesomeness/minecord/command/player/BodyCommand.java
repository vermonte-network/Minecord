package com.tisawesomeness.minecord.command.player;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import com.tisawesomeness.minecord.Bot;
import com.tisawesomeness.minecord.command.Command;
import com.tisawesomeness.minecord.util.DateUtils;
import com.tisawesomeness.minecord.util.MessageUtils;
import com.tisawesomeness.minecord.util.NameUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class BodyCommand extends Command {
	
	public CommandInfo getInfo() {
		return new CommandInfo(
			"body",
			"Gets the body of a player.",
			"<username|uuid> [date] [overlay?]",
			new String[]{
				"nude",
				"nudes"},
			2000,
			false,
			false,
			true
		);
	}

	public String getHelp() {
		return "`{&}body <player> [date] [overlay?]` - Gets an image of the player's body.\n" +
			"\n" +
			"- `<player>` can be a username or a UUID.\n" +
			"- `[overlay?]` whether to include the second skin layer.\n" +
			"- " + DateUtils.dateHelp + "\n" +
			"\n" +
			"Examples:\n" +
			"`{&}body Tis_awesomeness`\n" +
			"`{&}body Notch 3/2/06 2:47:32`\n" +
			"`{&}body f6489b797a9f49e2980e265a05dbc3af`\n" +
			"`{&}body 069a79f4-44e9-4726-a5be-fca90e38aaf5 overlay`\n";
	}
	
	public Result run(String[] argsOrig, MessageReceivedEvent e) {
		String prefix = MessageUtils.getPrefix(e);
		
		//No arguments message
		if (argsOrig.length == 0) {
			return new Result(Outcome.WARNING, ":warning: You must specify a player.", 5);
		}
		String[] args = argsOrig;
		
		//Check for overlay argument
		boolean overlay = false;
		int index = MessageUtils.parseBoolean(args, "overlay");
		if (index != -1) {
			overlay = true;
			args = ArrayUtils.remove(args, index);
		}

		String player = args[0];
		String param = player;
		if (!player.matches(NameUtils.uuidRegex)) {
			String uuid = null;
			
			//Parse date argument
			if (args.length > 1) {
				long timestamp = DateUtils.getTimestamp(Arrays.copyOfRange(args, 1, args.length));
				if (timestamp == -1) {
					return new Result(Outcome.WARNING, MessageUtils.dateErrorString(prefix, "body"));
				}
				
			//Get the UUID
				uuid = NameUtils.getUUID(player, timestamp);
			} else {
				uuid = NameUtils.getUUID(player);
			}
			
			//Check for errors
			if (uuid == null) {
				String m = ":x: The Mojang API could not be reached." +
					"\n" + "Are you sure that username exists?" +
					"\n" + "Usernames are case-sensitive.";
				return new Result(Outcome.WARNING, m, 2);
			} else if (!uuid.matches(NameUtils.uuidRegex)) {
				String m = ":x: The API responded with an error:\n" + uuid;
				return new Result(Outcome.ERROR, m, 3);
			}
			
			param = uuid;
		}

		//Fetch body
		String url = "https://crafatar.com/renders/body/" + param.replaceAll("-", "");
		if (overlay) url += "?overlay";
		return new Result(Outcome.SUCCESS, new EmbedBuilder().setImage(url).setColor(Bot.color).build());
	}
	
}