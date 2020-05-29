package com.tisawesomeness.minecord.command.general;

import com.tisawesomeness.minecord.Bot;
import com.tisawesomeness.minecord.command.Command;
import com.tisawesomeness.minecord.database.Database;
import com.tisawesomeness.minecord.util.DateUtils;
import com.tisawesomeness.minecord.util.DiscordUtils;
import com.tisawesomeness.minecord.util.MessageUtils;

import java.util.List;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

public class UserCommand extends Command {

    public CommandInfo getInfo() {
		return new CommandInfo(
			"user",
			"Shows user info.",
			"<user|id>",
			new String[]{"whois", "userinfo"},
			0,
			false,
			false,
			false
		);
    }
    
    public Result run(String[] args, MessageReceivedEvent e) {

        //If the author used the admin keyword and is an elevated user
        boolean elevated = false;
		if (args.length > 1 && args[1].equals("admin") && Database.isElevated(e.getAuthor().getIdLong())) {
            elevated = true;
            if (!args[0].matches(DiscordUtils.idRegex)) {
                return new Result(Outcome.WARNING, ":warning: Not a valid ID!");
            }
            User u = Bot.shardManager.retrieveUserById(args[0]).onErrorMap(ErrorResponse.UNKNOWN_USER::test, x -> null).complete();
            if (u == null) {
                long gid = Long.valueOf(args[0]);
                String elevatedStr = String.format("Elevated: `%s`", Database.isElevated(gid));
                if (Database.isBanned(gid)) {
                    return new Result(Outcome.SUCCESS, "__**USER BANNED FROM MINECORD**__\n" + elevatedStr);
                }
                return new Result(Outcome.SUCCESS, elevatedStr);
            }

            EmbedBuilder eb = new EmbedBuilder()
                .setTitle(MarkdownSanitizer.escape(u.getAsTag()))
                .setColor(Bot.color)
                .addField("ID", u.getId(), true)
                .addField("Bot?", u.isBot() ? "Yes" : "No", true)
                .addField("Elevated?", Database.isElevated(u.getIdLong()) ? "Yes" : "No", true);
            if (Database.isBanned(u.getIdLong())) {
                eb.setDescription("__**USER BANNED FROM MINECORD**__");
            }
            // Since user caching is disabled, retrieveMember() is required
            // This may cause a lot of requests and lag, so it must be explicitly requested
            if (args.length > 2 && args[2].equals("mutual")) {
                String mutualGuilds = Bot.shardManager.getGuilds().stream()
                    .filter(g -> {
                        try {
                            return g.retrieveMember(u).complete() != null;
                        } catch (ErrorResponseException ex) {
                            return false;
                        }
                    })
                    .map(g -> String.format("%s `%s`", g.getName(), g.getId()))
                    .collect(Collectors.joining("\n"));
                eb.addField("Mutual Guilds", mutualGuilds, false);
            }
            return new Result(Outcome.SUCCESS, MessageUtils.addFooter(eb).build());
        }
        
        // Guild-only command
        if (!e.isFromGuild()) {
            return new Result(Outcome.WARNING, ":warning: This command is not available in DMs.");
        }

        // Check for argument length
        if (args.length == 0) {
            return new Result(Outcome.WARNING, ":warning: You msut specify a user!");
        }
        
        // Find user
        Member mem = null;
        List<Member> mentioned = e.getMessage().getMentionedMembers();
        if (mentioned.size() > 0) {
            mem = mentioned.get(0);
        } else {
            if (args[0].matches(DiscordUtils.idRegex)) {
                mem = e.getGuild().getMemberById(args[0]);
            }
            if (mem == null) {
                if (!User.USER_TAG.matcher(args[0]).matches()) {
                    return new Result(Outcome.WARNING, ":warning: Not a valid user format. Use `name#1234`, a mention, or an 18-digit ID.");
                }
                mem = e.getGuild().getMemberByTag(args[0]);
                if (mem == null) {
                    return new Result(Outcome.WARNING, ":warning: That user does not exist.");
                }
            }
        }
        User u = mem.getUser();

        // Build role string
        String roles = "";
        int c = 0;
        for (Role r : mem.getRoles()) {
            roles += r.getAsMention() + "\n";
            c += 1;
            if (c == 5) {
                roles += "...";
                break;
            }
        }

        // Generate user info
        EmbedBuilder eb = new EmbedBuilder()
            .setTitle(MarkdownSanitizer.escape(u.getAsTag()))
            .setColor(mem.getColor())
            .setImage(u.getAvatarUrl())
            .addField("Nickname", mem.getNickname() == null ? "None" : MarkdownSanitizer.escape(mem.getNickname()), true)
            .addField("Bot?", u.isBot() ? "Yes" : "No", true)
            .addField("Joined Server", DateUtils.getDateAgo(mem.getTimeJoined()), false)
            .addField("Created Account", DateUtils.getDateAgo(u.getTimeCreated()), false);
        if (mem.getTimeBoosted() != null) {
            eb.addField("Boosted", DateUtils.getDateAgo(mem.getTimeBoosted()), false);
        }
        eb.addField("Roles", roles, false);
        if (elevated && Database.isBanned(u.getIdLong())) {
            eb.setDescription("__**USER BANNED FROM MINECORD**__");
        }
        return new Result(Outcome.SUCCESS, MessageUtils.addFooter(eb).build());
    }
    
}