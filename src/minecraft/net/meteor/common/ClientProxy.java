package net.meteor.common;

import java.util.List;

import net.meteor.common.entity.EntityAlienCreeper;
import net.meteor.common.entity.EntityCometKitty;
import net.meteor.common.entity.EntityMeteor;
import net.meteor.common.entity.EntitySummoner;
import net.meteor.common.tileentity.TileEntityMeteorShield;
import net.meteor.common.tileentity.TileEntityMeteorTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.entity.RenderOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.src.meteor.HandlerTextures;
import net.minecraft.src.meteor.MetCrashSoundHandler;
import net.minecraft.src.meteor.effect.EntityFrezaDustFX;
import net.minecraft.src.meteor.effect.EntityMeteorShieldParticleFX;
import net.minecraft.src.meteor.effect.EntityMeteordustFX;
import net.minecraft.src.meteor.model.ModelCometKitty;
import net.minecraft.src.meteor.render.RenderAlienCreeper;
import net.minecraft.src.meteor.render.RenderMeteor;
import net.minecraft.src.meteor.render.RenderSummoner;
import net.minecraft.src.meteor.render.tileentity.TileEntityMeteorShieldRayRenderer;
import net.minecraft.src.meteor.render.tileentity.TileEntityMeteorTimerRenderer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
	
	@Override
	public void registerTileEntities()
	{
		TileEntityMeteorShieldRayRenderer tileRend = new TileEntityMeteorShieldRayRenderer();
		ClientRegistry.registerTileEntity(TileEntityMeteorShield.class, "TileEntityMeteorShield", tileRend);
		TileEntityMeteorTimerRenderer timerRend = new TileEntityMeteorTimerRenderer();
		ClientRegistry.registerTileEntity(TileEntityMeteorTimer.class, "TileEntityMeteorTimer", timerRend);
	}

	@Override
	public void loadStuff()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityMeteor.class, new RenderMeteor());
		RenderingRegistry.registerEntityRenderingHandler(EntityAlienCreeper.class, new RenderAlienCreeper());
		RenderingRegistry.registerEntityRenderingHandler(EntityCometKitty.class, new RenderOcelot(new ModelCometKitty(), 0.4F));
		RenderingRegistry.registerEntityRenderingHandler(EntitySummoner.class, new RenderSummoner());
	}
	
	@Override
	public void regTextures() {
		MinecraftForge.EVENT_BUS.register(new HandlerTextures());
	}

	@Override
	public void loadSounds()
	{
		MinecraftForge.EVENT_BUS.register(new MetCrashSoundHandler());
	}

	@Override
	public void playCrashSound(World world, EntityMeteor meteor)
	{
		EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
		if (player.worldObj.getWorldInfo().getDimension() != 0) return;
		double xDiff = meteor.posX - player.posX;
		double zDiff = meteor.posZ - player.posZ;
		double xMod = xDiff / 128.0D * 4.0D;
		double zMod = zDiff / 128.0D * 4.0D;
		world.playSoundEffect(player.posX + xMod, player.posY + 1.0D, player.posZ + zMod, "meteor.crash", 1.0F, 1.0F);
	}

	@Override
	public void meteorProtectCheck(String owner)
	{
		if (FMLClientHandler.instance().getClient().thePlayer.username.equalsIgnoreCase(owner)) {
			FMLClientHandler.instance().getClient().thePlayer.addChatMessage("\247a" + LangLocalization.get("MeteorShield.meteorBlocked"));
			FMLClientHandler.instance().getClient().thePlayer.addStat(HandlerAchievement.meteorBlocked, 1);
		}
	}

	@Override
	public void updateMeteorBlockAch(World world)
	{
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		ChunkCoordIntPair chunkCoords = world.getChunkFromBlockCoords((int)player.posX, (int)player.posZ).getChunkCoordIntPair();
		List lCoords = MeteorsMod.proxy.meteorHandler.safeChunksWithOwners;
		for (int i = 0; i < lCoords.size(); i++) {
			SafeChunkCoordsIntPair coords = (SafeChunkCoordsIntPair)lCoords.get(i);
			if ((coords.hasCoords(chunkCoords.chunkXPos, chunkCoords.chunkZPos)) && (coords.getOwner().equalsIgnoreCase(player.username))) {
				Minecraft.getMinecraft().thePlayer.addChatMessage("\247a" + LangLocalization.get("MeteorShield.meteorBlocked"));
				Minecraft.getMinecraft().thePlayer.addStat(HandlerAchievement.meteorBlocked, 1);
				break;
			}
		}
	}

	@Override
	public void sendPortalCreationMessage()
	{
		FMLClientHandler.instance().getClient().thePlayer.addChatMessage("\2470" + LangLocalization.get("Meteor.netherPortalCreation"));
	}

	public static void spawnParticle(String s, double d, double d1, double d2, double d3, double d4, double d5, World worldObj, int opt)
	{
		Minecraft mc = Minecraft.getMinecraft();
		if (mc == null || mc.renderViewEntity == null || mc.effectRenderer == null) {
			return;
		}
		int i = mc.gameSettings.particleSetting;
		if (i == 1 && worldObj.rand.nextInt(3) == 0) {
			i = 2;
		}
		double d6 = mc.renderViewEntity.posX - d;
		double d7 = mc.renderViewEntity.posY - d1;
		double d8 = mc.renderViewEntity.posZ - d2;
		EntityFX obj = null;
		double d9 = 16D;
		if (d6 * d6 + d7 * d7 + d8 * d8 > d9 * d9) {
			return;
		}
		if (i > 1) {
			return;
		}
		if (s.equals("meteordust"))
			obj = new EntityMeteordustFX(worldObj, d, d1, d2, (float)d3, (float)d4, (float)d5);
		else if (s.equals("frezadust"))
			obj = new EntityFrezaDustFX(worldObj, d, d1, d2, (float)d3, (float)d4, (float)d5);
		else if (s.equals("meteorshield")) {
			if (opt != -1)
				obj = new EntityMeteorShieldParticleFX(worldObj, d, d1, d2, d3, d4, d5, opt);
			else {
				obj = new EntityMeteorShieldParticleFX(worldObj, d, d1, d2, d3, d4, d5);
			}
		}
		if (obj != null) {
			mc.effectRenderer.addEffect((EntityFX)obj);
		}
	}
}