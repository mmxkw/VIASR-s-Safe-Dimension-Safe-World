package cn.blockforge.generated.y65hbmcehbmapi2000;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

/**
 * 跨维度传送器：把玩家放到资源平原维度中心上方的安全落点。
 */
public class TeleporterSafe implements ITeleporter {

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        if (world.provider.getDimension() == ModDimension.DIM_ID) {
            net.minecraft.util.math.BlockPos spawn = ModDimension.computeSpawnPos(world);
            entity.setPosition(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        }
    }

    @Override
    public boolean isVanilla() {
        return false;
    }
}
