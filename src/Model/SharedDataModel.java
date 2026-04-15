package Model;

import java.util.ArrayList;

import java.util.List;

public class SharedDataModel {

    private static SharedDataModel instance;
    private List<Runnable> refreshCallbacks = new ArrayList<>();

    private SharedDataModel() {}

    public static synchronized SharedDataModel getInstance() {
        if (instance == null) {
            instance = new SharedDataModel();
        }
        return instance;
    }

    public void addRefreshCallback(Runnable callback) {
        refreshCallbacks.add(callback);
    }

    public void triggerRefresh() {
        for (Runnable callback : refreshCallbacks) {
            callback.run();
        }
    }
}
