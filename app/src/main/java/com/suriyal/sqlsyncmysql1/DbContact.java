package com.suriyal.sqlsyncmysql1;

public class DbContact
{
    public static final String DATABASE_NAME = "id14336900_mydb";
    public static final String TABLE_NAME = "contacts";
    public static final String NAME = "name";
    public static final String SYNC_STATUS = "syncstatus";

    public static final int SYNC_STATUS_OK = 0;
    public static final int SYNC_STATUS_FAILED = 1;

    public static final String Server_url="https://suriyal.000webhostapp.com/syncinsert.php";

    public static final String UI_UPDATE_BROADCAST="com.suriyal.sqlsyncmysql1.uiupdatebroadcast";
}
