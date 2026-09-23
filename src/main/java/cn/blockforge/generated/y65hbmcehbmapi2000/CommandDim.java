package cn.blockforge.generated.y65hbmcehbmapi2000;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * 资源维度统一命令：{@code /dim <tp|leave|list>}。
 *
 * <p><b>传送记忆</b>：
 * <ul>
 *   <li>{@code /dim tp} 会先记录玩家<b>在主世界</b>的 X/Z 坐标（若他在主世界），
 *       然后传送到资源维度。若玩家曾用 {@code /dim leave} 离开过资源维度，
 *       则返回他上次离开时的 X/Z；否则传送到维度原点。</li>
 *   <li>{@code /dim leave} 会先记录玩家在<b>资源维度</b>的 X/Z 坐标，
 *       然后传送到他上次用 {@code /dim tp} 进入资源维度前的主世界 X/Z；
 *       若从未记录过，则回落到主世界原点。</li>
 * </ul>
 *
 * <p><b>Y 坐标</b>：只记录 X/Z，Y 每次由目标世界的地表高度动态计算。
 * 这样无论中间地形如何变化（被玩家挖掉、被其它模组改过），传送落点始终位于
 * 地表上方第一个空气位置，不会出现卡进方块、悬空或掉进虚空。
 *
 * <p>两个记忆位置分别用 NBT 键
 * {@value #NBT_LAST_OVERWORLD} / {@value #NBT_LAST_DIM}
 * 保存在玩家的持久化数据中，随玩家存档自动持久化，重启服务器不丢失。
 */
public class CommandDim extends CommandBase {

    // =========================================================================
    //  常量
    // =========================================================================

    private static final String SUB_TP    = "tp";
    private static final String SUB_LEAVE = "leave";
    private static final String SUB_LIST  = "list";

    /** list 子命令所需权限等级（OP）。 */
    private static final int LIST_PERMISSION = 2;

    // =========================================================================
    //  传送记忆的 NBT 键名
    // =========================================================================

    /** 玩家在主世界的 X/Z（/dim tp 时写入）。 */
    private static final String NBT_LAST_OVERWORLD = "viasr_last_overworld";

    /** 玩家在资源维度的 X/Z（/dim leave 时写入）。 */
    private static final String NBT_LAST_DIM = "viasr_last_dim";

    // =========================================================================
    //  命令元信息
    // =========================================================================

    @Override
    public String getName() {
        return "dim";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/dim <tp|leave|list>";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("dimension", "resdim");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    // =========================================================================
    //  主入口
    // =========================================================================

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            printUsage(sender);
            return;
        }

        String sub = args[0].toLowerCase();
        String[] rest = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        switch (sub) {
            case SUB_TP:
                runTp(server, sender, rest);
                break;
            case SUB_LEAVE:
                runLeave(server, sender, rest);
                break;
            case SUB_LIST:
                runList(server, sender, rest);
                break;
            default:
                throw new CommandException(
                        "未知子命令：" + sub + "。用法：" + getUsage(sender));
        }
    }

    private void printUsage(ICommandSender sender) {
        sender.sendMessage(new TextComponentString("用法："));
        sender.sendMessage(new TextComponentString("  /dim tp    — 传送进建家维度（返回上次离开的位置）"));
        sender.sendMessage(new TextComponentString("  /dim leave — 离开建家维度，回到进入前的主世界位置"));
        if (sender.canUseCommand(LIST_PERMISSION, getName())) {
            sender.sendMessage(new TextComponentString("  /dim list  — 查看建家维度内的玩家（OP）"));
        }
    }

    // =========================================================================
    //  子命令：/dim tp
    // =========================================================================

    /**
     * 传送玩家进入资源维度。
     *
     * <p>逻辑顺序：
     * <ol>
     *   <li>若玩家在主世界，记录其当前 X/Z 到 {@link #NBT_LAST_OVERWORLD}；</li>
     *   <li>决定目标 X/Z：有 {@link #NBT_LAST_DIM} 记忆则用之，否则原点 (0, 0)；</li>
     *   <li>由目标世界地表高度计算安全 Y；</li>
     *   <li>执行跨维度传送并同步客户端。</li>
     * </ol>
     */
    private void runTp(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = CommandBase.getCommandSenderAsPlayer(sender);
        int dimId = ModDimension.DIM_ID;

        // ---- 1. 记录主世界位置（仅当玩家确实在主世界时）----
        if (player.dimension == 0) {
            saveXZ(player, NBT_LAST_OVERWORLD, player.getPosition());
        }

        // ---- 2. 决定目标 X/Z ----
        BlockPos mem = loadXZ(player, NBT_LAST_DIM);
        int targetX = (mem != null) ? mem.getX() : 0;
        int targetZ = (mem != null) ? mem.getZ() : 0;
        boolean usedMemory = (mem != null);

        // ---- 3. 计算安全 Y ----
        World targetWorld = server.getWorld(dimId);
        int targetY = findSafeY(targetWorld, targetX, targetZ);
        BlockPos target = new BlockPos(targetX, targetY, targetZ);

        double x = target.getX() + 0.5;
        double y = target.getY();
        double z = target.getZ() + 0.5;

        // ---- 4. 执行传送 ----
        if (player.dimension != dimId) {
            server.getPlayerList().transferPlayerToDimension(player, dimId, new TeleporterSafe());
        }
        player.setPositionAndUpdate(x, y, z);

        if (player.connection != null) {
            player.connection.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
        }

        // ---- 5. 反馈 ----
        if (usedMemory) {
            sender.sendMessage(new TextComponentString(String.format(
                    "已进入建家维度，返回上次位置 (%d, %d, %d)。",
                    target.getX(), target.getY(), target.getZ())));
        } else {
            sender.sendMessage(new TextComponentString(String.format(
                    "已进入建家维度（首次，落点为维度原点 %d, %d, %d）。",
                    target.getX(), target.getY(), target.getZ())));
        }
    }

    // =========================================================================
    //  子命令：/dim leave
    // =========================================================================

    /**
     * 传送玩家离开资源维度，回到主世界。
     *
     * <p>逻辑顺序：
     * <ol>
     *   <li>若玩家在资源维度，记录其当前 X/Z 到 {@link #NBT_LAST_DIM}；</li>
     *   <li>决定目标 X/Z：有 {@link #NBT_LAST_OVERWORLD} 记忆则用之，
     *       否则原点 (0, 0)；</li>
     *   <li>由主世界地表高度计算安全 Y；</li>
     *   <li>执行跨维度传送并同步客户端。</li>
     * </ol>
     */
    private void runLeave(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = CommandBase.getCommandSenderAsPlayer(sender);

        if (player.dimension != ModDimension.DIM_ID) {
            sender.sendMessage(new TextComponentString(
                    "你当前不在建家维度里，无需离开。进入建家维度请用 /dim tp。"));
            return;
        }

        // ---- 1. 记录资源维度位置 ----
        saveXZ(player, NBT_LAST_DIM, player.getPosition());

        // ---- 2. 决定返回主世界的 X/Z ----
        BlockPos mem = loadXZ(player, NBT_LAST_OVERWORLD);
        int targetX = (mem != null) ? mem.getX() : 0;
        int targetZ = (mem != null) ? mem.getZ() : 0;
        boolean usedMemory = (mem != null);

        // ---- 3. 计算安全 Y ----
        World overworld = server.getWorld(0);
        int targetY = findSafeY(overworld, targetX, targetZ);
        BlockPos target = new BlockPos(targetX, targetY, targetZ);

        // ---- 4. 执行传送 ----
        server.getPlayerList().transferPlayerToDimension(player, 0, new TeleporterLeave(target));

        double x = target.getX() + 0.5;
        double y = target.getY();
        double z = target.getZ() + 0.5;
        player.setPositionAndUpdate(x, y, z);

        if (player.connection != null) {
            player.connection.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
        }

        // ---- 5. 反馈 ----
        if (usedMemory) {
            sender.sendMessage(new TextComponentString(String.format(
                    "已离开建家维度，回到主世界位置 (%d, %d, %d)。",
                    target.getX(), target.getY(), target.getZ())));
        } else {
            sender.sendMessage(new TextComponentString(String.format(
                    "已离开建家维度（无进入记录，回到主世界原点 %d, %d, %d）。",
                    target.getX(), target.getY(), target.getZ())));
        }
    }

    // =========================================================================
    //  子命令：/dim list
    // =========================================================================

    private void runList(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!sender.canUseCommand(LIST_PERMISSION, getName())) {
            throw new CommandException("你没有权限使用该子命令。");
        }

        List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
        StringBuilder names = new StringBuilder();
        int count = 0;
        for (EntityPlayerMP p : players) {
            if (p.dimension == ModDimension.DIM_ID) {
                if (count > 0) {
                    names.append("、");
                }
                names.append(p.getName());
                count++;
            }
        }

        if (count == 0) {
            sender.sendMessage(new TextComponentString("建家维度当前没有玩家。"));
        } else {
            sender.sendMessage(new TextComponentString(
                    "建家维度当前有 " + count + " 名玩家：" + names.toString()));
        }
    }

    // =========================================================================
    //  安全高度
    // =========================================================================

    /**
     * 计算指定 X/Z 处的安全站立 Y 坐标。
     *
     * <p>算法：
     * <ol>
     *   <li>强制加载目标区块，保证高度查询准确（否则可能返回 0）；</li>
     *   <li>调用 {@link World#getHeight(int, int)}——返回该列最高非空气方块
     *       之上的第一个 Y，即玩家脚部应站的位置；</li>
     *   <li>钳制到世界合法范围 [1, 254]，防止原版边界情况。</li>
     * </ol>
     *
     * <p>注意：如果地表是水/岩浆，玩家会直接落到液体表面。
     * 这是可接受的——MC 自身的落地逻辑会让玩家浮在水面、被岩浆伤害，
     * 与原版其它传送（例如末地门、下界门）行为一致。
     *
     * @param world 目标世界
     * @param x     目标 X
     * @param z     目标 Z
     * @return 安全站立 Y
     */
    private static int findSafeY(World world, int x, int z) {
        // 强制加载目标区块，避免 getHeight 返回 0
        world.getChunk(x >> 4, z >> 4);

        int y = world.getHeight(x, z);

        // 钳制到世界合法范围
        if (y < 1) {
            y = 1;
        } else if (y > 254) {
            y = 254;
        }
        return y;
    }

    // =========================================================================
    //  传送记忆：NBT 读写（仅 X、Z）
    // =========================================================================

    /**
     * 把玩家的当前 X/Z 写入持久化 NBT。
     *
     * <p>只存 X/Z，不存 Y —— Y 每次由 {@link #findSafeY} 动态计算，
     * 从而在目标地形变化后依然能正确落地。
     */
    private static void saveXZ(EntityPlayer player, String key, BlockPos pos) {
        NBTTagCompound data = player.getEntityData();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("x", pos.getX());
        tag.setInteger("z", pos.getZ());
        data.setTag(key, tag);
    }

    /**
     * 读取玩家持久化 NBT 中的 X/Z。
     *
     * @return 保存的 X/Z（Y 恒为 0，仅作占位）；从未写入时返回 {@code null}
     */
    private static BlockPos loadXZ(EntityPlayer player, String key) {
        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey(key)) {
            return null;
        }
        NBTTagCompound tag = data.getCompoundTag(key);
        return new BlockPos(
                tag.getInteger("x"),
                0,
                tag.getInteger("z"));
    }

    // =========================================================================
    //  Tab 补全
    // =========================================================================

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
                                          String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add(SUB_TP);
            subs.add(SUB_LEAVE);
            if (sender.canUseCommand(LIST_PERMISSION, getName())) {
                subs.add(SUB_LIST);
            }
            return getListOfStringsMatchingLastWord(args, subs);
        }
        return Collections.emptyList();
    }
}