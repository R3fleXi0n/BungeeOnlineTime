package lu.r3flexi0n.bungeeonlinetime.utils;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class AsyncTask {

    private final Plugin plugin;

    public AsyncTask(Plugin plugin) {
        this.plugin = plugin;
    }

    private void execute(Task task, boolean retry) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            try {
                task.onSuccess(task.doTask());
            } catch (Exception ex) {
                if (retry) {
                    execute(task, false);
                    return;
                }
                task.onError(ex);
            }
        });
    }

    public void execute(Task task) {
        execute(task, true);
    }

    public interface Task<T> {

        T doTask() throws Exception;

        void onSuccess(T response);

        void onError(Exception exception);

    }

}
