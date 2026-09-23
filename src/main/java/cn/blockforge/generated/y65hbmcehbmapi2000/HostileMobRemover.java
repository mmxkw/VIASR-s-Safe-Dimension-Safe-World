package cn.blockforge.generated.y65hbmcehbmapi2000;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * 在资源维度中自动移除敌对生物。
 *
 * <p><b>功能</b>：
 * <ul>
 *   <li>{@link EntityJoinWorldEvent}：实体加入维度的瞬间拦截，
 *       覆盖自然刷新、刷怪笼、命令召唤、跨维度传送、区块重载；</li>
 *   <li>{@link TickEvent.WorldTickEvent}：每 5 秒扫描一次已加载实体，
 *       清理运行期切换开关后仍残留的旧生物，以及个别绕过事件的边界情况。</li>
 * </ul>
 *
 * <p><b>判断规则</b>：实体实现 {@link IMob} 且不在白名单中即被移除。
 * 玩家、动物、村民、铁傀儡、宠物不实现 IMob，天然不受影响。
 *
 * <p><b>注册</b>：必须在模组初始化时注册一次——
 * {@code MinecraftForge.EVENT_BUS.register(new HostileMobRemover());}
 * 忘记注册是最常见的失效原因。
 */
public class HostileMobRemover {

    // =========================================================================
    //  白名单缓存
    // =========================================================================

    private final Set<ResourceLocation> whitelist = new HashSet<>();

    public HostileMobRemover() {
        rebuildWhitelist();
        FMLLog.log.info("[HostileMobRemover] 已初始化，白名单条目数：{}", whitelist.size());
    }

    public void rebuildWhitelist() {
        whitelist.clear();
        String[] arr = DimConfig.removeHostileMobsWhitelist;
        if (arr == null) return;
        for (String s : arr) {
            if (s == null) continue;
            String trimmed = s.trim();
            if (trimmed.isEmpty()) continue;
            try {
                whitelist.add(new ResourceLocation(trimmed));
            } catch (Throwable t) {
                FMLLog.log.warn("[HostileMobRemover] 白名单条目无效：{}", trimmed);
            }
        }
    }

    // =========================================================================
    //  核心：实体加入世界时拦截
    // =========================================================================

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        // 只在服务端处理；客户端跟随同步
        if (event.getWorld().isRemote) return;

        // 总开关
        if (!DimConfig.removeHostileMobs) return;

        // 维度判断
        if (!isOurDimension(event.getWorld())) return;

        Entity entity = event.getEntity();
        if (shouldRemove(entity)) {
            event.setCanceled(true);
            entity.setDead();
            if (DimConfig.debugHostileMobRemover) {
                FMLLog.log.info("[HostileMobRemover] 拦截实体：{}",
                        EntityList.getKey(entity));
            }
        }
    }

    // =========================================================================
    //  可选：定期扫描已加载实体
    // =========================================================================

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isRemote) return;
        if (!DimConfig.removeHostileMobs) return;
        if (!isOurDimension(event.world)) return;

        // 每 100 tick（约 5 秒）扫一次
        if (event.world.getTotalWorldTime() % 100L != 0L) return;

        List<Entity> toRemove = new ArrayList<>();
        for (Entity e : event.world.loadedEntityList) {
            if (shouldRemove(e)) {
                toRemove.add(e);
            }
        }
        for (Entity e : toRemove) {
            e.setDead();
            if (DimConfig.debugHostileMobRemover) {
                FMLLog.log.info("[HostileMobRemover] 扫描清理实体：{}",
                        EntityList.getKey(e));
            }
        }
    }

    // =========================================================================
    //  判断逻辑
    // =========================================================================

    private boolean shouldRemove(Entity entity) {
        if (entity == null || entity.isDead) return false;
        if (entity instanceof EntityPlayer) return false;
        if (!(entity instanceof IMob)) return false;

        ResourceLocation id = EntityList.getKey(entity);
        if (id != null && whitelist.contains(id)) return false;

        return true;
    }

    /**
     * 维度判断：用<b>维度类型</b>而非 WorldProvider 类判断，更稳健。
     *
     * <p>原实现用 {@code world.provider instanceof WorldProviderModDim}，
     * 若维度提供者被其它模组包装（代理、子类），instanceof 会失败。
     * 改用 {@code DimensionType} 比对，只要维度类型一致就命中。
     */
    private boolean isOurDimension(World world) {
        if (world == null || world.provider == null) return false;
        try {
            return world.provider.getDimensionType() == ModDimension.getType();
        } catch (Throwable t) {
            // 兜底：回落到维度 ID 比对
            return world.provider.getDimension() == ModDimension.DIM_ID;
        }
    }
}