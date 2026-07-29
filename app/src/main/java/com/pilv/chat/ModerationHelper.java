package com.pilv.chat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ModerationHelper {

    // 🚫 BLOCKED WORDS (Expanded)
    private static final Set<String> BLOCKED_WORDS = new HashSet<>(Arrays.asList(
            // Profanity
            "fuck", "shit", "bitch", "cunt", "dick", "pussy", "asshole", "bastard",
            "motherfucker", "bullshit", "goddamn", "damn", "hell", "cock", "prick",
            "slut", "whore", "crap", "fag", "faggot", "retard", "retarded",

            // Sexual
            "porn", "xxx", "sex", "nude", "naked", "penis", "vagina", "boobs",
            "tits", "breast", "nipple", "clit", "erection", "orgasm", "masturbate",
            "blowjob", "handjob", "cum", "semen", "molest", "rape", "incest",

            // Harassment
            "kill yourself", "die", "hate", "stupid", "idiot", "moron", "loser",
            "pathetic", "worthless", "useless", "ugly", "fat", "disgusting",
            "retard", "crazy", "insane", "psycho", "weirdo", "creep", "stalker",

            // Drugs
            "cocaine", "heroin", "meth", "crack", "ecstasy", "mdma", "lsd",
            "shrooms", "weed", "marijuana", "drugs", "dealer", "drug",

            // Violence
            "bomb", "shoot", "kill", "murder", "suicide", "terror", "attack",
            "gun", "knife", "weapon", "blood", "gore", "torture", "abuse",

            // Racial/Discrimination
            "nigger", "nigga", "chink", "gook", "kike", "spic", "wetback",
            "raghead", "towelhead", "cameljockey", "sandnigger", "ape",

            // Sexual Orientation/Gender
            "dyke", "fag", "homo", "tranny", "shemale", "queer", "gay"
    ));

    // 📧 Email detection
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );

    // 📱 Phone number detection
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\+?[0-9]{10,15}"
    );

    // 🔗 Link detection
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );

    // 👤 Social media detection
    private static final Pattern SOCIAL_PATTERN = Pattern.compile(
            "instagram|instagram\\.com|ig\\.com|snapchat|snap|tiktok|tiktok\\.com"
    );

    public ModerationResult checkMessage(String text) {
        ModerationResult result = new ModerationResult();
        result.isAllowed = true;
        result.reasons = new HashSet<>();

        String lowerText = text.toLowerCase();

        // 1. Check for blocked words
        for (String word : BLOCKED_WORDS) {
            if (lowerText.contains(word)) {
                result.isAllowed = false;
                result.reasons.add("Inappropriate language detected");
                break;
            }
        }

        // 2. Check for hate speech (context-based)
        if (containsHateSpeech(lowerText)) {
            result.isAllowed = false;
            result.reasons.add("Hate speech detected");
        }

        // 3. Check for personal information (email, phone)
        if (EMAIL_PATTERN.matcher(text).find()) {
            result.isAllowed = false;
            result.reasons.add("Email addresses are not allowed");
        }

        if (PHONE_PATTERN.matcher(text).find()) {
            result.isAllowed = false;
            result.reasons.add("Phone numbers are not allowed");
        }

        // 4. Check for social media handles
        if (SOCIAL_PATTERN.matcher(lowerText).find()) {
            result.isAllowed = false;
            result.reasons.add("Social media references are not allowed");
        }

        // 5. Check for spam (multiple links)
        if (LINK_PATTERN.matcher(text).results().count() > 3) {
            result.isAllowed = false;
            result.reasons.add("Too many links (possible spam)");
        }

        // 6. Check for all caps (shouting)
        if (text.length() > 20 && text.equals(text.toUpperCase())) {
            result.isAllowed = false;
            result.reasons.add("All caps messages are not allowed");
        }

        // 7. Check for excessive repetition
        if (isExcessiveRepetition(text)) {
            result.isAllowed = false;
            result.reasons.add("Excessive repetition (spam)");
        }

        return result;
    }

    private boolean containsHateSpeech(String text) {
        String[] hateWords = {
                "hate", "kill all", "die all", "burn", "exterminate",
                "superior", "inferior", "race", "ethnic", "white power",
                "black power", "nazi", "holocaust", "genocide"
        };
        for (String word : hateWords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExcessiveRepetition(String text) {
        if (text.length() < 20) return false;

        // Check if same character repeated > 5 times
        char[] chars = text.toCharArray();
        int count = 1;
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == chars[i-1]) {
                count++;
                if (count > 5) return true;
            } else {
                count = 1;
            }
        }

        // Check if same word repeated > 3 times
        String[] words = text.split(" ");
        for (String word : words) {
            int wordCount = 0;
            for (String w : words) {
                if (w.equalsIgnoreCase(word)) wordCount++;
                if (wordCount > 3) return true;
            }
        }
        return false;
    }

    public static class ModerationResult {
        public boolean isAllowed;
        public Set<String> reasons;

        public String getReasonText() {
            if (reasons == null || reasons.isEmpty()) {
                return "Message blocked for safety";
            }
            return String.join(", ", reasons);
        }
    }
}