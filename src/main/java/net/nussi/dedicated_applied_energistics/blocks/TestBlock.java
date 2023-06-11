package net.nussi.dedicated_applied_energistics.blocks;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics;
import net.nussi.dedicated_applied_energistics.blockentities.TestBlockEntity;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = DedicatedAppliedEnegistics.MODID)
public class TestBlock extends Block implements EntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static HashMap<String, TestBlockEntity> blockEntities = new HashMap<>();

    public TestBlock(Properties properties) {
        super(properties);
    }

    public static String getIndex(BlockPos pos) {
        return pos.toShortString();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(blockEntities.containsKey(getIndex(pos))) {
            return blockEntities.get(getIndex(pos));
        } else {
            TestBlockEntity blockEntity = BlockEntityTypeInit.TEST_BLOCK_ENTITY_TYPE.get().create(pos, state);
            blockEntity.onCreate();
            blockEntities.put(getIndex(pos), blockEntity);
            return blockEntity;
        }
    }

    @Override
    public void onRemove(BlockState p_60515_, Level p_60516_, BlockPos pos, BlockState p_60518_, boolean p_60519_) {
        if(blockEntities.containsKey(getIndex(pos))) {
            TestBlockEntity blockEntity = blockEntities.remove(getIndex(pos));
            blockEntity.onDestroy();
        } else {
            throw new RuntimeException("When removing block at " + pos + " corresponding block entity wasn't found!");
        }
    }

//    @SubscribeEvent
//    public static void onChunkLoad(ChunkEvent.Load event) {
//        LOGGER.info("Chunk load");
//    }
//
//    @SubscribeEvent
//    public static void onChunkUnload(ChunkEvent.Unload event) {
//        LOGGER.info("Chunk unload");
//    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        for(TestBlockEntity blockEntity : blockEntities.values()) {
            blockEntity.onDestroy();
        }

        blockEntities = new HashMap<>();
    }

}
