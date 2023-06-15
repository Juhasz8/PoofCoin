package com.example.poof_ui.Blockchain_Side;

import com.example.poof_ui.PoofController;
import com.example.poof_ui.TraderGUI;

import java.util.Random;

enum TraderType { RISK_APPETITE, TREND_FOLLOWER, CONTRARIAN_APPROACH, EVENT_FOLLOWER, PSYCHOPATH }

public class Trader extends User
{

    public TraderType type;

    private TraderGUI traderGUI;

    public Trader(TraderType type)
    {
        super();
        this.type = type;
        Network.getInstance().JoinTraderToTheNetwork(this);

        traderGUI = new TraderGUI();
        PoofController.getInstance().SetTraderGUICoin(traderGUI, decFormatter.format(0));
        PoofController.getInstance().AddTraderGUI(traderGUI);
    }

    public void run()
    {
        synchronized (this)
        {
            while (true)
            {
                try
                {
                    if (isSuspended)
                        wait();

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
        if(cycleUntilPossibleNextExchange > 0 || random.nextInt(10) == 0)
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
            //whatever he decides to do, he will deal with a lot of his current money or poof
            exchange.percent = random.nextDouble(.4)+0.35; //will deal with 35-75% of his current currency

            //sells fewer times

            //buys if the price is very low, or the amount of people buying is very few

        }
        else if (type == TraderType.TREND_FOLLOWER)  //makes decisions based on what the majority is doing
        {
            //sells if a lot of people sell

            //buys if a lot of people buy
        }
        else if (type == TraderType.CONTRARIAN_APPROACH)  //makes decisions based on what the minority is doing
        {
            //sells if a lot of people buy

            //buys if a lot of people sell
        }
        else if (type == TraderType.EVENT_FOLLOWER)  //makes decisions based on the events
        {
            //sell and buys based on the positive or negative impact of an event



        }
        else if (type == TraderType.PSYCHOPATH)  //makes decisions completely randomly?
        {
            //completely random behaviour

            //random difference between -100 and 100
            exchange.difference = random.nextInt(200)-100;
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
                //MakeTheTradeRequest(difference, exchangePercent);
                return;
            }
        }

        //everyone has a 5% chance of deciding based on complete randomness, like a psychopath
        if(random.nextInt(20) == 0)
        {
            //completely random behaviour

            //random difference between -100 and 100
            exchange.difference = random.nextInt(200)-100;
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
        else if(Network.getInstance().GetMinerAmount() < 5)
        {
            //they buy between 40% and 100%
            exchange.percent = random.nextDouble(0.4)+0.6;
        }


        //change the difference and the percentage values slightly, based on events, buying and selling trends, and people leaving or joining

    }

    private void MakeTheTradeRequest(Exchange exchange)
    {
        //double feePercent = random.nextDouble(5)+2;
        double feePercent = random.nextDouble(0.05)+0.02; //feePercent is a random between 2 and 7

        //calculate the actual amount based on difference and exchangePercent

        if(exchange.difference > 0)
            RequestToBuy(exchange.difference);
        else if (hypotheticalPoofWallet > 0)
        {
            RequestToSell(exchange.difference * (1 - feePercent), exchange.difference * feePercent);


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

    @Override
    public void IncreaseWallet(double amount)
    {
        super.IncreaseWallet(amount);
        PoofController.getInstance().SetTraderGUICoin(traderGUI, decFormatter.format(poofWallet));
    }

}
