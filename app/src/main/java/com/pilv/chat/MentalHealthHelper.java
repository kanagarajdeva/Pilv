package com.pilv.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MentalHealthHelper {

    private static final List<String> AFFIRMATIONS = Arrays.asList(
            "💪 You are stronger than you think.",
            "🌟 You are enough, just as you are.",
            "🌱 Every day is a new beginning.",
            "💖 You deserve love and happiness.",
            "🌈 This too shall pass.",
            "✨ You are capable of amazing things.",
            "🌺 Take a deep breath. You've got this.",
            "🦋 Growth is beautiful, even when it's hard.",
            "🌞 You are a light in this world.",
            "💫 You are not alone. You matter.",
            "🌿 Be gentle with yourself today.",
            "💎 You are worthy of all the good things.",
            "🌊 Your feelings are valid.",
            "🌟 You are doing the best you can.",
            "💗 You are loved."
    );

    private static final List<String> SUPPORTIVE_QUESTIONS = Arrays.asList(
            "How are you feeling right now? I'm here to listen.",
            "What's something that made you smile recently?",
            "Is there anything you'd like to talk about?",
            "What helps you feel better when you're down?",
            "What are you grateful for today?",
            "What's something good that happened this week?",
            "How can I support you right now?",
            "What's your favorite way to relax?"
    );

    private static final List<String> RESOURCES = Arrays.asList(
            "📞 988 Suicide & Crisis Lifeline: Call or text 988",
            "📞 Crisis Text Line: Text HOME to 741741",
            "📞 National Mental Health Helpline: 1-800-273-TALK",
            "📞 SAMHSA Helpline: 1-800-662-4357",
            "📞 For international resources, visit findahelpline.com"
    );

    private Random random = new Random();

    public String getRandomAffirmation() {
        return AFFIRMATIONS.get(random.nextInt(AFFIRMATIONS.size()));
    }

    public String getRandomSupportiveQuestion() {
        return SUPPORTIVE_QUESTIONS.get(random.nextInt(SUPPORTIVE_QUESTIONS.size()));
    }

    public String[] getResources() {
        return RESOURCES.toArray(new String[0]);
    }

    public String getBreathingExercise() {
        return "🌬️ Breathing Exercise:\n\n" +
                "1. Breathe in slowly for 4 seconds...\n" +
                "2. Hold for 4 seconds...\n" +
                "3. Breathe out slowly for 6 seconds...\n" +
                "4. Pause for 2 seconds...\n" +
                "🔄 Repeat 5 times.\n\n" +
                "You are doing great! 💪";
    }

    public String getMoodCheckMessage() {
        String[] moods = {
                "😊 Happy - That's wonderful! Share that joy with someone.",
                "😢 Sad - It's okay to feel sad. You're not alone. 💙",
                "😠 Angry - Take a deep breath. Your feelings are valid. 💜",
                "😰 Anxious - You are safe. Breathe deeply. 💚",
                "😴 Tired - Rest is important. Take a break. 💛",
                "🤗 Hopeful - That's beautiful! Keep that hope. 💖",
                "😌 Peaceful - Enjoy this calm moment. 🕊️",
                "😔 Lonely - You are not alone. We're here for you. 🤗"
        };
        return moods[random.nextInt(moods.length)];
    }

    // 👩‍⚕️ PSYCHOLOGIST DIRECTORY
    public static class Psychologist {
        public String name;
        public String title;
        public String country;
        public String phone;
        public String email;
        public String availability;
        public String languages;

        public Psychologist(String name, String title, String country,
                            String phone, String email, String availability,
                            String languages) {
            this.name = name;
            this.title = title;
            this.country = country;
            this.phone = phone;
            this.email = email;
            this.availability = availability;
            this.languages = languages;
        }
    }

    public List<Psychologist> getPsychologists() {
        List<Psychologist> list = new ArrayList<>();
        list.add(new Psychologist(
                "Dr. Fabiana Borelli",
                "BACP-Accredited Counsellor & Psychotherapist",
                "UK (Online/International)",
                "+44 7359 322 820",
                "fabiana@relationalspaces.com",
                "24/7 for urgent cases",
                "English, Portuguese, Spanish"
        ));
        list.add(new Psychologist(
                "Dr. Jessica Carlile",
                "Clinical Psychologist",
                "USA (WA & PSYPACT States)",
                "(206) 590-4094",
                "jessica@elevatepsych.com",
                "24/7 on-call service",
                "English"
        ));
        list.add(new Psychologist(
                "Dr. Aurora Falcone",
                "Chartered Clinical Psychologist",
                "London, UK",
                "+44 7359 322 820",
                "aurora@relationalspaces.com",
                "24/7 availability",
                "English, Italian"
        ));
        return list;
    }
}