package io.github.abdullahcxd.soliditycore.discord;

import io.github.abdullahcxd.soliditycore.utils.Resolvable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WebhookEmbed {
    private String title;
    private String description;
    private String url;
    private Integer color;
    private Footer footer;
    private Thumbnail thumbnail;
    private Image image;
    private Author author;
    private List<Field> fields;
    private String timestamp;

    private WebhookEmbed() {
        this.fields = new ArrayList<>();
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Builder {
        private final WebhookEmbed embed;

        public Builder() {
            this.embed = new WebhookEmbed();
        }

        public Builder setTitle(Resolvable<String> title) {
            embed.title = title.resolve();
            return this;
        }

        public Builder setDescription(Resolvable<String> description) {
            embed.description = description.resolve();
            return this;
        }

        public Builder setUrl(Resolvable<String> url) {
            embed.url = url.resolve();
            return this;
        }

        public Builder setColor(Color color) {
            embed.color = color.getRGB() & 0xFFFFFF;
            return this;
        }

        public Builder setColor(int rgb) {
            embed.color = rgb & 0xFFFFFF;
            return this;
        }

        public Builder setFooter(@NotNull Resolvable<String> text, Resolvable<String> iconUrl) {
            embed.footer = new Footer();
            embed.footer.setText(text.resolve());
            embed.footer.setIconUrl(iconUrl != null ? iconUrl.resolve() : null);
            return this;
        }

        public Builder setThumbnail(Resolvable<String> url) {
            embed.thumbnail = new Thumbnail();
            embed.thumbnail.setUrl(url.resolve());
            return this;
        }

        public Builder setImage(Resolvable<String> url) {
            embed.image = new Image();
            embed.image.setUrl(url.resolve());
            return this;
        }

        public Builder setAuthor(Resolvable<String> name, Resolvable<String> url, Resolvable<String> iconUrl) {
            embed.author = new Author();
            embed.author.setName(name.resolve());
            embed.author.setUrl(url != null ? url.resolve() : null);
            embed.author.setIconUrl(iconUrl != null ? iconUrl.resolve() : null);
            return this;
        }

        public Builder addField(Resolvable<String> name, Resolvable<String> value, boolean inline) {
            Field field = new Field();
            field.setName(name.resolve());
            field.setValue(value.resolve());
            field.setInline(inline);
            embed.fields.add(field);
            return this;
        }

        public Builder setTimestamp(String timestamp) {
            embed.timestamp = timestamp;
            return this;
        }

        public WebhookEmbed build() {
            return embed;
        }
    }
}