package com.example.poof_ui.Blockchain_Side;

import com.example.poof_ui.CurrentEventManager;
import com.example.poof_ui.PoofController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private int currentListLength = 0;
    private int maxListLength = 5;

    //the requests that happened in the last update cycle
    public RequestLink requestLinkHead = new RequestLink();

    //the requests that happened in the 5th last update cycle
    public RequestLink requestLinkTail;

    public float previousMarketPrice;

    private ArrayList<String> NAMES = new ArrayList<>(Arrays.asList(
            "Alejandro", "Andrés", "Angel", "Antonio", "Armando", "Arturo", "Benjamín", "Carlos", "César", "Daniel",
            "David", "Eduardo", "Emilio", "Enrique", "Esteban", "Felipe", "Fernando", "Francisco", "Gabriel", "Germán",
            "Guillermo", "Ignacio", "Ismael", "Javier", "Jesús", "Jorge", "José", "Juan", "Luis", "Manuel", "Marco",
            "Mario", "Miguel", "Nicolás", "Pablo", "Pedro", "Rafael", "Ramón", "Raúl", "Ricardo", "Roberto", "Rodrigo",
            "Salvador", "Samuel", "Santiago", "Sergio", "Víctor", "William", "Xavier", "Yasser", "Abel", "Adrián",
            "Agustín", "Aitor", "Alán", "Aldo", "Álvaro", "Amado", "Amaury", "Ángel Luis", "Aníbal", "Ariel", "Arnaldo",
            "Arturo", "Axel", "Baltasar", "Bautista", "Bernardo", "Braulio", "Bruno", "Camilo", "Cándido",
            "César Augusto", "Claudio", "Cornelio", "Cristian", "Cristóbal", "Damián", "Danilo", "Darío", "Delfín",
            "Diego", "Dimas", "Dionisio", "Domingo", "Edmundo", "Eduardo José", "Efraín", "Eladio", "Elías", "Eliezer",
            "Elpidio", "Eloy", "Elvis", "Emiliano", "Eneas", "Epifanio", "Ernesto", "Eulogio", "Ezequiel", "Fabián",
            "Fausto", "Félix", "Fermín", "Fidel", "Filiberto", "Florián", "Fortunato", "Gabriel", "Gadiel", "Gaspar",
            "Genaro", "Geraldo", "Gerardo", "Gilberto", "Gonzalo", "Gregorio", "Gumersindo", "Gustavo", "Heriberto",
            "Hermógenes", "Higinio", "Hilario", "Homero", "Honorio", "Horacio", "Hugo", "Humberto", "Iker", "Inocencio",
            "Isaac", "Isaías", "Isidro", "Ismael", "Israel", "Ítalo", "Jacobo", "Jaime", "Janiel", "Jardiel", "Jeremías",
            "Joel", "Jonás", "Jorge Luis", "Josafat", "Joselito", "Josué", "Jovanny", "Juan Carlos", "Juan Pablo",
            "Juan Ramón", "Julián", "Julio", "Julián Alberto", "Laureano", "Lázaro", "Leandro", "Leocadio", "Leonel",
            "Leonardo", "Leopoldo", "Lisandro", "Lorenzo", "Lucas", "Luciano", "Luis Alberto", "Luis Enrique",
            "Luis Fernando", "Luís Manuel", "Luís Miguel", "Luís Ramón", "Luís Roberto", "Luisito", "Macario",
            "Manuel Alejandro", "Marcelino", "Marcelo", "Marco Antonio", "Marcos", "Mario Alberto", "Mario Antonio",
            "Mario José", "Mario Luis", "Mario Roberto", "Martín", "Mateo", "Matías", "Mauricio", "Maximiliano",
            "Melchor", "Miguel Ángel", "Moisés", "Nahun", "Néstor", "Nicanor", "Nicolás Antonio", "Octavio", "Odalis",
            "Olegario", "Omar", "Onofre", "Orlando", "Osvaldo", "Otto", "Pablo Antonio", "Pascual", "Patricio",
            "Pedro Antonio", "Primitivo", "Ramón Antonio", "Ramón Emilio", "Raúl Alberto", "Raúl Andrés", "Raúl Antonio",
            "Raúl Emilio", "Raúl Francisco", "Raúl José", "Raúl Manuel", "Raúl Ramón", "Régulo", "Reinaldo", "Renato",
            "Ricardo Antonio", "Ricardo José", "Ricardo Manuel", "Ricardo Ramón", "Richard", "Roberto Antonio",
            "Roberto Carlos", "Roberto José", "Roberto Manuel", "Robinson", "Rodolfo", "Rodrigo Antonio", "Rodrigo José",
            "Rodrigo Manuel", "Rogelio", "Rolando", "Román", "Rommel", "Ronny", "Roque", "Rubén", "Rubiel", "Rufino",
            "Salvador Antonio", "Samuel Antonio", "Samuel"));

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

    private void PrintRequestsUI()
    {
        RequestLink current = requestLinkTail;
        int currentIndex = 0;
        while(current.next != null)
        {
            PoofController.getInstance().SetBuyingRequestLabelGUI(currentIndex, current.cycleBuyingRequestAmount);
            PoofController.getInstance().SetSellingRequestLabelGUI(currentIndex, current.cycleSellingRequestAmount);

            currentIndex++;
            current = current.next;
        }
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
        //PoofController.getInstance().SetBuyingRequestLabelGUI(currentListLength, random.nextInt(10));


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
            if(currentListLength == 0)
                requestLinkTail = requestLinkHead;

            //PoofController.getInstance().SetSellingRequestLabelGUI(currentListLength, 15);

            //adding new element and setting the head point there
            RequestLink newHead = new RequestLink();
            requestLinkHead.next = newHead;
            newHead.previous = requestLinkHead;
            requestLinkHead = newHead;

            //we increment the length
            currentListLength++;
        }

        PrintRequestsUI();
    }
    //----------------


    public String GetRandomNameGenerator()
    {
        int randomIndex = random.nextInt(NAMES.size());
        String randomName = NAMES.get(randomIndex);
        NAMES.remove(randomIndex);
        return randomName;
    }

    public static SimulationManager getInstance() {
        if (instance == null)
            instance = new SimulationManager();

        return instance;
    }

    private SimulationManager()
    {
        //make the very first miner join the network
        for (int i = 0; i < 1; i++)
        {
            Miner miner1 = new Miner(MinerType.GROUP, GetMinerSleepingTime(MinerType.GROUP), GetRandomNameGenerator());
            miner1.start();
        }
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
                        wait(10);

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
            Trader firstTrader = new Trader(typeToJoin, GetRandomNameGenerator());
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
            if(Network.getInstance().GetMinerAmount() == 30)
                break;

            Miner newMiner = new Miner(MinerType.THESE_GUYS, GetMinerSleepingTime(MinerType.THESE_GUYS), GetRandomNameGenerator());
            newMiner.start();
        }

        for (int i = 0; i < traderAmountToJoin; i++)
        {
            if(Network.getInstance().GetTraderAmount() == 30)
                break;

            Trader newTrader = new Trader(TraderType.RISK_APPETITE, GetRandomNameGenerator());
            newTrader.start();
        }

    }

    private void DetermineMarketPrice()
    {
        previousMarketPrice = marketPrice;
        double marketPriceChange = 0;

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

        //check whether an event happened or not


        //if not, then
        if(random.nextInt(5) == 0) //there is a 20% chance that the value be just random between these two and not increasing or decreasing
        {
            marketPrice += marketPriceChange;
            if(marketPrice < 0)
                marketPrice = 0;
            SetPriceUI();
            return;
        }
        // GetBuyingRequestAmountDifference and GetSellingRequestAmountDifference return a pos number if more requests were made this cycle.

        //when there are more buying requests, t
        //marketPrice += GetBuyingRequestAmountDifference() * random.nextFloat();

        //marketPrice -= GetSellingRequestAmountDifference() * random.nextFloat();

        int buyerSellerDifference = requestLinkHead.cycleBuyingRequestAmount - requestLinkHead.cycleSellingRequestAmount;
        int totalRequests = requestLinkHead.cycleBuyingRequestAmount + requestLinkHead.cycleSellingRequestAmount;

        /*
        System.out.println("...................");
        System.out.println("selling requests last cycle: " + requestLinkHead.cycleSellingRequestAmount);
        System.out.println("buying requests last cycle: " + requestLinkHead.cycleBuyingRequestAmount);
        System.out.println("buyerSellerDifference: " + buyerSellerDifference);
        System.out.println("...................");
        */



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
            marketPriceChange += buyerSellerDifference * (1+marketPrice/100) * (random.nextFloat(0.75f)+0.75);
        else if (totalRequests > 10)
            marketPriceChange += totalRequests/10 * (1+marketPrice/100) * (random.nextFloat(0.5f)+0.5);


        if(marketPrice > 0)
        {
            //can only change max 15%
            if (marketPriceChange > marketPrice * 0.15)
                marketPriceChange = marketPrice * 0.15;
            else if (marketPriceChange < -marketPrice * 0.15)
                marketPriceChange = -marketPrice * 0.15;
        }

        marketPrice += marketPriceChange;

        if(marketPrice < 0)
            marketPrice = 0;

        SetPriceUI();
    }

    private void SetPriceUI()
    {
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

    private Double GetMinerSleepingTime(MinerType minerType)
    {
        if(minerType == MinerType.THAT_ONE_GUY)
            return random.nextDouble(150)+50;  //mining power range -> 5-20
        else if(minerType == MinerType.THESE_GUYS)
            return random.nextDouble(25)+25;  //mining power range -> 20-40
        else if(minerType == MinerType.GROUP)
            return random.nextDouble(8.35)+16.65;  //mining power range -> 40-60
        else if(minerType == MinerType.SMALL_CORP)
            return random.nextDouble(4.15)+12.5;  //mining power range -> 60-80
        else if(minerType == MinerType.HUGE_CORP)
            return random.nextDouble(7.5)+5;   //mining power range -> 80-200

        //just for safety
        System.out.println("Something went wrong! Non-existing MinerType!");
        return random.nextDouble(100)+50;
    }

}
