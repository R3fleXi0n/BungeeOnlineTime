package lu.r3flexi0n.bungeeonlinetime;

public class OnlineTime {

    private final long joinTime;

    private long afk;

    private long afkJoin;

    public OnlineTime() {
        this.joinTime = System.currentTimeMillis();
    }

    public long getJoinTime() {
        return joinTime;
    }

    public long getAFK() {
        return afk;
    }

    public void joinAFK() {
        afkJoin = System.currentTimeMillis();
    }

    public void leaveAFK() {
        if (afkJoin > 0) {
            afk += System.currentTimeMillis() - afkJoin;
            afkJoin = 0;
        }
    }
}
