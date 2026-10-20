package am2.blocks.tileentities;

import am2.blocks.BlocksCommonProxy;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

public class TileEntityInfusedStem extends TileEntity {

    public List<EntityLiving> killedEntities = new ArrayList<EntityLiving>();
    EntityAnimal[] bredEntities = new EntityAnimal[5];
    private int tick = 0;

    public void updateEntity() {
        tick++;
        List<EntityAnimal> breedablesInRange = this.worldObj.getEntitiesWithinAABB(EntityAnimal.class, AxisAlignedBB.getBoundingBox(this.xCoord - 6, this.yCoord - 6, this.zCoord - 6, this.xCoord + 6, this.yCoord + 6, this.zCoord + 6));

        int current = 0;
        for (int j = 0; j < breedablesInRange.size(); j++) {
            if (bredEntities[4] != null) break;
            if (bredEntities[current] == null) {
                if (breedablesInRange.get(j).isInLove()) {
                    bredEntities[current] = breedablesInRange.get(j);
                    current++;
                } else continue;
            } else {
                current++;
                j--; // we want to try again with the same one since occupied
            }
        }

        if (this.worldObj != null && tick >= 200) {
            tick = 0;

            int variety = 0;
            for (int i = 0; i < killedEntities.size(); i++) {
                if (killedEntities.get(i) == null) break;
                if (i == 0 || (killedEntities.get(i).getCommandSenderName() != killedEntities.get(max(0, i-1)).getCommandSenderName() && killedEntities.get(i).getCommandSenderName() != killedEntities.get(max(0, i-2)).getCommandSenderName())) variety++;
                if (variety >= 3 && i == 4) {
                    this.spawnParticle(false);
                    this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "ambient.weather.thunder", 10000.0F, 0.8F + this.worldObj.rand.nextFloat() * 0.2F);
                    this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, BlocksCommonProxy.sanguineAmaryllis);
                    return;
                }
            }
            for (int i = 0; i < 5; i++) {
                if (bredEntities[i] == null) break;
                if (i == 4) {
                    this.spawnParticle(true);
                    this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "ambient.weather.thunder", 10000.0F, 0.8F + this.worldObj.rand.nextFloat() * 0.2F);
                    this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, BlocksCommonProxy.effervescentSnowdrop);
                    return;
                }
            }
            killedEntities.clear();
            for (int i = 0; i < 5; i++) {
                if(bredEntities[i] != null) {
                    bredEntities[i].resetInLove(); // clear if conditions not met
                    bredEntities[i] = null;
                }
            }
        }
        super.updateEntity();
    }

    private void spawnParticle(boolean isSnowdrop) {
        String particle = isSnowdrop ? "largesmoke" : "smoke";
        String secondaryparticle = isSnowdrop ? "portal" : "flame";
        this.worldObj.spawnParticle(particle, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, 0.1D, 0.1D, 0.1D);
        this.worldObj.spawnParticle(secondaryparticle, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, 0.0D, 0.0D, 0.0D);
        this.worldObj.spawnParticle(particle, this.xCoord + 0.5F, this.yCoord + 0.4F, this.zCoord + 0.5F, 0.0D, 0.0D, 0.0D);
        this.worldObj.spawnParticle(secondaryparticle, this.xCoord + 0.4F, this.yCoord + 0.5F, this.zCoord + 0.5F, 0.0D, 0.0D, 0.0D);
        this.worldObj.spawnParticle(particle, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.4F, 0.0D, 0.0D, 0.0D);
        this.worldObj.spawnParticle(secondaryparticle, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, 0.0D, 0.0D, 0.0D);
        this.worldObj.spawnParticle(particle, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, 0.0D, 0.0D, 0.0D);
        this.worldObj.spawnParticle(secondaryparticle, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, 0.0D, 0.0D, 0.0D);
        this.worldObj.spawnParticle(particle, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, 0.0D, 0.0D, 0.0D);
        this.worldObj.spawnParticle(secondaryparticle, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, 0.0D, 0.0D, 0.0D);
        this.worldObj.spawnParticle(particle, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, 0.0D, 0.0D, 0.0D);
        this.worldObj.spawnParticle(secondaryparticle, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, 0.0D, 0.0D, 0.0D);
        this.worldObj.spawnParticle(particle, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, 0.0D, 0.0D, 0.0D);
        this.worldObj.spawnParticle(secondaryparticle, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, 0.0D, 0.0D, 0.0D);
    }
}
