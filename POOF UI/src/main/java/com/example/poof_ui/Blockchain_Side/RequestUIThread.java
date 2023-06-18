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
    private HashMap<MinerGUI, MinerDateUI> minersHavingRequestsAlready = new HashMap<>();

    private ArrayList<TraderDateUI> traderDates = new ArrayList<>();
    private HashMap<TraderGUI, TraderDateUI> tradersHavingRequestsAlready = new HashMap<>();

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
                        if((currentTime - minerDates.get(i).startTime) > 1000)
                        {
                            PoofController.getInstance().SetMinerGUIColor(minerDates.get(i).minerGUI, greyColor);
                            minersHavingRequestsAlready.remove(minerDates.get(i).minerGUI);
                            minerDates.remove(i);
                        }
                    }

                    for (int i = traderDates.size()-1; i >= 0; i--)
                    {
                        if((currentTime - traderDates.get(i).startTime) > 1000)
                        {
                            PoofController.getInstance().SetTraderGUIColor(traderDates.get(i).traderGUI, greyColor);
                            tradersHavingRequestsAlready.remove(traderDates.get(i).traderGUI);
                            traderDates.remove(i);
                        }
                    }

                    Thread.sleep(10);

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }

    }

    public void AddTraderStartingTime(TraderGUI traderGUI)
    {
        if(!tradersHavingRequestsAlready.containsKey(traderGUI))
        {
            TraderDateUI traderDateUI = new TraderDateUI(traderGUI, new Date());
            traderDates.add(traderDateUI);
            tradersHavingRequestsAlready.put(traderGUI, traderDateUI);
        }
        else
        {
            tradersHavingRequestsAlready.get(traderGUI).startTime = new Date().getTime();
        }
    }

    public void AddMinerStartingTime(MinerGUI minerGUI)
    {
        if(!minersHavingRequestsAlready.containsKey(minerGUI))
        {
            MinerDateUI minerDateUI = new MinerDateUI(minerGUI, new Date());
            minerDates.add(minerDateUI);
            minersHavingRequestsAlready.put(minerGUI, minerDateUI);
        }
        else
        {
            minersHavingRequestsAlready.get(minerGUI).startTime = new Date().getTime();
        }
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
