package md.mirrerror.discordutils.discord;

import md.mirrerror.discordutils.Main;
import net.dv8tion.jda.api.entities.Activity;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ActivityManager {

    private final List<Activity> botActivities;
    private Iterator<Activity> botActivitiesIterator;

    public ActivityManager() {
        this.botActivities = getActivitiesFromConfig();
        this.botActivitiesIterator = botActivities.iterator();
    }

    public Activity nextActivity() {
        if(!botActivitiesIterator.hasNext()) {
            botActivitiesIterator = botActivities.iterator();
        }
        return botActivitiesIterator.next();
    }

    private static List<Activity> getActivitiesFromConfig() {
        final List<Activity> botActivities = new LinkedList<>();
        Main.getInstance().getConfigManager().getConfig().getConfigurationSection("Discord.Activities").getKeys(false).forEach(activity -> {
            if(!activity.equals("UpdateDelay")) {
                Activity.ActivityType activityType = Activity.ActivityType.valueOf(Main.getInstance().getConfigManager().getConfig().getString("Discord.Activities." + activity + ".Type").toUpperCase());
                String activityText = Main.getInstance().getConfigManager().getConfig().getString("Discord.Activities." + activity + ".Text");
                botActivities.add(Activity.of(activityType, activityText));
            }
        });
        return botActivities;
    }

    public List<Activity> getBotActivities() {
        return botActivities;
    }
}
