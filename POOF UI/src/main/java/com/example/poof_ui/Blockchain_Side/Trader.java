package com.example.poof_ui.Blockchain_Side;

import com.example.poof_ui.MinerGUI;
import com.example.poof_ui.PoofController;
import com.example.poof_ui.TraderGUI;

import java.util.Date;
import java.util.Random;

enum TraderType { RISK_APPETITE, TREND_FOLLOWER, CONTRARIAN_APPROACH, EVENT_FOLLOWER, PSYCHOPATH }

public class Trader extends User
{

    public TraderType type;

    private TraderGUI traderGUI;

    private String redColor = "-fx-background-color: #ff5e57;";
    private String redOutline = "#ff5e57";
    private String greyOutline = "#8d8d8d";
    private String greenOutline = "#4FCB59";
    private String greenColor = "-fx-background-color: #4FCB59;";
    private String greyColor = "-fx-background-color: #8D8D8D;";

    public Trader(TraderType type, String name)
    {
        super(name);
        this.type = type;
        Network.getInstance().JoinTraderToTheNetwork(this);

        traderGUI = new TraderGUI();
        PoofController.getInstance().SetTraderGUICoin(traderGUI, decFormatter.format(0), greyColor);
        PoofController.getInstance().AddTraderGUI(traderGUI, String.valueOf(type));
    }

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

                    if(cycleUntilPossibleNextExchange > 0)
                        cycleUntilPossibleNextExchange--;

                    DecideWhetherToBuyOrSell();

                    Thread.sleep(2000);

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }

    }

    private void DecideWhetherToBuyOrSell()
    {
        //there is a 10% chance that the trader won't sell or buy anything
        if(cycleUntilPossibleNextExchange > 0 || amountOfPendingRequests == 3 || random.nextInt(10) == 0)
            return;

        Exchange exchange = new Exchange();
        //difference represents how likely u will sell or buy
        //Double difference = 0.0;
        //percent represent how many percent of you current poof or euro you want to exchange
        //Double exchangePercent;

        //for some people, the likeliness of trading is also taken into account when the amount is calculated
        //this depends on the type of the trader

        if(type == TraderType.RISK_APPETITE) // high risk high reward guy
        {
            //sells fewer times, and more likely to buy if the price is low or the amount of people buying is very low
            exchange.difference = random.nextDouble(175)-75; //random difference between -75 and 100

            //whatever he decides to do, he will deal with a lot of his current money or poof
            exchange.percent = random.nextDouble(.4)+0.5; //will deal with 50-90% of his current currency

        }
        else if (type == TraderType.TREND_FOLLOWER)  //makes decisions based on what the majority is doing
        {
            //sells if a lot of people sell and buys if a lot of people buy
            int requestDifference = SimulationManager.getInstance().GetRequestDifference();
            exchange.difference = requestDifference * (random.nextDouble(requestDifference/10)+1) * (random.nextDouble(14)+1);
            exchange.percent = random.nextDouble(0.4)+0.35; //will deal with 35-75% of his current currency
        }
        else if (type == TraderType.CONTRARIAN_APPROACH)  //makes decisions based on what the minority is doing
        {
            //more likely to sell if a lot of people buy
            //more likely to buy if a lot of people sell
            //he knows he is going against the current, so he is dealing with a bit less poof than the trend follower
            int requestDifference = SimulationManager.getInstance().GetRequestDifference();
            exchange.difference = -requestDifference * (random.nextDouble(requestDifference/10)+1) * (random.nextDouble(10)+1);
            exchange.percent = random.nextDouble(0.4)+0.25; //will deal with 25-65% of his current currency
        }
        else if (type == TraderType.EVENT_FOLLOWER)  //makes decisions based on the events
        {
            //sell and buys based on the positive or negative impact of an event
            double influence = SimulationManager.getInstance().GetEventThisCycle().GetEventInfluence();
            if(influence > 1)
                exchange.difference = influence * 100;
            else
                exchange.difference = -influence * 100;
            exchange.percent = random.nextDouble(0.4)+0.3; //will deal with 30-70% of his current currency

        }
        else if (type == TraderType.PSYCHOPATH)  //makes decisions completely randomly?
        {
            //completely random behaviour

            //random difference between -100 and 100
            exchange.difference = random.nextDouble(200)-100;
            //will deal with 5-95% of his current currency
            exchange.percent = random.nextDouble(.9)+0.05;
        }


        //every type's decision is influenced by the current event and the trend
        CalculateNormalInfluences(exchange);

        //there is a 20% chance for the trader to make a bigger decision
        if(random.nextInt(5) == 0)
        {
            exchange.difference *= 1.5;

            if(type == TraderType.PSYCHOPATH)
            {
                //the influence of everything has to be big enough for the trader to make a request
                if(Math.abs(exchange.difference) >= 25)
                    MakeTheTradeRequest(exchange);
                return;
            }
        }

        //everyone has a 5% chance of deciding based on complete randomness, like a psychopath
        if(random.nextInt(20) == 0)
        {
            //completely random behaviour

            //random difference between -100 and 100
            exchange.difference = random.nextDouble(200)-100;
            //will deal with 5-95% of his current currency
            exchange.percent = random.nextDouble(.9)+0.05;
        }

        //the influence of everything has to be big enough for the trader to make a request
        if(Math.abs(exchange.difference) >= 25)
            MakeTheTradeRequest(exchange);
    }

    //this is called by the traders, since they can either sell or buy
    //passing the variables as references is impossible in java T_T -> created new class called Exchange
    protected void CalculateNormalInfluences(Exchange exchange)
    {
        //if there are only a few traders, they are more very likely to buy
        if(Network.getInstance().GetTraderAmount() == 1)
        {
            exchange.difference += 35;
            //they buy between 60% and 100%
            exchange.percent = random.nextDouble(0.4)+0.6;
        }
        /*
        else if(Network.getInstance().GetMinerAmount() < 5)
        {
            //they buy between 40% and 100%
            exchange.percent = random.nextDouble(0.6)+0.4;
        }
        */

        //change the difference and the percentage values slightly, based on events, buying and selling trends, and people leaving or joining

    }

    private void MakeTheTradeRequest(Exchange exchange)
    {
        if(exchange.difference > 0)
        {
            amountOfPendingRequests++;

            double amountToBuy;
            if(random.nextInt(5) == 0)
                amountToBuy = random.nextDouble(10);
            else
                amountToBuy = exchange.difference/10 * Math.max(SimulationManager.getInstance().marketPrice/1000, 1) * (random.nextDouble(1)+1);

            RequestToBuy(amountToBuy);
            //setting the UI outline
            //PoofController.getInstance().SetTraderOutline(traderGUI, greenColor);

            //increment the cycle request amount
            //we do this here because the miner class uses the same RequestToSell method in the User and overriding would be tricky
            SimulationManager.getInstance().requestLinkHead.cycleBuyingRequestAmount++;
        }
        else if (hypotheticalPoofWallet > 0)
        {
            amountOfPendingRequests++;

            exchange.difference *= -1;
            double feePercent = random.nextDouble(0.05)+0.02; //feePercent is a random between 2 and 7
            //calculate the actual amount based on difference and exchangePercent
            double amountToSell = hypotheticalPoofWallet * Math.min((exchange.percent+(exchange.difference/10)), 1);

            RequestToSell(exchange.difference * (1 - feePercent), exchange.difference * feePercent);
            //setting the UI outline
            //PoofController.getInstance().SetTraderOutline(traderGUI, redColor);

            hypotheticalPoofWallet -= amountToSell;

            //increment the cycle request amount
            //we do this here because the miner class uses the same RequestToSell method in the User and overriding would be tricky
            SimulationManager.getInstance().requestLinkHead.cycleSellingRequestAmount++;
        }

        //this trader won't trade for the next 1-3 turns (2-6 sec)
        cycleUntilPossibleNextExchange = random.nextInt(3)+1;
    }

    public void RequestToBuy(double amount)
    {
        //TransactionRequest request = new TransactionRequest(this, publicKeyString, TransactionType.BUY, amount, 0);
        BuyingRequest request = new BuyingRequest(this, amount);
        try
        {
            Network.getInstance().ProcessBuyingRequest(request);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    Date startDate;
    @Override
    public void IncreaseWallet(double amount)
    {
        startDate = new Date();
        super.IncreaseWallet(amount);
        if (amount < 0){
            PoofController.getInstance().SetTraderGUICoin(traderGUI,decFormatter.format(poofWallet), redColor);
            SimulationManager.getInstance().GetRequestUIThread().AddTraderStartingTime(traderGUI);
        } else  {
            PoofController.getInstance().SetTraderGUICoin(traderGUI,decFormatter.format(poofWallet), greenColor);
            SimulationManager.getInstance().GetRequestUIThread().AddTraderStartingTime(traderGUI);
        }
    }

}
