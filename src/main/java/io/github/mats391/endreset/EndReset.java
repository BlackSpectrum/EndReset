package io.github.mats391.endreset;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EndReset extends JavaPlugin implements Listener
{


	public static FileConfiguration	config;
	private final long				MS_IN_DAY	= 86400000;




	public static World getWorld( final String name ) {
		return Bukkit.getServer().getWorld( name );
	}




	@Override
	public boolean onCommand( final CommandSender sender, final Command cmd, final String commandLabel, final String[] args ) {
		if ( cmd.getName().equalsIgnoreCase( "resetend" ) )
		{
			this.resetEnd();
			return true;
		}

		return false;
	}




	@Override
	public void onDisable() {

		// Only when there are no players left
		if ( this.getServer().getOnlinePlayers().length > 0 )
			return;

		if ( config.getLong( "nextReset" ) != 0 && System.currentTimeMillis() > config.getLong( "nextReset" ) )
			this.resetEnd();
	}




	@Override
	public void onEnable() {

		this.setUpConfig();
		this.getServer().getPluginManager().registerEvents( this, this );
	}




	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDie( final EntityDeathEvent event ) {
		final Entity e = event.getEntity();

		if ( e.getType().equals( EntityType.ENDER_DRAGON ) )
			this.scheduleReset();
	}




	private void resetEnd() {
		final World end = getWorld( "world_the_end" );
		for ( int a = -32; a <= 31; a++ )
			for ( int i = -32; i <= 31; i++ )
				if ( end.loadChunk( a, i, false ) )
					if ( end.regenerateChunk( a, i ) )
					{
						end.refreshChunk( a, i );
						end.unloadChunkRequest( a, i );
					}

		config.set( "nextReset", 0 );
		this.saveConfig();
	}




	private void scheduleReset() {
		final Random rand = new Random();

		final int maxDays = config.getInt( "maxDays" ) - config.getInt( "minDays" );
		final int minDays = config.getInt( "minDays" );

		long nextReset = System.currentTimeMillis() + minDays * this.MS_IN_DAY;

		if ( maxDays > 0 )
			nextReset += rand.nextInt( maxDays ) * this.MS_IN_DAY;

		config.set( "nextReset", nextReset );
		this.saveConfig();
	}




	private void setUpConfig() {
		config = this.getConfig();

		config.set( "nextReset", config.getLong( "nextReset", 0 ) );
		config.set( "minDays", config.getInt( "minDays", 5 ) );
		config.set( "maxDays", config.getInt( "maxDays", 15 ) );

		this.saveConfig();
	}
}
