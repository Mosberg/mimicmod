# Mimic Mod

![Mimic Mod Icon](src/main/resources/assets/mimicmod/icon.png)

A Minecraft mod that adds deceptive chest mimics with configurable difficulty, multiple variants, and custom loot systems.

## Features

- **Multiple Mimic Variants**: Classic, Corrupted, Ender, and seasonal Christmas mimics
- **Dynamic Difficulty Scaling**: Health and damage scale based on world difficulty, biome, and variant
- **Configurable Behavior**: Extensive JSON configuration for spawn rates, stats, and loot
- **Custom Loot Tables**: Unique drops including Mimic Tooth and Cursed Pages
- **Advancement System**: Progressive challenges for defeating different mimic types
- **Immersive Audio**: Custom sounds for idle, reveal, and attack states

## Requirements

- Minecraft 1.21.10
- Fabric Loader 0.18.1
- Fabric API 0.138.3+1.21.10
- Java 21

## Installation

1. Download the latest release from the [Releases page]
2. Place the JAR file in your `mods` folder
3. Launch Minecraft with Fabric

## Configuration

Configuration file is automatically generated at `config/mimicmod.json` on first run.

### Key Configuration Options

```json
{
  "spawn_chance": 0.1,
  "combat_scaling": {
    "health_base": 24.0,
    "damage_base": 4.0
  }
}
```

See the [Configuration Guide](docs/CONFIGURATION.md) for detailed options.

## Variants

- **Classic**: Standard mimic found in most biomes
- **Corrupted**: Stronger variant common in dangerous biomes
- **Ender**: Rare fire-immune variant in the Deep Dark
- **Christmas**: Festive seasonal variant (very rare)

## Development

### Building from Source

```bash
./gradlew build
```

Built JAR will be in `build/libs/`

### Development Setup

```bash
./gradlew genSources
./gradlew idea # or eclipse
```

## Commands

### Spawn a mimic at your location

/mimic spawn [variant]
/mimic spawn classic
/mimic spawn corrupted
/mimic spawn ender
/mimic spawn christmas

### Get detailed info about a mimic

/mimic info <entity>
/mimic info @e[type=mimicmod:mimic,limit=1,sort=nearest]

### Change a mimic's variant

/mimic setvariant <entity> <variant>
/mimic setvariant @e[type=mimicmod:mimic,limit=1] corrupted

### Reveal or hide a mimic

/mimic reveal <entity>
/mimic hide <entity>

### Spawn multiple mimics in a circle

/mimic spawnmany unt> [variant]
/mimic spawnmany 10 classic
/mimic spawnmany 5 corrupted

### Kill all mimics in the world

/mimic killall

### Reload configuration

/mimic config reload

### Get current biome information

/mimic biome

## Credits

- **Author**: Mosberg
- **Fabric API**: Fabric Development Team
- **Minecraft**: Mojang Studios

## License

All Rights Reserved © 2025 Mosberg

## Support

- [Report Issues](https://github.com/mosberg/mimicmod/issues)
- [Documentation](https://mosberg.github.io/mimicmod)

---

**Happy hunting... or being hunted!** 🎒👾
