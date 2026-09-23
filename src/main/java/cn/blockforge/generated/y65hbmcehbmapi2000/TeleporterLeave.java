package cn.blockforge.generated.y65hbmcehbmapi2000;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

/**
 * 离开资源维度、回到主世界的传送器：把玩家放到返回落点（主世界的床 / 世界出生点）。
 *
 * <p>落点由 {@link CommandDimLeave} 在构造时传入，这里只负责在玩家进入主世界时
 * 把它放到该位置（+0.5 落在方块中心）。
 */
public class TeleporterLeave implements ITeleporter {

    private final BlockPos target;

    public TeleporterLeave(BlockPos target) {
        this.target = target;
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        if (target != null) {
            entity.setPosition(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        }
    }

    @Override
    public boolean isVanilla() {
        return false;
    }
}
