package com.tisawesomeness.minecord.command.player;

import com.tisawesomeness.minecord.Bot;
import com.tisawesomeness.minecord.command.Command;
import com.tisawesomeness.minecord.util.DateUtils;
import com.tisawesomeness.minecord.util.MessageUtils;
import com.tisawesomeness.minecord.util.RequestUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public class UuidatTimeCommand extends Command{

	public CommandInfo getInfo() {
		return new CommandInfo(
			"uuidattime",
			"Gets the UUID at a speicfic time of a playername.",
			"<username> <unixtimestamp>",
			new String[]{"uuidat"},
			2000,
			false,
			false,
			true
		);
	}

	public String getHelp() {
		return "`{&}uuidattime <player> <unixtime>` - Gets a UUID of a playername at a specific time.\n" +
			"\n" +
			"- `Use 0 to get the UUID of the original user of that name" +
			"- `<player>` must be a username.\n" +
			"- `<unixtimestamp>` get from https://www.unixtimestamp.com/index.php" +
			"\n";
	}

    public Result run(String[] args, MessageReceivedEvent e) throws Exception {
		String prefix = MessageUtils.getPrefix(e);
    	// No arguments message
        if (args.length == 0) {
            String m = ":warning: Incorrect arguments." +
                    "\n" + prefix + "nameattime <username|uuid> <date> " +
                    "\n" + DateUtils.dateHelp;
            return new Result(Outcome.WARNING, m, 5);
        }

        String player = args[0];
        
        String unixtime = args[1];
        
        if (unixtime == null) {
        	unixtime = ""; 
        }

        String timestamp = unixtime;
        String request = RequestUtils.get("https://api.mojang.com/users/profiles/minecraft/" + player + "?at=" + timestamp);
        if (request == null) {
            return new Result(Outcome.ERROR, ":x: Either the Mojang API could not be reached, or that name wasn't registered at the time");
        }	    
        
        EmbedBuilder eb = new EmbedBuilder()
            .setColor(Bot.color)
            .setDescription("**UUID** at the time of " + "**" + player + "**")
            .addField("Output:", request, false);
        
		return new Result(Outcome.SUCCESS, eb.build());
    }
}