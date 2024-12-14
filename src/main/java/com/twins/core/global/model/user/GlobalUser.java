package com.twins.core.global.model.user;

import com.twins.core.global.model.craft.Craft;
import com.twins.core.global.model.mail.Mailbox;
import com.twins.core.global.model.mail.type.MailboxType;
import com.twins.core.global.model.user.language.LanguageType;
import com.twins.core.monster.type.BuffType;
import com.twins.core.scoreboard.ScoreboardPlugin;
import com.twins.core.scoreboard.model.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class GlobalUser {

    private final String name;
    private String deathLocation;
    private int kills;
    private float playTime;
    private boolean tutorialDone;
    private boolean firstStep;
    private boolean secondStep;
    private boolean thirdStep;
    private boolean fourthStep;
    private LanguageType languageType;
    private boolean forceLanguage;
    private boolean beginner;
    private final List<Craft> crafts;
    private boolean quit;
    private int villainPoints;
    private int conquerorPoints;
    private boolean needRunnable;
    private final List<Mailbox> mailboxes;
    private final Map<BuffType, Long> personalBuffs;
    private final Map<String, Integer> recipes;

    public GlobalUser(String name, int kills, float playTime, String deathLocation, boolean tutorialDone, LanguageType languageType, boolean forceLanguage, boolean beginner, boolean quit, int villainPoints, int conquerorPoints, List<Mailbox> mailboxes) {
        this.name = name;
        this.kills = kills;
        this.playTime = playTime;
        this.tutorialDone = tutorialDone;
        this.languageType = languageType;
        this.forceLanguage = forceLanguage;
        this.beginner = beginner;
        this.quit = quit;
        this.villainPoints = villainPoints;
        this.conquerorPoints = conquerorPoints;
        this.mailboxes = mailboxes;
        this.crafts = new ArrayList<>();
        this.personalBuffs = new HashMap<>();
        this.recipes = new HashMap<>();

        if (deathLocation != null) {
            setDeathLocation(deathLocation);
        }

    }

    public void setDeathLocation(String deathLocation) {
        this.deathLocation = deathLocation;
        ScoreboardPlugin.INSTANCE.getUserService().get(name).ifPresent(User::setToUpdate);
    }

    public int getVillainPoints() {
        return villainPoints;
    }

    public void setVillainPoints(int villainPoints) {
        this.villainPoints = villainPoints;
    }

    public int getConquerorPoints() {
        return conquerorPoints;
    }

    public void setConquerorPoints(int conquerorPoints) {
        this.conquerorPoints = conquerorPoints;
    }

    public boolean isInnocent() {
        return villainPoints == 0 && conquerorPoints == 0;
    }

    public boolean isVillain() {
        return villainPoints != 0;
    }

    public boolean isConqueror() {
        return conquerorPoints != 0;
    }

    public void addKills(int amount) {
        this.kills += amount;
        ScoreboardPlugin.INSTANCE.getUserService().get(name).ifPresent(User::setToUpdate);
    }

    public void addPlayTime(float playTime) {

        int playTimeInteger = (int) this.playTime;

        this.playTime += playTime;

        if (playTimeInteger != (int) this.playTime) {
            ScoreboardPlugin.INSTANCE.getUserService().get(name).ifPresent(User::setToUpdate);
        }

    }

    public boolean isOnline() { return Bukkit.getOfflinePlayer(this.name).isOnline(); }

    public String getName() {
        return name;
    }

    public String getDeathLocation() {
        return deathLocation;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
        ScoreboardPlugin.INSTANCE.getUserService().get(name).ifPresent(User::setToUpdate);
    }

    public float getPlayTime() {
        return playTime;
    }

    public boolean isTutorialDone() {
        return tutorialDone;
    }

    public void setTutorialDone(boolean tutorialDone) {
        this.tutorialDone = tutorialDone;
    }

    public LanguageType getLanguageType() {
        return languageType;
    }

    public void setLanguageType(LanguageType languageType) {
        this.languageType = languageType;
        ScoreboardPlugin.INSTANCE.getUserService().get(name).ifPresent(User::setToUpdate);
    }

    public boolean isForceLanguage() {
        return forceLanguage;
    }

    public void setForceLanguage(boolean forceLanguage) {
        this.forceLanguage = forceLanguage;
    }

    public List<Craft> getCrafts() {
        return crafts;
    }

    public void setFirstStep(boolean firstStep) {
        this.firstStep = firstStep;
    }

    public void setSecondStep(boolean secondStep) {
        this.secondStep = secondStep;
    }

    public void setThirdStep(boolean thirdStep) {
        this.thirdStep = thirdStep;
    }

    public void setFourthStep(boolean fourthStep) {
        this.fourthStep = fourthStep;
    }

    public boolean isFirstStep() {
        return firstStep;
    }

    public boolean isSecondStep() {
        return secondStep;
    }

    public boolean isThirdStep() {
        return thirdStep;
    }

    public boolean isFourthStep() {
        return fourthStep;
    }

    public boolean isBeginner() {
        return beginner;
    }

    public void setBeginner(boolean beginner) {
        this.beginner = beginner;
    }

    public boolean isQuit() {
        return quit;
    }

    public void setQuit(boolean quit) {
        this.quit = quit;
    }

    public boolean isNeedRunnable() {
        return needRunnable;
    }

    public void setNeedRunnable(boolean needRunnable) {
        this.needRunnable = needRunnable;
    }

    public List<Mailbox> getMailboxes() {
        return mailboxes;
    }

    public boolean containsMailbox(MailboxType type, String location, long time) {

        for (Mailbox mailbox : mailboxes) {
            if (mailbox.getType().equals(type) && mailbox.getLocation().equals(location) && mailbox.getCurrentTime() == time) {
                return true;
            }
        }

        return false;
    }

    public Map<BuffType, Long> getPersonalBuffs() {
        return personalBuffs;
    }

    public Map<String, Integer> getRecipes() {
        return recipes;
    }

    public boolean hasRecipe(String recipe) {
        Integer count = recipes.get(recipe);
        return count != null && (count == -1 || count > 0);
    }

    public boolean hasRecipeOnInteract(Player player, String recipe) {

        if (player.hasPermission("recipe.admin")) {
            return true;
        }

        Integer count = recipes.get(recipe);
        return count != null && count == -1;
    }

    public boolean hasRecipe(Player player, String recipe) {

        if (player.hasPermission("recipe.admin")) {
            return true;
        }

        Integer count = recipes.get(recipe);
        return count != null && (count == -1 || count > 0);
    }

    public int getRemainingUsages(String recipe) {

        Integer count = recipes.get(recipe);

        if (count == null) {
            return 0;
        }

        return count;
    }

    public int getRecipeUsages(String recipe, int amount) {

        Integer count = recipes.get(recipe);

        if (count == null) {
            return 0;
        }

        if (count == -1) {
            return -1;
        }

        if (count >= amount) {
            return -1;
        }

        return count;
    }

    public int getRecipeUsages(Player player, String recipe, int amount) {

        if (player.hasPermission("recipe.admin")) {
            return -1;
        }

        Integer count = recipes.get(recipe);

        if (count == null) {
            return 0;
        }

        if (count == -1) {
            return -1;
        }

        if (count >= amount) {
            return -1;
        }

        return count;
    }

    public boolean hasRecipe(Player player, String recipe, int amount) {

        if (player.hasPermission("recipe.admin")) {
            return true;
        }

        Integer count = recipes.get(recipe);
        return count != null && (count == -1 || count >= amount);
    }

    public void addRecipe(String recipe) {

        int amount = switch (recipe) {
            case "c4" -> 20;
            case "fuse" -> 40;
            case "t4" -> 100;
            case "t5" -> 50;
            default -> -1;
        };

        recipes.put(recipe, amount + recipes.getOrDefault(recipe, 0));
    }

    public void removeRecipe(String recipe) {
        recipes.put(recipe, 0);
    }

    public void removeRecipe(String recipe, int amount) {

        int currentAmount = recipes.get(recipe);

        if (currentAmount == -1) {
            return;
        }

        int newAmount = currentAmount - amount;

        if (newAmount <= 0) {
            recipes.remove(recipe);
        } else {
            recipes.put(recipe, newAmount);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GlobalUser that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

}
