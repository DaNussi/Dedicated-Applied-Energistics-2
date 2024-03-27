package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.core.definitions.AEItems;
import com.mojang.logging.LogUtils;
import com.rabbitmq.client.Channel;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.VirtualDiskClient;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.VirtualDiskHost;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.VirtualDiskInfo;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class InterDimensionalInterfaceStorage implements IStorageProvider, IGridTickable {
    private static final Logger LOGGER = LogUtils.getLogger();

    private InterDimensionalInterfaceBlockEntity instance;
    private Jedis redis;
    private Channel rabbitmq;

    private Vector<VirtualDiskHost> diskHosts = new Vector<>();
    private Vector<VirtualDiskClient> diskClients = new Vector<>();

    AtomicBoolean running = new AtomicBoolean(false);

    public InterDimensionalInterfaceStorage(InterDimensionalInterfaceBlockEntity instance) {
        this.instance = instance;
    }

    public void onStart() {
        LOGGER.info(instance.getUuid() + " | Starting storage provider");

        this.redis = instance.getRedis();
        this.rabbitmq = instance.getRabbitmq();

        LOGGER.info(instance.getUuid() + " | Started storage provider");
    }

    public void onStop() {
        LOGGER.info(instance.getUuid() + " | Stopping storage provider");

        for (VirtualDiskClient diskClient : diskClients) {
            diskClient.onStop();
        }

        for (VirtualDiskHost diskHost : diskHosts) {
            diskHost.onStop();
        }

        LOGGER.info(instance.getUuid() + " | Stopped storage provider");
    }


    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
//        indexGrid();
    }

    AtomicLong lastGridIndex = new AtomicLong(0);
    AtomicBoolean busyGridIndex = new AtomicBoolean(false);
    public void indexGrid() {
//        if(busyGridIndex.get() && System.currentTimeMillis() - lastGridIndex.get() > 2000) {
//            busyGridIndex.set(false);
//            LOGGER.warn(instance.getUuid() + " | Unstuck grid indexing");
//        }
//
//        if (busyGridIndex.get()) {
////            LOGGER.warn(instance.getUuid() + " | Skipping indexing of grid because it's already running");
//            return;
//        }
//        busyGridIndex.set(true);

        if (System.currentTimeMillis() - lastGridIndex.get() < 900) {
//            LOGGER.warn(instance.getUuid() + " | Skipping indexing of grid because of cooldown");
            return;
        }

        if (redis == null) {
            LOGGER.warn(instance.getUuid() + " | Skipping indexing of grid because redis is null");
            return;
        }

        if (rabbitmq == null) {
            LOGGER.warn(instance.getUuid() + " | Skipping indexing of grid because rabbit mq is null");
            return;
        }

//        LOGGER.info(instance.getUuid() + " | Indexing grid");
        var grid = instance.getMainNode().getGrid();
        if (grid == null) {
            LOGGER.error(instance.getUuid() + " | Failed to index because grid is null");
            return;
        }

//        LOGGER.info(instance.getUuid() + " | Indexing grid!");
//        LOGGER.info(instance.getUuid() + " | Machine Classe: " + instance.getMainNode().getGrid().getMachineClasses().toString());

        List<MEStorage> storageCells = new ArrayList<>();

        for (Class<?> machineClasses : grid.getMachineClasses()) {
            var machines = grid.getMachines(machineClasses);
            for (var machine : machines) {
                if (machine instanceof IStorageProvider storageProvider) {
                    storageProvider.mountInventories((inventory, priority) -> {
                        storageCells.add(inventory);
                    });
                }
//                if (machine instanceof DriveBlockEntity driveBlock) {
//                    for (int i = 0; i < driveBlock.getCellCount(); i++) {
//                        DriveWatcher driveWatcher = (DriveWatcher) driveBlock.getCellInventory(i);
//                        if(driveWatcher != null) {
//                            StorageCell storageCell = driveWatcher.getCell();
//                            storageCells.add(storageCell);
//                        } else {
////                            LOGGER.warn("Skipping StorageCell because it's null");
//                        }
//                    }
//                }
            }
        }

//        LOGGER.info(instance.getUuid() + " | Found " + storageCells.size() + " storages");
        updateVirtualHosts(storageCells);



        List<String> channels = new ArrayList<>();
        var diskInfos = redis.hgetAll(VirtualDiskInfo.REDIS_PATH);
        diskInfos.forEach((chanel,data) -> {
            VirtualDiskInfo info = null;
            try {
                info = VirtualDiskInfo.fromString(data);

                // IF VDrive is not form this instance add it
                if(!info.getOrigin().equals(instance.getUuid())) {
                    channels.add(chanel);
//                    LOGGER.debug("Adding virtual disk " + chanel);
                } else {
//                    LOGGER.debug("Skipping virtual disk because we are the origin");
                }
            } catch (Exception e) {
                LOGGER.error("Failed to parse disk info data");
                e.printStackTrace();
            }
        });
//        LOGGER.info(instance.getUuid() + " | Found " + channels.size() + " virtual storages");
        updateVirtualClients(channels);
        IStorageProvider.requestUpdate(instance.getMainNode());


        for(VirtualDiskHost host : diskHosts) {
            host.storage.insert(AEItemKey.of(AEItems.ITEM_CELL_256K.stack()), 1, Actionable.MODULATE, IActionSource.ofMachine(instance));
        }

        lastGridIndex.set(System.currentTimeMillis());
        busyGridIndex.set(false);
    }

    private void updateVirtualHosts(List<MEStorage> storageCells) {
        List<VirtualDiskHost> hosts = new ArrayList<>();

        for (MEStorage storageCell : storageCells) {
            hosts.add(new VirtualDiskHost(storageCell, 0, instance));
        }

        // REMOVE HOST IF IT DOSEN'T EXIST ANYMORE
        for (int i = 0; i < diskHosts.size(); i++) {
            VirtualDiskHost disk = diskHosts.get(i);
            if (!hosts.contains(disk)) {
                LOGGER.info("Removing virtual disk host " + disk.getChannel());
                disk.onStop();
                diskHosts.remove(disk);
            }
        }

        // ADD HOST IF IT DOESN'T YET EXIST
        for (VirtualDiskHost disk : hosts) {
            if (!diskHosts.contains(disk)) {
                diskHosts.add(disk);
                LOGGER.info("Adding virtual disk host " + disk.getChannel());
                disk.onStart();
            }
        }
    }

    private void updateVirtualClients(List<String> channels) {
        List<VirtualDiskClient> clients = new ArrayList<>();
        for (String channel : channels) {
            clients.add(new VirtualDiskClient(channel, instance));
        }

        // REMOVE HOST IF IT DOSEN'T EXIST ANYMORE
        for (int i = 0; i < diskClients.size(); i++) {
            VirtualDiskClient disk = diskClients.get(i);
            if (!clients.contains(disk)) {
                LOGGER.info("Removing virtual disk client " + disk.getChannel());
                disk.onStop();
                diskClients.remove(disk);
            }
        }

        // ADD HOST IF IT DOESN'T YET EXIST
        for (VirtualDiskClient disk : clients) {
            if (!diskClients.contains(disk)) {
                diskClients.add(disk);
                LOGGER.info("Adding virtual disk client " + disk.getChannel());
                disk.onStart();
            }
        }
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        for (VirtualDiskClient disk : diskClients) {
            storageMounts.mount(disk, 0);
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {

        return new TickingRequest(1, 1, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        indexGrid();

        for (var diskHost : diskHosts.stream().toList()) {
            diskHost.onTick();
        }

        return TickRateModulation.SAME;
    }
}
