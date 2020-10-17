package com.wumbo.queryengine;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;

import net.runelite.api.events.GameObjectChanged;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.Tile;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.NPC;
import net.runelite.api.NullNpcID;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import static net.runelite.api.Skill.AGILITY;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.DecorativeObjectChanged;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectChanged;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.WallObjectChanged;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Extension
@PluginDescriptor(
	name = "Java example",
	description = "Java example",
	type = PluginType.MISCELLANEOUS
)
@Slf4j
public class QueryEnginePlugin extends Plugin
{
	// Injects our config
	@Inject
	private QueryEngineConfig config;

	@Getter(AccessLevel.PACKAGE)
	private final Map<Integer, TileObject> objects = new HashMap<>();

	@Inject
	private QueryEngineServer server;

	// Provides our config
	@Provides
	QueryEngineConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(QueryEngineConfig.class);
	}

	@Override
	protected void startUp()
	{
		// runs on plugin startup
		log.info("Plugin started");

		// example how to use config items
		if (config.example())
		{
			// do stuff
			log.info("The value of 'config.example()' is ${config.example()}");
		}

		try {
			this.server.start();
		} catch(IOException e) {
			log.error("unable to start grpc server");
			e.printStackTrace();
		}
	}

	@Override
	protected void shutDown()
	{
		// runs on plugin shutdown
		log.info("Plugin stopped");
	}

	@Subscribe
	private void onGameTick(GameTick gameTick)
	{
		// runs every gametick
		log.info("Gametick");
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event)
	{
		onTileObject(event.getTile(), null, event.getGameObject());
	}

	@Subscribe
	private void onGameObjectChanged(GameObjectChanged event)
	{
		onTileObject(event.getTile(), event.getPrevious(), event.getGameObject());
	}

	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned event)
	{
		onTileObject(event.getTile(), event.getGameObject(), null);
	}

	@Subscribe
	private void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		onTileObject(event.getTile(), null, event.getGroundObject());
	}

	@Subscribe
	private void onGroundObjectChanged(GroundObjectChanged event)
	{
		onTileObject(event.getTile(), event.getPrevious(), event.getGroundObject());
	}

	@Subscribe
	private void onGroundObjectDespawned(GroundObjectDespawned event)
	{
		onTileObject(event.getTile(), event.getGroundObject(), null);
	}

	@Subscribe
	private void onWallObjectSpawned(WallObjectSpawned event)
	{
		onTileObject(event.getTile(), null, event.getWallObject());
	}

	@Subscribe
	private void onWallObjectChanged(WallObjectChanged event)
	{
		onTileObject(event.getTile(), event.getPrevious(), event.getWallObject());
	}

	@Subscribe
	private void onWallObjectDespawned(WallObjectDespawned event)
	{
		onTileObject(event.getTile(), event.getWallObject(), null);
	}

	@Subscribe
	private void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		onTileObject(event.getTile(), null, event.getDecorativeObject());
	}

	@Subscribe
	private void onDecorativeObjectChanged(DecorativeObjectChanged event)
	{
		onTileObject(event.getTile(), event.getPrevious(), event.getDecorativeObject());
	}

	@Subscribe
	private void onDecorativeObjectDespawned(DecorativeObjectDespawned event)
	{
		onTileObject(event.getTile(), event.getDecorativeObject(), null);
	}

	private void onTileObject(Tile tile, TileObject oldObject, TileObject newObject) {
		if (newObject == null) {
			objects.remove(oldObject.getId());
		} else if (oldObject == null) {
			objects.put(newObject.getId(), newObject);
		} else {
			objects.remove(oldObject.getId());
			objects.put(newObject.getId(), newObject);
		}
	}
}