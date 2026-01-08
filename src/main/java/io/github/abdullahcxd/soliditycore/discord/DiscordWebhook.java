package io.github.abdullahcxd.soliditycore.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.abdullahcxd.soliditycore.utils.Resolvable;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Discord Webhook client with fluent API
 */
public class DiscordWebhook {
    private final String webhookUrl;
    private final Gson gson;
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private final List<WebhookEmbed> embeds;

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.gson = new GsonBuilder().create();
        this.embeds = new ArrayList<>();
    }

    /**
     * Set the message content
     */
    public DiscordWebhook setContent(Resolvable<String> content) {
        this.content = content.resolve();
        return this;
    }

    /**
     * Set the webhook username
     */
    public DiscordWebhook setUsername(Resolvable<String> username) {
        this.username = username.resolve();
        return this;
    }

    /**
     * Set the webhook avatar URL
     */
    public DiscordWebhook setAvatarUrl(Resolvable<String> avatarUrl) {
        this.avatarUrl = avatarUrl.resolve();
        return this;
    }

    /**
     * Enable text-to-speech
     */
    public DiscordWebhook setTts(boolean tts) {
        this.tts = tts;
        return this;
    }

    /**
     * Add an embed to the webhook
     */
    public DiscordWebhook addEmbed(Resolvable<WebhookEmbed> embed) {
        WebhookEmbed resolved = embed.resolve();
        if (resolved != null) {
            this.embeds.add(resolved);
        }
        return this;
    }

    /**
     * Create and add an embed using a builder
     */
    public DiscordWebhook createEmbed(Function<WebhookEmbed.Builder, WebhookEmbed> embedBuilder) {
        WebhookEmbed embed = embedBuilder.apply(new WebhookEmbed.Builder());
        this.embeds.add(embed);
        return this;
    }

    /**
     * Clear all embeds
     */
    public DiscordWebhook clearEmbeds() {
        this.embeds.clear();
        return this;
    }

    /**
     * Execute the webhook
     */
    @SneakyThrows
    public void execute() throws IOException {
        WebhookPayload payload = new WebhookPayload();
        payload.setContent(this.content);
        payload.setUsername(this.username);
        payload.setAvatarUrl(this.avatarUrl);
        payload.setTts(this.tts);
        payload.setEmbeds(this.embeds.isEmpty() ? null : this.embeds);

        String json = gson.toJson(payload);

        URL url = new URI(webhookUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("Webhook request failed with status: " + responseCode);
        }
    }
}