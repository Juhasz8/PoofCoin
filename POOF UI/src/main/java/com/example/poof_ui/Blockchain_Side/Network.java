package com.example.poof_ui.Blockchain_Side;

import java.util.*;


//should also have a type REWARD, which is used by the miners
enum TransactionType { NORMAL, REWARD }

//class used to listen for broadcasted transaction requests, and connect them with eachother
//after these transaction requests become connected, it broadcasts it towards every miner.
//then the miners will create the block from them, and try to mine it
public class Network
{

    private static Network instance;
    private ArrayList<SellingRequest> sellingRequests = new ArrayList<>();
    private ArrayList<BuyingRequest> buyingRequests = new ArrayList<>();

    private ArrayList<Transaction> matches = new ArrayList<>();

    private float minerReward = 10;
    public float GetMinerReward() { return minerReward; }

    private int miningDifficulty = 2;
    public int GetDifficulty() { return miningDifficulty; }

    private int maxTransactionOnLedger = 15;
    public int GetMaxLedgerCount() { return maxTransactionOnLedger; }

    private ArrayList<Miner> miners = new ArrayList<>();
    private ArrayList<Trader> traders = new ArrayList<>();

    // an array of random names
    private int[] amountOfUsedNames = new int[3];

    private Random rand = new Random();

    public FullNode fullNode = new FullNode();
    public Map<String, User> networkUsers = new HashMap<>();

    public static Network getInstance()
    {
        if(instance == null)
            instance = new Network();

        return instance;
    }

    private Network()
    {
        for(int i = 0; i < amountOfUsedNames.length; i++)
        {
            amountOfUsedNames[i] = 0;
        }
    }

    private void TryToMatch()
    {

        if(sellingRequests.size() == 0 || buyingRequests.size() == 0)
            return;

        Collections.sort(sellingRequests);

        //for(int i = 0; i < sellingRequests.size(); i++)
        while (sellingRequests.size() > 0 && buyingRequests.size() > 0)
        //for(int i = sellingRequests.size()-1; i >= 0; i--)
        {
            //System.out.println("REQ: " + request.user + request.tradeAmount + " " + request.transactionFee );

            User seller = sellingRequests.get(0).user;
            Trader trader = buyingRequests.get(0).trader;

            Transaction transaction = GetMatch(seller, trader);

            //byte[] originalMessage = Cryptography.Convert(match);
            //convert the match into an array of bits, and pass these bytes and the publicKey of the buyer to the seller to sign
            //byte[] signedMessage = seller.Sign(originalMessage);

            transaction.SignTransaction(seller);

            for(int i = 0; i < miners.size(); i++)
            {
                miners.get(i).ProcessLedger(transaction);
            }

            fullNode.waitingTransSinceLastTrustedBlock.add(transaction);
            //ask for the verification of the trader for the signed transaction
//            if(trader.VerifySignedMessage(originalMessage, signedMessage, seller.publicKey))
//            {
//                //pass the message to the miners
//
//                System.out.println("VERIFIED MATCH: " + seller.name + " sends " + match.amount + " puffs to "+ trader.name);
//                matches.add(match);
//            }
//            else
//            {
//                System.out.println("The Buyer didnt verify the Transaction! ");
//            }

            AdjustRequests();
        }
    }

    private Transaction GetMatch(User seller, Trader trader)
    {
        //the seller sells more
        if(sellingRequests.get(0).tradeAmount > buyingRequests.get(0).tradeAmount)
            return new Transaction(TransactionType.NORMAL, seller.publicKeyString, trader.publicKeyString, buyingRequests.get(0).tradeAmount, sellingRequests.get(0).transactionFee);
        else
            return new Transaction(TransactionType.NORMAL, seller.publicKeyString, trader.publicKeyString, sellingRequests.get(0).tradeAmount, sellingRequests.get(0).transactionFee);
    }

    private void AdjustRequests()
    {
        if(sellingRequests.get(0).tradeAmount > buyingRequests.get(0).tradeAmount)
        {
            sellingRequests.get(0).tradeAmount -= buyingRequests.get(0).tradeAmount;
            buyingRequests.remove(0);
        }
        else
        {
            buyingRequests.get(0).tradeAmount -= sellingRequests.get(0).tradeAmount;
            if(buyingRequests.get(0).tradeAmount == 0)
                buyingRequests.remove(0);

            sellingRequests.remove(0);
        }
    }

    public void ProcessBuyingRequest(BuyingRequest request)
    {
        //store the request here
        buyingRequests.add(request);
        //increment the cycle request amount
        SimulationManager.getInstance().requestLinkHead.cycleBuyingRequestAmount++;

        TryToMatch();
    }

    public void ProcessSellingRequest(SellingRequest request)
    {
        //store the request in the network
        sellingRequests.add(request);

        TryToMatch();
    }

    public void JoinMinerToTheNetwork(Miner miner)
    {
        //adding the miner to the list of miners in the network
        miners.add(miner);
        //adding the user itself in the hashmap, with the key of the users publicKey
        networkUsers.put(miner.publicKeyString, miner);
    }

    public void JoinTraderToTheNetwork(Trader trader)
    {
        //adding the trader to the list of traders
        traders.add(trader);
        //adding the user itself in the hashmap, with the key of the users publicKey
        networkUsers.put(trader.publicKeyString, trader);
    }

    public void NewBlockWasMined(Block newBlock, String luckyMinerPublicKey)
    {
        System.out.println("miner: + " + luckyMinerPublicKey);
        System.out.println("is trying to enter method with root: + " + newBlock.dataTree.merkleRoot);
        //we notify the full nodes about the new block mined
        fullNode.NotifyNodeThatNewBlockWasMined(new FullNodeBlock(newBlock, luckyMinerPublicKey));
    }

    public void NotifyMinersAboutNewBlockMined(FullNodeBlock fullNodeBlock)
    {
        //we notify every miner in the network about the new Block
        for(int i = 0; i < miners.size(); i++)
        {
            if(miners.get(i).publicKeyString != fullNodeBlock.luckyMinerPublicKey)
                miners.get(i).SomebodyElseMinedABlock(fullNodeBlock.block);
        }
    }

    public int GetTraderAmount()
    {
        return traders.size();
    }

    public int GetMinerAmount()
    {
        return miners.size();
    }
}
