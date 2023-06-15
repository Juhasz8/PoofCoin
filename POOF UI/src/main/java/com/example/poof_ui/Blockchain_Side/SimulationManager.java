package com.example.poof_ui.Blockchain_Side;

import com.example.poof_ui.CurrentEventManager;
import com.example.poof_ui.PoofController;

import java.util.Random;

public class SimulationManager implements Runnable
{
    private static SimulationManager instance;
    //creates the User objects and the threads
    private Miner miner;

    public CurrentEventManager eventManager;

    public float marketPrice = 0;

    private Random random = new Random();

    private boolean isSuspended = false;


    //These fields are for storing the last 5 cycle's buying and selling requests
    // (which happened in the last 5*2 = 10 seconds)
    //if we would need the correct order in which the amounts happened we will need a linked list
    private int currentListLength = 1;
    private int maxListLength = 5;

    //the requests that happened in the last update cycle
    public RequestLink requestLinkHead = new RequestLink();

    //the requests that happened in the 5th last update cycle
    public RequestLink requestLinkTail;

    public float previousMarketPrice;

    //getters for the total values
    public int GetSellingRequestAmount()
    {
        int sum = 0;
        RequestLink current = requestLinkTail;
        while(current.next != null)
        {
            sum += current.cycleSellingRequestAmount;
            current = current.next;
        }
        return sum;
    }
    public int GetBuyingRequestAmount()
    {
        int sum = 0;
        RequestLink current = requestLinkTail;
        while(current.next != null)
        {
            sum += current.cycleBuyingRequestAmount;
            current = current.next;
        }
        return sum;
    }

    public int GetBuyingRequestAmountDifference()
    {
        return requestLinkHead.cycleBuyingRequestAmount - requestLinkHead.previous.cycleBuyingRequestAmount;
    }

    public int GetSellingRequestAmountDifference()
    {
        return requestLinkHead.cycleSellingRequestAmount - requestLinkHead.previous.cycleSellingRequestAmount;
    }

    //called in the update loop
    private void UpdateRequestListening()
    {
        //the linked list reached its maximum capacity
        if(currentListLength == maxListLength)
        {
            //we remove the last element by setting the tail pointer to the right by one
            requestLinkTail = requestLinkTail.next;

            //adding new element, and setting the head point there
            RequestLink newHead = new RequestLink();
            requestLinkHead.next = newHead;
            requestLinkHead = newHead;
        }
        else
        {
            if(currentListLength == 1)
                requestLinkTail = requestLinkHead;

            //adding new element and setting the head point there
            RequestLink newHead = new RequestLink();
            requestLinkHead.next = newHead;
            newHead.previous = requestLinkHead;
            requestLinkHead = newHead;

            //we increment the length
            currentListLength++;
        }
    }
    //----------------


    public static SimulationManager getInstance() {
        if (instance == null)
            instance = new SimulationManager();

        return instance;
    }

    private SimulationManager()
    {
        //make the very first miner join the network
        //Miner miner1 = new Miner(10, 11, MinerType.THAT_ONE_GUY, GetMinerSleepingTime(MinerType.THAT_ONE_GUY));
        //miner1.start();

        Miner miner2 = new Miner(MinerType.HUGE_CORP, GetMinerSleepingTime(MinerType.HUGE_CORP));
        miner2.start();


        //Miner miner3 = new Miner(MinerType.HUGE_CORP, GetMinerSleepingTime(MinerType.HUGE_CORP));
        //miner3.start();
    }

    public void run()
    {
        synchronized (this)
        {
            while (true)
            {
                try
                {
                    while (isSuspended)
                        wait();

                    DetermineMarketPrice();
                    JoiningPeople();
                    UpdateRequestListening();

                    Thread.sleep(2000);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void JoiningPeople() {

        //there shouldn't be any people joining until the first block is mined
        // (after that the price will go up so this won't return)
        if(marketPrice == 0)
            return;

        //create the user objects and their threads respectively

        //decide how many miner will join
        //depends on the amount of miners currently on the market, and the value of the currency and some random factor

        //decide how many trader will join
        //depends on the amount of traders currently on the market, and the value of the currency and some random factor
        //1 + 1/(n+1)
        if(Network.getInstance().GetTraderAmount() == 0)
        {
            TraderType typeToJoin;
            //50% chance that a risk appetite trader joins
            if(random.nextInt(2) == 0)
                typeToJoin = TraderType.RISK_APPETITE;
            else//50% chance that a psychopath trader joins
                typeToJoin = TraderType.PSYCHOPATH;

            //join the first trader to the simulation
            Trader firstTrader = new Trader(typeToJoin);
            firstTrader.start();
            //return cause in this cycle only this trader should join
            return;
        }

        //if the marketprice went up people are more likely to join
        float priceDifference = marketPrice - previousMarketPrice;
        int minerAmountToJoin = 0;
        int traderAmountToJoin = 0;

        if(priceDifference > 0)
        {
            //since market price went up, more people will be joining


            //there is a 20% chance that no miners join, otherwise there will be 1-2 miners joining, and even more if the market price increase is above 50%
            if(random.nextInt(5) != 0)
            {
                //if the market price increase is above 50%
                if((previousMarketPrice / marketPrice) * 100 > 50)
                {
                    //there will be more miner and trader joining

                    //20%chance that no miners join
                    if(random.nextInt(5) != 0)
                        minerAmountToJoin += (int)(Network.getInstance().GetMinerAmount() / 5) + random.nextInt(2)+1;

                    //20%chance that no traders join
                    if(random.nextInt(5) != 0)
                        traderAmountToJoin += (int)(Network.getInstance().GetTraderAmount() / 5) + random.nextInt(2)+1;
                }
                else
                {
                    //there will be less miner and trader joining

                    //20%chance that no miners join
                    if(random.nextInt(5) != 0)
                        minerAmountToJoin += (int)(Network.getInstance().GetMinerAmount() / 10) + random.nextInt(1)+0;

                    //20%chance that no traders join
                    if(random.nextInt(5) != 0)
                        traderAmountToJoin += (int)(Network.getInstance().GetTraderAmount() / 10) + random.nextInt(1)+0;

                }
            }

        }
        else
        {
            ///since market price went down, less people will be joining

            //if the market price decrease is less than 25%
            if((marketPrice / previousMarketPrice) * 100 < 25)
            {
                //there will be more miner and trader joining

                //50%chance that no miners join
                if(random.nextInt(2) != 0)
                    minerAmountToJoin += (int)(Network.getInstance().GetMinerAmount() / 20) + random.nextInt(1)+0;

                //50%chance that no traders join
                if(random.nextInt(2) != 0)
                    traderAmountToJoin += (int)(Network.getInstance().GetTraderAmount() / 20) + random.nextInt(1)+0;
            }

            //otherwise some people are leaving ?

        }

        //when determining the trader type, if priceDifference < 0, psychopaths and risky traders are more likely to join


        //join the traders and miners to the network
        //we will decide the miner and trader type here, and not in the miner and trader constructor.
        for (int i = 0; i < minerAmountToJoin; i++)
        {
            if(Network.getInstance().GetMinerAmount() == 60)
                break;

            Miner newMiner = new Miner(MinerType.HUGE_CORP, GetMinerSleepingTime(MinerType.HUGE_CORP));
            newMiner.start();
        }

        for (int i = 0; i < traderAmountToJoin; i++)
        {
            if(Network.getInstance().GetTraderAmount() == 60)
                break;

            Trader newTrader = new Trader(TraderType.RISK_APPETITE);
            newTrader.start();
        }


    }

    private void DetermineMarketPrice()
    {
        previousMarketPrice = marketPrice;
        float marketPriceChange = 0;

        //before the first block was mined, the market price stays at 0
        if(Network.getInstance().fullNode.GetLongestChainSize() == 0)
            return;

        //I need a reference to all the transaction requests that has happened since the last update cycle
        //based on their amounts, we change the value of market price

        //if(Network.getInstance().networkUsers.size() > 1)


        if (marketPrice == 0)
        {
            marketPriceChange += random.nextFloat(2) + 0.5; //random number between 0.5 and 2.5
        }
        else if(Network.getInstance().networkUsers.size() < 5)
            marketPriceChange += random.nextFloat(5) - 2.5; //random number between -2.5 and 2.5
        else
            marketPriceChange += random.nextFloat(20) - 10; //random number between -10 and 10

        // GetBuyingRequestAmountDifference and GetSellingRequestAmountDifference return a pos number if more requests were made this cycle.

        //when there are more buying requests, t
        //marketPrice += GetBuyingRequestAmountDifference() * random.nextFloat();

        //marketPrice -= GetSellingRequestAmountDifference() * random.nextFloat();

        int buyerSellerDifference = requestLinkHead.cycleBuyingRequestAmount - requestLinkHead.cycleSellingRequestAmount;
        int totalRequests = requestLinkHead.cycleBuyingRequestAmount + requestLinkHead.cycleSellingRequestAmount;

        System.out.println("...................");
        System.out.println("selling requests last cycle: " + requestLinkHead.cycleSellingRequestAmount);
        System.out.println("buying requests last cycle: " + requestLinkHead.cycleBuyingRequestAmount);
        System.out.println("buyerSellerDifference: " + buyerSellerDifference);
        System.out.println("...................");

        /*
        //if more people are trying to buy than sell the market price decreases
        if(requestLinkHead.cycleBuyingRequestAmount > requestLinkHead.cycleSellingRequestAmount)
            marketPrice += (buyerSellerDifference) * (1+marketPrice/100) * (random.nextFloat(0.75f)+0.75);
        //otherwise increases
        else
            marketPrice += (buyerSellerDifference) * (1+marketPrice/100) * (random.nextFloat(0.75f)+0.75);
        */
        if(buyerSellerDifference < -5 && (requestLinkHead.cycleBuyingRequestAmount/totalRequests) * 100 < 20)
            marketPriceChange += (buyerSellerDifference) * (1+marketPrice/100) * (random.nextFloat(0.75f)+0.75);
        else if (buyerSellerDifference != 0)
            marketPriceChange += buyerSellerDifference * (1+marketPrice/50) * (random.nextFloat(0.75f)+0.75);
        else if (totalRequests > 10)
            marketPriceChange += totalRequests/10 * (1+marketPrice/50) * (random.nextFloat(0.75f)+0.75);


        marketPrice += marketPriceChange;

        if(marketPrice < 0)
            marketPrice = 0;

        //System.out.println("new market price: " + marketPrice);

        // Update Market Price Label
        PoofController.getInstance().updateMarketPriceLabel(String.valueOf(marketPrice));
        // Update Price Graph
        PoofController.getInstance().updatePriceGraph(String.valueOf(marketPrice));
        // Update Price Percentage
        PoofController.getInstance().updateMarketPercentageLabel(String.valueOf(marketPrice));
    }

    public void SuspendSimulation()
    {
        //suspend all the user threads
        for(User user : Network.getInstance().networkUsers.values())
        {
            user.SuspendThread();
        }
        //suspend this thread
        isSuspended = true;
    }

    public void ResumeSimulation()
    {
        //resume all the user threads
        for(User user : Network.getInstance().networkUsers.values())
        {
            user.ResumeThread();
        }
        //resume this thread
        isSuspended = false;
    }

    private long GetMinerSleepingTime(MinerType minerType)
    {
        if(minerType == MinerType.THAT_ONE_GUY)
            return random.nextLong(100)+50;
        else if(minerType == MinerType.THESE_GUYS)
            return random.nextLong(100)+50;
        else if(minerType == MinerType.GROUP)
            return random.nextLong(100)+50;
        else if(minerType == MinerType.SMALL_CORP)
            return random.nextLong(100)+50;
        else if(minerType == MinerType.HUGE_CORP)
            return random.nextLong(10)+10;
            //return 30;

        System.out.println("Something went wrong! Non-existing MinerType!");
        return random.nextLong(100)+50;
    }

}
