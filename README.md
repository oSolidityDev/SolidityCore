# SolidityCore

A library made to make plugins easier, used with oSolidity plugins made by AbdullahCXD

## What features does this include?

This library includes everything! This plugin is built like a framework where you don't extend `JavaPlugin` but rather `SolidityPlugin` which includes more methods and more helpers for your plugins

Features the library includes:
 - Builders: ItemBuilder, LegacyMessageBuilder (Deprecated in favor of MessageBuilder), MessageBuilder, ParticleBuilder, PlaceholderBuilder, SchedulerBuilder (Deprecated in favor of TaskChain), SoundBuilder, TitleBuilder
 - Commands: BaseCommand, CommandContext, ArgumentResolver, CommandInfo
 - Config: YamlConfiguration
 - Cooldown: CooldownManager
 - Database: Schema based Database which supports (MySQL, MariaDB, Postgresql, SQLite, H2)
 - Editor: SolidityEditor
 - Exception: SolidityException
 - Listener: SolidityListener
 - Screen: InventoryManager, InventoryScreen, PaginatedInventoryScreen
 - Storage (For Screens): Storage, StorageManager, PlayerTemporaryStorage
 - Task: TaskChain
 - Discord: WebhookPayload, WebhookEmbed, Thumbnail, Image, Footer, Field, DiscordWebhook, Author
 - Prefix: PrefixManager
 - Utility: ChainedList, ListUtils, LocationUtils, SenderUtils, TimeParser, Validator
 - Main: SolidityMetadata, SolidityPlugin

## Version Compatibility

For this library, it'll only work on 1.21+ for now.

## License and Development

Developed by OSolidity Team, licensed under the MIT license.