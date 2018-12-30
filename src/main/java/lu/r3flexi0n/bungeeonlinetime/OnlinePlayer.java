package lu.r3flexi0n.bungeeonlinetime;

public class OnlinePlayer {

    private final long joinTime;

    private long afkTime;

    private long afkJoin;

    public OnlinePlayer() {
        this.joinTime = System.currentTimeMillis();
    }

    public long getNoAFKTime() {
        return System.currentTimeMillis() - joinTime - afkTime - getCurrentAFKTime();
    }

    public void joinAFK() {
        if (afkJoin == 0) {
            afkJoin = System.currentTimeMillis();
        }
    }

    public void leaveAFK() {
        if (afkJoin > 0) {
            afkTime += System.currentTimeMillis() - afkJoin;
            afkJoin = 0;
        }
    }

    private long getCurrentAFKTime() {
        if (afkJoin > 0) {
            return System.currentTimeMillis() - afkJoin;
        }
        return 0;
    }
}
