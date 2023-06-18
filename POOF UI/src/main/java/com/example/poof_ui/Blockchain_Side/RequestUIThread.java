package com.example.poof_ui.Blockchain_Side;

import com.example.poof_ui.MinerGUI;
import com.example.poof_ui.PoofController;
import com.example.poof_ui.TraderGUI;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;



public class RequestUIThread extends Thread
{

    private class MinerDateUI
    {
        public MinerGUI minerGUI;
        public long startTime;

        public MinerDateUI(MinerGUI minerGUI, Date startDate)
        {
            this.minerGUI = minerGUI;
            this.startTime = startDate.getTime();
        }
    }

    private class TraderDateUI
    {
        public TraderGUI traderGUI;
        public long startTime;

        public TraderDateUI(TraderGUI traderGUI, Date startDate)
        {
            this.traderGUI = traderGUI;
            this.startTime = startDate.getTime();
        }
    }

    private boolean isSuspended = false;

    private ArrayList<MinerDateUI> minerDates = new ArrayList<>();
    private ArrayList<TraderDateUI> traderDates = new ArrayList<>();

    private String greyColor = "-fx-background-color: #8D8D8D;";

    public void run()
    {
        synchronized (this)
        {
            while (true)
            {
                try
                {
                    while(isSuspended)
                        wait(10);

                    long currentTime = new Date().getTime();
                    for (int i = minerDates.size()-1; i >= 0; i--)
                    {
                        if((currentTime - minerDates.get(i).startTime) < 500)
                        {
                            PoofController.getInstance().SetMinerGUIColor(minerDates.get(i).minerGUI, greyColor);
                            minerDates.remove(i);
                        }
                    }

                    for (int i = traderDates.size()-1; i >= 0; i--)
                    {
                        if((currentTime - traderDates.get(i).startTime) < 500)
                        {
                            PoofController.getInstance().SetTraderGUIColor(traderDates.get(i).traderGUI, greyColor);
                            traderDates.remove(i);
                        }
                    }

                    Thread.sleep(100);

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }

    }

    public void AddStartingTime()
    {

    }

    public void SuspendThread()
    {
        isSuspended = true;
    }

    public void ResumeThread()
    {
        isSuspended = false;
    }

}
