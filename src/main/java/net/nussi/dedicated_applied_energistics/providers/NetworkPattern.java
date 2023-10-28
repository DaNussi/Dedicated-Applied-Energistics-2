package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.level.Level;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.HashMap;
import java.util.UUID;

public class NetworkPattern extends DedicatedAppliedEnergisticsController.Controllable {
    public IPatternDetails pattern;
    public String origin;
    private Jedis jedis;
    private String uuid = UUID.randomUUID().toString();
    private Level level;
    public boolean exists;
    public boolean enableResponse = false;

    public NetworkPattern(IPatternDetails pattern, String origin, boolean exists, Level level) {
        this.pattern = pattern;
        this.origin = origin;
        this.level = level;
        this.exists = exists;

        DedicatedAppliedEnergisticsController.addControllable(this);
        if(DedicatedAppliedEnergisticsController.IsRunning && !this.isRunning()) this.externalStart();
    }

    @Override
    protected void onStart() {
        this.jedis = getJedis();

        this.jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if(!enableResponse) return;

                try {
                    CompoundTag compoundTag = TagParser.parseTag(message);
                    CompoundTag response = new CompoundTag();
                    response.putString("Type", "Response");

                    if(!compoundTag.getString("Type").equals("Request")) return;
                    String requestID = compoundTag.getString("RequestID");
                    response.putString("RequestID", requestID);

                    if(!compoundTag.contains("Method")) return;
                    String method = compoundTag.getString("Method");
                    response.putString("Method", method);


                    switch (method) {
                        case "IsValid":
                            AEKey input = AEKey.fromTagGeneric(compoundTag.getCompound("VAR_Input"));
                            int index = compoundTag.getInt("VAR_Index");

                            boolean isValid = pattern.getInputs()[index].isValid(input, level);
                            response.putBoolean("Answer", isValid);

                            jedis.publish(channel, response.toString());
                            break;
                    }

                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }, this.getRedisChanel(uuid));
    }

    @Override
    protected void onStop() {

    }

    public static String getRedisChanel(String uuid) {
        return uuid+".pattern";
    }

    public static NetworkPattern fromString(String data, Level level, Jedis jedis) throws CommandSyntaxException {
        CompoundTag compoundTag = TagParser.parseTag(data);
        String origin = compoundTag.getString("Origin");
        boolean exists = compoundTag.getBoolean("Exists");

        IPatternDetails pattern = convertCompoundTagToIPatternDetails(compoundTag.getCompound("Pattern"), jedis);
        return new NetworkPattern(pattern, origin, exists, level);
    }

    public static String toString(NetworkPattern networkPattern) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Origin", networkPattern.origin);
        compoundTag.putBoolean("Exists", networkPattern.exists);
        compoundTag.put("Pattern", convertIPatternDetailsToCompoundTag(networkPattern.pattern, networkPattern));
        return compoundTag.toString();
    }

    private static IPatternDetails convertCompoundTagToIPatternDetails(CompoundTag compoundTag, Jedis jedis) {
        return new IPatternDetails() {
            @Override
            public AEItemKey getDefinition() {
                return AEItemKey.fromTag(compoundTag.getCompound("Definition"));
            }

            @Override
            public IInput[] getInputs() {
                return convertCompoundTagToIInputList(compoundTag.getCompound("Inputs"), jedis);
            }

            @Override
            public GenericStack[] getOutputs() {
                return convertCompoundTagToGenericStackList(compoundTag.getCompound("Outputs"));
            }

            @Override
            public GenericStack getPrimaryOutput() {
                return convertCompoundTagToGenericStack(compoundTag.getCompound("PrimaryOutput"));
            }
        };
    }

    private static CompoundTag convertIPatternDetailsToCompoundTag(IPatternDetails pattern, NetworkPattern networkPattern) {
        CompoundTag patternTag = new CompoundTag();
        patternTag.put("PrimaryOutput", convertGenericStackToCompoundTag(pattern.getPrimaryOutput()));
        patternTag.put("Outputs", convertGenericStackListToCompoundTag(pattern.getOutputs()));
        patternTag.put("Definition", pattern.getDefinition().toTagGeneric());
        patternTag.put("Inputs", convertIInputListToCompoundTag(pattern.getInputs(), networkPattern));
        return patternTag;
    }

    private static CompoundTag convertIInputToCompoundTag(IPatternDetails.IInput data, NetworkPattern networkPattern) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("UUID", networkPattern.uuid);
        compoundTag.put("PossibleInputs", convertGenericStackListToCompoundTag(data.getPossibleInputs()));
        compoundTag.putLong("Multiplier", data.getMultiplier());
        return compoundTag;
    }

    private static IPatternDetails.IInput convertCompoundTagToIInput(CompoundTag data, int index, Jedis jedis) {
        String redisChannel = getRedisChanel(data.getString("UUID"));

        return new IPatternDetails.IInput() {
            @Override
            public GenericStack[] getPossibleInputs() {
                return convertCompoundTagToGenericStackList(data.getCompound("PossibleInputs"));
            }

            @Override
            public long getMultiplier() {
                return data.getLong("Multiplier");
            }

            @Override
            public boolean isValid(AEKey input, Level level) {
                String requestId = UUID.randomUUID().toString();

                CompoundTag message = new CompoundTag();
                message.putString("Type","Request");
                message.putString("Method", "IsValid");
                message.putString("RequestID", requestId);
                message.put("VAR_Input", input.toTagGeneric());
                message.putInt("VAR_Index", index);

                final boolean[] waiting = {true};
                final boolean[] answer = {false};
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        try {
                            CompoundTag compoundTag = TagParser.parseTag(message);
                            if(!compoundTag.getString("Type").equals("Response")) return;
                            if(!compoundTag.getString("Method").equals("IsValid")) return;
                            if(!compoundTag.getString("RequestID").equals(requestId)) return;

                            answer[0] = compoundTag.getBoolean("Answer");
                            waiting[0] = false;
                        } catch (CommandSyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, redisChannel);
                jedis.publish(redisChannel, message.toString());

                while (waiting[0]) {}
                return answer[0];
            }

            @Nullable
            @Override
            public AEKey getRemainingKey(AEKey template) {
                return null;
            }
        };
    }

    private static CompoundTag convertIInputListToCompoundTag(IPatternDetails.IInput[] data, NetworkPattern networkPattern) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("Size", data.length);
        for(int i = 0; i < data.length; i++) {
            compoundTag.put(i+"-index", convertIInputToCompoundTag(data[i], networkPattern));
        }
        return compoundTag;
    }

    private static IPatternDetails.IInput[] convertCompoundTagToIInputList(CompoundTag data, Jedis jedis) {
        int size = data.getInt("Size");
        IPatternDetails.IInput[] iInputs = new IPatternDetails.IInput[size];
        for(int i = 0; i < size; i++) {
            iInputs[i] = convertCompoundTagToIInput(data.getCompound(i+"-index"), i, jedis);
        }
        return iInputs;
    }



    private static CompoundTag convertGenericStackToCompoundTag(GenericStack data) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("What", data.what().toTagGeneric());
        compoundTag.putLong("Amount", data.amount());
        return compoundTag;
    }

    private static GenericStack convertCompoundTagToGenericStack(CompoundTag data) {
        AEKey what = AEKey.fromTagGeneric(data.getCompound("What"));
        long amount = data.getLong("Amount");
        return new GenericStack(what, amount);
    }

    private static CompoundTag convertGenericStackListToCompoundTag(GenericStack[] data) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("Size", data.length);
        for(int i = 0; i < data.length; i++) {
            compoundTag.put(i+"-index", convertGenericStackToCompoundTag(data[i]));
        }
        return compoundTag;
    }

    private static GenericStack[] convertCompoundTagToGenericStackList(CompoundTag data) {
        int size = data.getInt("Size");
        GenericStack[] genericStacks = new GenericStack[size];
        for(int i = 0; i < size; i++) {
            genericStacks[i] = convertCompoundTagToGenericStack(data.getCompound(i+"-index"));
        }
        return genericStacks;
    }

}
