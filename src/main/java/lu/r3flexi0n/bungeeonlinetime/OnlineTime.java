package lu.r3flexi0n.bungeeonlinetime;

public class OnlineTime {

    private final String name;

    private final long time;

    public OnlineTime(String name, long time) {
        this.name = name;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return time;
    }
}
