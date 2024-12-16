<img align="right" src="https://i.imgur.com/okw2Gre.png" height="100" width="100">

[![Release](https://img.shields.io/github/v/release/Flioris/JVA?label=Release)](https://github.com/Flioris/JVA/releases)
[![discord-shield](https://discord.com/api/guilds/1045660297236582462/widget.png)](https://discord.gg/AZSZ8nhtra)
[![docs-shield](https://img.shields.io/badge/Wiki-Docs-blue.svg)](https://flioris.github.io/JVA/)

# JVA (Java VK API)

An open source Java wrapper for working with the VK Long Poll API. Provides the ability to work with VK methods and its events for group bots.

## ⬇️ Installation

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Flioris</groupId>
        <artifactId>JVA</artifactId>
        <version>0.1.7-alpha</version>
    </dependency>
</dependencies>
```

### Gradle

```gradle
repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.Flioris:JVA:0.1.7-alpha")
}
```

## 🚪️ Getting Started

Launch the bot and attach an event listener:

```java
public static void main(String[] args) {
    jva = new JVA(token, groupId);
    jva.setListeners(new MainListener());
}
```

Your event listener will look something like this:

```java
public class MainListener extends EventListener {
    
    @Override
    public void onCommand(CommandEvent event) {
        ...
    }
    
    @Override
    public void onMessage(MessageEvent event) {
        ...
    }
}
```

More details in [Javadoc](https://flioris.github.io/JVA/).
