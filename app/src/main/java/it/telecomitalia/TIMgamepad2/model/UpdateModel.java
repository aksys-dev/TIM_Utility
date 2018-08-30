package it.telecomitalia.TIMgamepad2.model;

import it.telecomitalia.TIMgamepad2.fota.SPPConnection;

/**
 * Created by czy on 2018/7/24.
 */

public class UpdateModel {
    private SPPConnection mainConnection;

    private boolean isUpdate;

    public UpdateModel(SPPConnection mainConnection, boolean isUpdate) {
        this.mainConnection = mainConnection;
        this.isUpdate = isUpdate;
    }

    public SPPConnection getMainConnection() {
        return mainConnection;
    }


    public boolean isUpdate() {
        return isUpdate;
    }

}
