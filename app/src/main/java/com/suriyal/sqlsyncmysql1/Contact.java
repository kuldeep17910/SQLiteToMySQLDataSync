package com.suriyal.sqlsyncmysql1;

public class Contact {

    private String Name;
    private int Sync_status;


    //constructor
    public Contact(String Name, int Sync_status)
    {
       // using in readFromLocalStorage() in main activity
        this.Name = Name;
        this.Sync_status = Sync_status;
    }

   //getter setter start
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getSync_status() {
        return Sync_status;
    }

    public void setSync_status(int sync_status) {
        Sync_status = sync_status;
    }

}
