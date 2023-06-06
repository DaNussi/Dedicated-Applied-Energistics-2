package net.nussi.dedicated_applied_energistics;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import net.nussi.dedicated_applied_energistics.init.BlockInit;
import net.nussi.dedicated_applied_energistics.init.CellInit;
import net.nussi.dedicated_applied_energistics.init.ItemInit;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

@Mod(DedicatedAppliedEnegistics.MODID)
public class DedicatedAppliedEnegistics
{
    public static final String MODID = "dae2";
    private static final Logger LOGGER = LogUtils.getLogger();

    public DedicatedAppliedEnegistics()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BlockInit.BLOCKS.register(modEventBus);
        ItemInit.ITEMS.register(modEventBus);
        BlockEntityTypeInit.BLOCK_ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(DedicatedAppliedEnegistics::commonSetup);
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        CellInit.Init();


        new Thread(() -> {
            new Jedis("localhost", 6379).subscribe(new JedisPubSub() {
                Jedis jedis = new Jedis("localhost", 6379);

                @Override
                public void onMessage(String channel, String message) {
                    try {
                        CompoundTag compoundTag = TagParser.parseTag(message);
                        String index = compoundTag.getString("Index");
                        compoundTag.remove("Index");

                        String UUID = compoundTag.getString("UUID");
                        compoundTag.remove("UUID");

                        Long newAmount = compoundTag.getLong("Amount");
                        if(jedis.exists(index)) {
                            CompoundTag currentTag = TagParser.parseTag(jedis.get(index));
                            newAmount += currentTag.getLong("Amount");
                        }
                        compoundTag.putLong("Amount", newAmount);

                        if(newAmount > 0) jedis.set(index, compoundTag.getAsString());
                        else jedis.del(index);
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, "0.inv");
        }).start();
    }

}
