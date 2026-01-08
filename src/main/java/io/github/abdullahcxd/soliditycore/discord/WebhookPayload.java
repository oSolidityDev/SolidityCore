package io.github.abdullahcxd.soliditycore.discord;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Internal payload class for GSON serialization
 */
@Getter
@Setter
public class WebhookPayload {
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private List<WebhookEmbed> embeds;
}