package lu.r3flexi0n.bungeeonlinetime.objects;

public class OnlineTimePlayer {

    private Long savedOnlineTime = null;

    private final long joinProxyTimestamp;

    private long timeOnDisabledServers;

    private long joinDisabledServerTimestamp;

    public OnlineTimePlayer() {
        this.joinProxyTimestamp = System.currentTimeMillis();
    }

    public void setSavedOnlineTime(long onlineTime) {
        this.savedOnlineTime = onlineTime;
    }

    public Long getSavedOnlineTime() {
        return savedOnlineTime;
    }

    public long getSessionOnlineTime() {
        return System.currentTimeMillis() - joinProxyTimestamp - getTimeOnDisabledServers();
    }

    private long getTimeOnDisabledServers() {
        long time = timeOnDisabledServers;
        if (joinDisabledServerTimestamp > 0) {
            time += (System.currentTimeMillis() - joinDisabledServerTimestamp);
        }
        return time;
    }

    public void joinDisabledServer() {
        if (joinDisabledServerTimestamp == 0) {
            joinDisabledServerTimestamp = System.currentTimeMillis();
        }
    }

    public void leaveDisabledServer() {
        if (joinDisabledServerTimestamp > 0) {
            timeOnDisabledServers += System.currentTimeMillis() - joinDisabledServerTimestamp;
            joinDisabledServerTimestamp = 0;
        }
    }
}
