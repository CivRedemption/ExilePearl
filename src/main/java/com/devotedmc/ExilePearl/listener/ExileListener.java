package com.devotedmc.ExilePearl.listener;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlConfig;
import com.devotedmc.ExilePearl.event.ExilePearlEvent;
import com.devotedmc.ExilePearl.event.ExilePearlEvent.Type;
import com.devotedmc.ExilePearl.util.Guard;

import vg.civcraft.mc.citadel.events.ReinforcementDamageEvent;

/**
 * Listener for disallowing certain actions of exiled players
 * @author Gordon
 * 
 * Loss of privileges (for Devoted) when exiled, each of these needs to be toggleable in the config:
 * Cannot break reinforced blocks. (Citadel can stop damage, use that)
 * Cannot break bastions by placing blocks. (Might need bastion change)
 * Cannot throw ender pearls at all. 
 * Cannot enter a bastion field they are not on. Same teleport back feature as world border.
 * Cannot do damage to other players.
 * Cannot light fires.
 * Cannot light TNT.
 * Cannot chat in local chat. Given a message suggesting chatting in a group chat in Citadel.
 * Cannot use water or lava buckets.
 * Cannot use any potions.
 * Cannot set a bed.
 * Cannot enter within 1k of their ExilePearl. Same teleport back feature as world border.
 * Can use a /suicide command after a 180 second timeout. (In case they get stuck in a reinforced box).
 * Cannot place snitch or note-block.
 * Exiled players can still play, mine, enchant, trade, grind, and explore.
 *
 */
public class ExileListener implements Listener {

	private final ExilePearlApi pearlApi;
	private final PearlConfig config;
	
	/**
	 * Creates a new ExileListener instance
	 * @param logger The logger instance
	 * @param pearls The pearl manger
	 * @param config The plugin configuration
	 */
	public ExileListener(final ExilePearlApi pearlApi) {
		Guard.ArgumentNotNull(pearlApi, "pearlApi");
		
		this.pearlApi = pearlApi;
		this.config = pearlApi.getPearlConfig();
	}
	
	
	/**
	 * Clears the bed of newly exiled players
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void exileRuleClearBed(ExilePearlEvent e) {
		if (config.getRuleCanUseBed()) {
			return;
		}
		
		if (e.getType() == Type.NEW) {
			e.getExilePearl().getPlayer().setBedSpawnLocation(null, true);
		}
	}

	/**
	 * Prevent exiled players from using a bed
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerEnterBed(PlayerBedEnterEvent e) {
		checkAndCancelRule(ExileRule.USE_BED, e, e.getPlayer());
	}
	
	/**
	 * Prevent exiled players from using buckets
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerFillBucket(PlayerBucketFillEvent e) {
		checkAndCancelRule(ExileRule.USE_BUCKET, e, e.getPlayer());
	}
	
	/**
	 * Prevent exiled players from using buckets
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerEmptyBucket(PlayerBucketEmptyEvent e) {
		checkAndCancelRule(ExileRule.USE_BUCKET, e, e.getPlayer());
	}
	
	/**
	 * Prevent exiled players from using local chat
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		
		// TODO check chat channel
		checkAndCancelRule(ExileRule.CHAT, e, e.getPlayer());
	}
	
	/**
	 * Prevent exiled players from using brewing stands
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.hasBlock()) {
			if (e.getClickedBlock().getType().equals(Material.BREWING_STAND)) {
				checkAndCancelRule(ExileRule.BREW, e, e.getPlayer());
			}
		}
	}
	
	/**
	 * Prevent exiled players from enchanting
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerEnchant(EnchantItemEvent e) {
		checkAndCancelRule(ExileRule.ENCHANT, e, e.getEnchanter());
	}
	
	/**
	 * Prevent exiled players from pvping
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerPvp(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player && e.getDamager() instanceof Player)) {
			return;
		}
		
		checkAndCancelRule(ExileRule.PVP, e, (Player)e.getDamager());
	}
	
	/**
	 * Prevent exiled players from drinking potions
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDrinkPotion(PlayerItemConsumeEvent e) {
		if(e.getItem().getType() == Material.POTION) {
			checkAndCancelRule(ExileRule.USE_POTIONS, e, e.getPlayer());
		}
	}
	
	/**
	 * Prevent exiled players from using splash potions
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerThrowPotion(PotionSplashEvent e) {
		checkAndCancelRule(ExileRule.USE_POTIONS, e, (Player)e.getEntity().getShooter());
	}
	
	/**
	 * Prevents exiled players from breaking blocks
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerIgnite(BlockIgniteEvent e) {
		if (e.getCause() == IgniteCause.FLINT_AND_STEEL) {
			checkAndCancelRule(ExileRule.IGNITE, e, e.getPlayer());
		}
	}
	
	
	/**
	 * Prevents exiled players from breaking blocks
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		checkAndCancelRule(ExileRule.MINE, e, e.getPlayer());
	}
	
	/**
	 * Prevents exiled players from damaging reinforcements
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onReinforcementDamage(ReinforcementDamageEvent e) {
		checkAndCancelRule(ExileRule.DAMAGE_REINFORCEMENT, e, e.getPlayer());
	}
	
	
	/**
	 * Prevents exiled players from placing snitches
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onReinforcementDamage(BlockPlaceEvent e) {
		Material m = e.getBlockPlaced().getType();
		if (m == Material.JUKEBOX || m == Material.NOTE_BLOCK) {
			checkAndCancelRule(ExileRule.SNITCH, e, e.getPlayer());
		}
	}
	  
	
	
	/**
	 * Gets whether a rule is active for the given player
	 * @param rule The exile rule
	 * @param playerId The player to check
	 * @return true if the rule is active for the player
	 */
	private boolean isRuleActive(ExileRule rule, UUID playerId) {
		return config.isRuleSet(rule) && pearlApi.isPlayerExiled(playerId);
	}
	
	
	/**
	 * Checks if a rule is active for a given player and cancels it
	 * @param rule The rule to check
	 * @param event The event
	 * @param player The player to check
	 */
	private void checkAndCancelRule(ExileRule rule, Cancellable event, Player player) {
		if (event == null || player == null) {
			return;
		}
		
		UUID playerId = player.getUniqueId();
		if (isRuleActive(rule, playerId)) {
			((Cancellable)event).setCancelled(true);
			pearlApi.getPearlPlayer(playerId).msg(Lang.ruleCantDoThat, rule.getActionString());
		}
	}
}
