package com.pilv.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // 🔒 CONTENT MODERATION - Blocked words list
    private static final String[] BLOCKED_WORDS = {
            "fuck", "shit", "bitch", "cunt", "dick", "pussy",
            "nigger", "faggot", "retard", "whore", "slut",
            "asshole", "bastard", "motherfucker", "bullshit",
            "porn", "xxx", "sex", "nude", "naked", "penis",
            "vagina", "boobs", "tits", "cock", "kill yourself",
            "die", "hate", "stupid", "idiot", "moron", "loser", "pathetic"
    };

    // ❓ ICEBREAKER QUESTIONS
    private String[] icebreakerQuestions = {
            "What is the best thing that happened to you today?",
            "What is your absolute go-to comfort food when you've had a long day?",
            "What is the most overrated thing everyone else seems to love?"
    };

    // UI Elements
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView icebreakerButton;
    private TextView nextButton;
    private TextView mentalHealthButton;
    private RecyclerView messagesList;
    private TextView connectionStatus;
    private TextView cloudTimer;
    private MessageAdapter adapter;
    private List<String> messages = new ArrayList<>();
    private String userId = "user_" + System.currentTimeMillis();
    private int messageBlockCount = 0;

    // Firebase
    private FirebaseFirestore db;
    private String currentChatPartner = null;

    // Mental Health
    private MentalHealthHelper mentalHealthHelper;

    // Moderation
    private ModerationHelper moderationHelper;

    // Profile
    private String currentUsername = "";
    private String currentLocation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize Helpers
        mentalHealthHelper = new MentalHealthHelper();
        moderationHelper = new ModerationHelper();

        // Initialize views
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        icebreakerButton = findViewById(R.id.icebreakerButton);
        nextButton = findViewById(R.id.nextButton);
        mentalHealthButton = findViewById(R.id.mentalHealthButton);
        messagesList = findViewById(R.id.messagesList);
        connectionStatus = findViewById(R.id.connectionStatus);
        cloudTimer = findViewById(R.id.cloudTimer);

        // Setup RecyclerView
        adapter = new MessageAdapter(messages);
        messagesList.setLayoutManager(new LinearLayoutManager(this));
        messagesList.setAdapter(adapter);

        // 👤 Check if user has a profile
        checkProfile();

        connectionStatus.setText("☁️ Looking for someone to chat with...");

        // Start listening for messages
        startCloudTimer();
        listenForMessages();
        findNewChatPartner();

        // Send button with moderation
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString().trim();
                if (message.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Type a message", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 🔒 AI CONTENT MODERATION
                ModerationHelper.ModerationResult result = moderationHelper.checkMessage(message);
                if (!result.isAllowed) {
                    String reason = result.getReasonText();
                    Toast.makeText(MainActivity.this,
                            "🚫 Message blocked: " + reason,
                            Toast.LENGTH_LONG).show();

                    if (++messageBlockCount > 3) {
                        showModerationWarningDialog();
                    }
                    messageInput.setText("");
                    return;
                }

                sendMessageToFirebase(message);
                messageInput.setText("");
            }
        });

        // ❓ Icebreaker button
        icebreakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIcebreakerDialog();
            }
        });

        // ⏭️ Next button - Skip to new person
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findNewChatPartner();
            }
        });

        // 🧘 Mental Health button
        mentalHealthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMentalHealthMenu();
            }
        });
    }

    // 👤 PROFILE CHECK
    private void checkProfile() {
        SharedPreferences prefs = getSharedPreferences("pilv_profile", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        currentLocation = prefs.getString("location", "📍 Unknown");

        if (username.isEmpty()) {
            // No profile yet, show profile screen
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        } else {
            // User has a profile
            currentUsername = username;
            connectionStatus.setText("☁️ Welcome, " + currentUsername + "!");
        }
    }

    // 🔍 FIND A NEW CHAT PARTNER
    private void findNewChatPartner() {
        connectionStatus.setText("☁️ Looking for someone new...");
        messages.clear();
        adapter.notifyDataSetChanged();

        currentChatPartner = null;
        userId = "user_" + System.currentTimeMillis();

        messages.add("🔄 Searching for a new chat partner...");
        adapter.notifyDataSetChanged();

        messageInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentChatPartner = "partner_" + System.currentTimeMillis();
                connectionStatus.setText("☁️ Connected to someone new!");
                messages.add("✨ You're now chatting with a new person!");
                messages.add("💬 Say hello or ask an icebreaker!");
                adapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "🎉 Connected to someone new!", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }

    // 🔒 MODERATION WARNING
    private void showModerationWarningDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🚫 Multiple Violations Detected")
                .setMessage("You've sent multiple messages that violate our community guidelines. Please be respectful and keep the chat safe for everyone.")
                .setPositiveButton("I Understand", null)
                .setNegativeButton("View Guidelines", (dialog, which) -> {
                    // Show guidelines
                    Toast.makeText(this, "📋 Community Guidelines: No hate speech, no harassment, no explicit content.", Toast.LENGTH_LONG).show();
                })
                .show();
    }

    // ❓ SHOW ICEBREAKER DIALOG
    private void showIcebreakerDialog() {
        new AlertDialog.Builder(this)
                .setTitle("❓ Icebreaker Question")
                .setItems(icebreakerQuestions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String question = icebreakerQuestions[which];
                        messageInput.setText(question);
                        Toast.makeText(MainActivity.this, "💬 " + question, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // 🧘 MENTAL HEALTH MENU
    private void showMentalHealthMenu() {
        String[] options = {
                "💬 Affirmation",
                "🌬️ Breathing Exercise",
                "💚 Supportive Question",
                "📞 Mental Health Resources",
                "😊 Mood Check",
                "👩‍⚕️ Psychologist Directory"
        };

        new AlertDialog.Builder(this)
                .setTitle("🧘 Mental Health Support")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                showAffirmation();
                                break;
                            case 1:
                                showBreathingExercise();
                                break;
                            case 2:
                                showSupportiveQuestion();
                                break;
                            case 3:
                                showResources();
                                break;
                            case 4:
                                showMoodCheck();
                                break;
                            case 5:
                                showPsychologistDirectory();
                                break;
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // 💬 AFFIRMATION
    private void showAffirmation() {
        String affirmation = mentalHealthHelper.getRandomAffirmation();
        messages.add("🧘 Pilv: " + affirmation);
        adapter.notifyDataSetChanged();
        messagesList.smoothScrollToPosition(messages.size() - 1);
        Toast.makeText(this, "💫 " + affirmation, Toast.LENGTH_LONG).show();
    }

    // 🌬️ BREATHING EXERCISE
    private void showBreathingExercise() {
        String exercise = mentalHealthHelper.getBreathingExercise();
        messages.add("🧘 Pilv: " + exercise);
        adapter.notifyDataSetChanged();
        messagesList.smoothScrollToPosition(messages.size() - 1);
    }

    // 💚 SUPPORTIVE QUESTION
    private void showSupportiveQuestion() {
        String question = mentalHealthHelper.getRandomSupportiveQuestion();
        messageInput.setText(question);
        Toast.makeText(this, "💚 " + question, Toast.LENGTH_LONG).show();
    }

    // 📞 RESOURCES
    private void showResources() {
        String[] resources = mentalHealthHelper.getResources();
        String resourceText = "";
        for (String r : resources) {
            resourceText += r + "\n\n";
        }
        messages.add("🧘 Pilv: 📞 Helpline Resources:\n\n" + resourceText);
        adapter.notifyDataSetChanged();
        messagesList.smoothScrollToPosition(messages.size() - 1);
    }

    // 😊 MOOD CHECK
    private void showMoodCheck() {
        String[] moods = {
                "😊 Happy", "😢 Sad", "😠 Angry", "😰 Anxious",
                "😴 Tired", "🤗 Hopeful", "😌 Peaceful", "😔 Lonely"
        };

        new AlertDialog.Builder(this)
                .setTitle("😊 How are you feeling?")
                .setItems(moods, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mood = moods[which];
                        String response = mentalHealthHelper.getMoodCheckMessage();
                        messages.add("🧘 Pilv: You feel " + mood + ". " + response);
                        adapter.notifyDataSetChanged();
                        messagesList.smoothScrollToPosition(messages.size() - 1);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // 👩‍⚕️ PSYCHOLOGIST DIRECTORY
    private void showPsychologistDirectory() {
        List<MentalHealthHelper.Psychologist> psychologists = mentalHealthHelper.getPsychologists();

        String[] names = new String[psychologists.size()];
        for (int i = 0; i < psychologists.size(); i++) {
            MentalHealthHelper.Psychologist p = psychologists.get(i);
            names[i] = p.name + "\n" + p.title + "\n📍 " + p.country;
        }

        new AlertDialog.Builder(this)
                .setTitle("👩‍⚕️ Psychologist Directory")
                .setItems(names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showPsychologistDetails(psychologists.get(which));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPsychologistDetails(MentalHealthHelper.Psychologist p) {
        String details = "📋 " + p.title + "\n\n" +
                "📍 " + p.country + "\n" +
                "📞 " + p.phone + "\n" +
                "✉️ " + p.email + "\n" +
                "⏰ " + p.availability + "\n" +
                "🗣️ " + p.languages + "\n\n" +
                "💚 For immediate support, call or email directly.";

        new AlertDialog.Builder(this)
                .setTitle("👩‍⚕️ " + p.name)
                .setMessage(details)
                .setPositiveButton("📞 Call Now", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(android.net.Uri.parse("tel:" + p.phone));
                    startActivity(intent);
                })
                .setNeutralButton("✉️ Email", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(android.net.Uri.parse("mailto:" + p.email));
                    startActivity(intent);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    // 📤 SEND MESSAGE TO FIREBASE
    private void sendMessageToFirebase(String text) {
        if (currentChatPartner == null) {
            Toast.makeText(this, "⏳ Waiting for a chat partner...", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("pilv_profile", MODE_PRIVATE);
        String username = prefs.getString("username", "Newbie");
        String location = prefs.getString("location", "📍 Unknown");

        ChatMessage chatMessage = new ChatMessage(text, userId, username, location);
        db.collection("chats")
                .add(chatMessage)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MainActivity.this, "☁️ Sent!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "❌ Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 📥 LISTEN FOR MESSAGES FROM FIREBASE
    private void listenForMessages() {
        db.collection("chats")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        messages.clear();

                        if (currentChatPartner == null) {
                            messages.add("🔄 Looking for a chat partner...");
                        } else {
                            messages.add("✨ Connected to someone new!");
                            messages.add("💬 Say hello or ask an icebreaker!");
                        }

                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots) {
                                ChatMessage msg = doc.toObject(ChatMessage.class);
                                if (msg != null) {
                                    // Show username and location for other users
                                    String sender;
                                    if (msg.getSenderId().equals(userId)) {
                                        sender = "You";
                                    } else {
                                        String name = msg.getSenderName() != null ? msg.getSenderName() : "Newbie";
                                        String location = msg.getSenderLocation() != null ? msg.getSenderLocation() : "📍 Unknown";
                                        sender = name + " (" + location + ")";
                                    }
                                    messages.add(sender + ": " + msg.getText());
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (messages.size() > 0) {
                            messagesList.smoothScrollToPosition(messages.size() - 1);
                        }
                    }
                });
    }

    // ⏰ STRICT 24-HOUR TIMER
    private void startCloudTimer() {
        SharedPreferences prefs = getSharedPreferences("pilv_timer", MODE_PRIVATE);
        long startTime = prefs.getLong("startTime", 0);

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
            prefs.edit().putLong("startTime", startTime).apply();
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        long remainingTime = Math.max(0, 24 * 60 * 60 * 1000 - elapsedTime);

        if (remainingTime <= 0) {
            cloudTimer.setText("Expired");
            messageInput.setEnabled(false);
            sendButton.setEnabled(false);
            connectionStatus.setText("Cloud faded away");
            Toast.makeText(MainActivity.this, "💨 Cloud disappeared!", Toast.LENGTH_LONG).show();
            return;
        }

        new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = millisUntilFinished / 3600000;
                long minutes = (millisUntilFinished % 3600000) / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                cloudTimer.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

                if (hours < 1) {
                    cloudTimer.setTextColor(0xFFFF6B6B);
                }
            }

            @Override
            public void onFinish() {
                cloudTimer.setText("Expired");
                messageInput.setEnabled(false);
                sendButton.setEnabled(false);
                connectionStatus.setText("Cloud faded away");
                Toast.makeText(MainActivity.this, "💨 Cloud disappeared!", Toast.LENGTH_LONG).show();
            }
        }.start();
    }
}