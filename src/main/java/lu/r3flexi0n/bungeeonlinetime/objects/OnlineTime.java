package lu.r3flexi0n.bungeeonlinetime.objects;

import java.util.UUID;

public class OnlineTime {

    private final UUID uuid;

    private final String name;

    private final long time;

    public OnlineTime(UUID uuid, String name, long time) {
        this.uuid = uuid;
        this.name = name;
        this.time = time;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return time;
    }
}
