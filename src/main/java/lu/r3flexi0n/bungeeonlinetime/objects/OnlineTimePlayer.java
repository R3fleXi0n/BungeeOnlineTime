package lu.r3flexi0n.bungeeonlinetime.objects;

public class OnlineTimePlayer {

    private OnlineTime totalOnlineTime = null;

    private final long joinProxyTimestamp;

    private long timeOnDisabledServers;

    private long joinDisabledServerTimestamp;

    public OnlineTimePlayer() {
        this.joinProxyTimestamp = System.currentTimeMillis();
    }

    public void setTotalOnlineTime(OnlineTime onlineTime) {
        this.totalOnlineTime = onlineTime;
    }

    public OnlineTime getTotalOnlineTime() {
        return totalOnlineTime;
    }

    public long getSessionOnlineTime() {
        leaveDisabledServer();
        return System.currentTimeMillis() - joinProxyTimestamp - timeOnDisabledServers;
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
